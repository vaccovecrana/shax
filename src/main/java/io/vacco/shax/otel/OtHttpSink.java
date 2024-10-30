package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.schema.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Integer.toHexString;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;
import static io.vacco.shax.logging.ShLogger.messageFormat;
import static io.vacco.shax.logging.ShLogLevel.*;

public class OtHttpSink implements OtSink, ThreadFactory {

  private static final String OtThread = "otel-http-sink";

  private static int      FlushIntervalMs = 5000;
  private static Duration ClientTimeout   = Duration.ofSeconds(3);

  private final ShObjectWriter              objectWriter = new ShObjectWriter(true, false);
  private final BlockingQueue<OtLogRecord>  logQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<OtSpan<?>>    spanQueue = new LinkedBlockingQueue<>();
  private final URI                         collectorUri;
  private final HttpClient                  client;
  private final ScheduledExecutorService    scheduler = Executors.newSingleThreadScheduledExecutor(this);
  private final ExecutorService             workerPool = Executors.newCachedThreadPool(this);

  public OtHttpSink(URI collectorEndpoint) {
    this.collectorUri = collectorEndpoint;
    this.client = HttpClient.newBuilder()
      .connectTimeout(ClientTimeout)
      .build();
  }

  @Override public Thread newThread(Runnable r) {
    return new Thread(r, format("%s-%s", OtThread, toHexString(r.hashCode())));
  }

  private void sendRequest(String path, String payload) {
    try {
      var request = HttpRequest.newBuilder()
        .uri(collectorUri.resolve(path))
        .timeout(ClientTimeout)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(payload))
        .build();
      var res = client.send(request, BodyHandlers.ofString());
      if (res.statusCode() != 200) {
        var msg = format(
          "Failed to send OTEL request: [%s, %s, %s]",
          request.uri(), res.statusCode(), res.body()
        );
        err.println(messageFormat(ERROR, currentTimeMillis(), currentThread().getName(), msg));
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void sendLogs(List<OtLogRecord> logRecords) {
    var payload = objectWriter.apply(OtContext.logBatchOf(logRecords));
    sendRequest("/v1/logs", payload);
  }

  private void sendSpans(List<OtSpan<?>> spanRecords) {
    var payload = objectWriter.apply(OtContext.spanBatchOf(spanRecords));
    sendRequest("/v1/traces", payload);
  }

  private void logError(Exception e, boolean logsOrSpans) {
    err.println(messageFormat(
      ERROR, currentTimeMillis(), currentThread().getName(),
      format(
        "Exception during OTEL %s processing: %s - %s",
        logsOrSpans ? "logs" : "spans",
        e.getClass().getSimpleName(), e.getMessage()
      )
    ));
    e.printStackTrace(err);
  }

  private void processLogQueue() {
    try {
      var logBatch = new ArrayList<OtLogRecord>();
      logQueue.drainTo(logBatch);
      if (!logBatch.isEmpty()) {
        sendLogs(logBatch);
      }
    } catch (Exception e) {
      logError(e, true);
    }
  }

  private void processSpanQueue() {
    try {
      var spanBatch = new ArrayList<OtSpan<?>>();
      spanQueue.drainTo(spanBatch);
      if (!spanBatch.isEmpty()) {
        sendSpans(spanBatch);
      }
    } catch (Exception e) {
      logError(e, false);
    }
  }

  public OtHttpSink start() {
    scheduler.scheduleAtFixedRate(() -> workerPool.submit(this::processLogQueue), 0, FlushIntervalMs, TimeUnit.MILLISECONDS);
    scheduler.scheduleAtFixedRate(() -> workerPool.submit(this::processSpanQueue), 0, FlushIntervalMs, TimeUnit.MILLISECONDS);
    Runtime.getRuntime().addShutdownHook(newThread(this::shutdown));
    return this;
  }

  public void shutdown() {
    scheduler.shutdown();
    workerPool.shutdown();
    err.println(messageFormat(
      INFO, currentTimeMillis(), currentThread().getName(),
      "OTEL HTTP Sink stopped."
    ));
  }

  @Override public void accept(OtSpan<?> sp) {
    spanQueue.offer(sp);
  }

  @Override public void accept(OtLogRecord lr) {
    logQueue.offer(lr);
  }

}
