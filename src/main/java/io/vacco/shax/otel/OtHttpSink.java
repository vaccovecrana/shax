package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.logging.ShLogLevel;
import io.vacco.shax.otel.schema.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;
import static io.vacco.shax.logging.ShLogger.messageFormat;

public class OtHttpSink implements OtSink {

  private static final String OtThread = "otel-http-sink";

  private static int      FlushIntervalMs = 5000;
  private static Duration ClientTimeout = Duration.ofSeconds(3);
  private static int      MaxRetries = 3;
  private static int      RetryDelayMs = 1000;

  private final ShObjectWriter              objectWriter = new ShObjectWriter(true, false);
  private final BlockingQueue<OtLogRecord>  logQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<OtSpan<?>>    spanQueue = new LinkedBlockingQueue<>();
  private final URI                         collectorUri;
  private final HttpClient                  client;

  private volatile boolean running = true;

  public OtHttpSink(URI collectorEndpoint) {
    this.collectorUri = collectorEndpoint;
    this.client = HttpClient.newBuilder()
      .connectTimeout(ClientTimeout)
      .build();
  }

  private static void retry(HttpClient httpClient, HttpRequest request, int attempt) {
    try {
      sleep((long) RetryDelayMs * (attempt + 1));
    } catch (InterruptedException e) {
      currentThread().interrupt();
      return;
    }
    sendWithRetries(httpClient, request, attempt + 1);
  }

  private static void sendWithRetries(HttpClient httpClient, HttpRequest request, int attempt) {
    httpClient.sendAsync(request, BodyHandlers.ofString())
      .thenAccept(response -> {
        if (response.statusCode() != 200) {
          err.println(messageFormat(
            ShLogLevel.ERROR, currentTimeMillis(), String.format("%s-%s", OtThread, currentThread().getName()),
            String.format(
              "Failed to send OTEL request: [%s, %s, %s]",
              request.uri(), response.statusCode(), response.body()
            )
          ));
          if (attempt < MaxRetries) {
            retry(httpClient, request, attempt);
          }
        }
      })
      .exceptionally(e -> {
        err.println(messageFormat(
          ShLogLevel.ERROR, currentTimeMillis(), String.format("%s-%s", OtThread, currentThread().getName()),
          String.format("Exception sending OTEL request: %s - %s", e.getClass().getSimpleName(), e.getMessage())
        ));
        if (attempt < MaxRetries) {
          retry(httpClient, request, attempt);
        }
        return null;
      });
  }

  private void sendRequest(String path, String payload) {
    var request = HttpRequest.newBuilder()
        .uri(collectorUri.resolve(path))
        .timeout(ClientTimeout)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(payload))
        .build();
    sendWithRetries(client, request, 0);
  }

  private void sendLogs(List<OtLogRecord> logRecords) {
    var payload = objectWriter.apply(OtContext.logBatchOf(logRecords));
    sendRequest("/v1/logs", payload);
  }

  private void sendSpans(List<OtSpan<?>> spanRecords) {
    var payload = objectWriter.apply(OtContext.spanBatchOf(spanRecords));
    sendRequest("/v1/traces", payload);
  }

  private void processLogQueue() {
    var logBatch = new ArrayList<OtLogRecord>();
    logQueue.drainTo(logBatch);
    if (!logBatch.isEmpty()) {
      sendLogs(logBatch);
    }
  }

  private void processSpanQueue() {
    var spanBatch = new ArrayList<OtSpan<?>>();
    spanQueue.drainTo(spanBatch);
    if (!spanBatch.isEmpty()) {
      sendSpans(spanBatch);
    }
  }

  public OtHttpSink start() {
    var dispatcherThread = new Thread(() -> {
      while (running) {
        try {
          processLogQueue();
          processSpanQueue();
          sleep(FlushIntervalMs);
        } catch (InterruptedException e) {
          currentThread().interrupt();
          break;
        }
      }
      processLogQueue();
      processSpanQueue();
    }, OtThread);
    dispatcherThread.setDaemon(true);
    dispatcherThread.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    return this;
  }

  public void shutdown() {
    running = false;
  }

  @Override public void accept(OtSpan<?> sp) {
    spanQueue.offer(sp);
  }

  @Override public void accept(OtLogRecord lr) {
    logQueue.offer(lr);
  }

}
