package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.schema.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.err;
import static java.lang.Thread.*;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpResponse.BodyHandlers;

public class OtHttpSink implements OtSink {

  private static final String ThreadName = "otel-http-sink";

  private static int      FlushIntervalMs = 5000;
  private static Duration ClientTimeout = Duration.ofSeconds(5);
  private static int      MaxRetries = 3;
  private static int      RetryDelayMs = 1000;

  private volatile boolean                  running = true;
  private final ShObjectWriter              objectWriter = new ShObjectWriter(true, false);
  private final BlockingQueue<OtLogRecord>  logQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<OtSpan>       spanQueue = new LinkedBlockingQueue<>();
  private final URI                         collectorUri;
  private final HttpClient                  client;

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
          err.printf("%s - Failed to send logs: [%s, %s]%n", ThreadName, response.statusCode(), response.body());
          if (attempt < MaxRetries) {
            retry(httpClient, request, attempt);
          }
        }
      })
      .exceptionally(e -> {
        err.printf("%s - Exception while sending logs: %s%n", ThreadName, e.getMessage());
        if (attempt < MaxRetries) {
          retry(httpClient, request, attempt);
        }
        return null;
      });
  }

  private void sendLogs(List<OtLogRecord> logRecords) {
    // TODO create log batch structure
    var logPayload = objectWriter.apply(logRecords);
    var request = HttpRequest.newBuilder()
      .uri(collectorUri)
      .timeout(ClientTimeout)
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofString(logPayload))
      .build();
    sendWithRetries(client, request, 0);
  }

  private void process() {
    var logBatch = new ArrayList<OtLogRecord>();
    logQueue.drainTo(logBatch);
    if (!logBatch.isEmpty()) {
      sendLogs(logBatch);
    }
  }

  public OtHttpSink start() {
    var dispatcherThread = new Thread(() -> {
      while (running) {
        try {
          sleep(FlushIntervalMs);
          process();
        } catch (InterruptedException e) {
          currentThread().interrupt();
          break;
        }
      }
      process();
    }, ThreadName);
    dispatcherThread.setDaemon(true);
    dispatcherThread.start();
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    return this;
  }

  public void shutdown() {
    running = false;
  }

  @Override public void accept(OtSpan sp) {

  }

  @Override public void accept(OtLogRecord lr) {
    logQueue.offer(lr);
  }

}
