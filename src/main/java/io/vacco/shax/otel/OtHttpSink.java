package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.schema.*;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Integer.toHexString;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.lang.Thread.*;
import static io.vacco.shax.logging.ShLogger.messageFormat;
import static io.vacco.shax.logging.ShLogLevel.*;

public class OtHttpSink implements OtSink, ThreadFactory {

  public static final int TimeoutDefaultMs = 10_000;

  private static final String OtThread = "otel-http-sink";

  private static final int    FlushIntervalMinMs = 500;
  private static final int    FlushIntervalMaxMs = 5000;
  private static final double OverheadPercent    = 0.15; // 25% overhead
  private static final double IntervalRatio      = 0.25;

  private final int flushIntervalMs;
  private final Duration clientTimeout;

  private final Map<String, String>         headers;
  private final ShObjectWriter              objectWriter = new ShObjectWriter(true, false);
  private final BlockingQueue<OtLogRecord>  logQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<OtSpan<?>>    spanQueue = new LinkedBlockingQueue<>();
  private final URI                         collectorUri;

  @SuppressWarnings("this-escape")
  private final ScheduledExecutorService    scheduler = Executors.newSingleThreadScheduledExecutor(this);
  private final byte[]                      responseBuff = new byte[2048];

  private volatile Socket currentSocket = null;

  public OtHttpSink(URI collectorUri, Map<String, String> headers, int timeoutMs) {
    this.collectorUri = Objects.requireNonNull(collectorUri);
    this.headers = Objects.requireNonNull(headers);
    int overheadMs = (int) (timeoutMs * OverheadPercent);
    int cycleTimeMs = Math.max(0, timeoutMs - overheadMs);
    int intervalMs = (int) (cycleTimeMs * IntervalRatio);
    this.flushIntervalMs = Math.max(FlushIntervalMinMs, Math.min(FlushIntervalMaxMs, intervalMs));
    this.clientTimeout = Duration.ofMillis(Math.min(timeoutMs / 3, timeoutMs - 1000));
  }

  @Override public Thread newThread(Runnable r) {
    return new Thread(r, format("%s-%s", OtThread, toHexString(r.hashCode())));
  }

  private void logError(Exception e, String message) {
    err.println(messageFormat(
      ERROR, currentTimeMillis(), currentThread().getName(),
      format("%s: %s - %s", message, e.getClass().getSimpleName(), e.getMessage())
    ));
  }

  private void closeCurrentSocket() {
    try {
      if (currentSocket != null) {
        currentSocket.close();
      }
    } catch (IOException e) {
      logError(e, "Failed to close socket");
    } finally {
      currentSocket = null;
    }
  }

  private void createNewSocket() throws IOException {
    var scheme = collectorUri.getScheme();
    var isHttps = "https".equalsIgnoreCase(scheme);
    int port = collectorUri.getPort() == -1 ? (isHttps ? 443 : 80) : collectorUri.getPort();
    if (isHttps) {
      var sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      currentSocket = sslSocketFactory.createSocket();
    } else {
      currentSocket = new Socket();
    }
    currentSocket.connect(new InetSocketAddress(collectorUri.getHost(), port), (int) clientTimeout.toMillis());
    currentSocket.setSoTimeout((int) clientTimeout.toMillis());
  }

  private void sendHttpRequest(BufferedWriter out, String path, String payload) throws IOException {
    out.write("POST " + path + " HTTP/1.1\r\n");
    out.write("Host: " + collectorUri.getHost() + "\r\n");
    out.write("Content-Type: application/json\r\n");
    out.write("Content-Length: " + payload.length() + "\r\n");
    for (var e : headers.entrySet()) {
      out.write(e.getKey() + ": " + e.getValue() + "\r\n");
    }
    out.write("\r\n");
    out.write(payload);
    out.flush();
  }

  private int parseHttpStatus(String response) {
    if (response == null || response.isEmpty()) {
      return -1;
    }
    var firstLine = response.split("\n")[0].trim();
    var parts = firstLine.split("\\s+");
    if (parts.length >= 2) {
      try {
        return Integer.parseInt(parts[1]);
      } catch (NumberFormatException ignored) {}
    }
    return -1;
  }

  private void sendRequest(String path, String payload) throws Exception {
    try {
      if (currentSocket == null || currentSocket.isClosed()) {
        createNewSocket();
      }
      if (currentSocket != null) {
        var out = new BufferedWriter(new OutputStreamWriter(currentSocket.getOutputStream()));
        sendHttpRequest(out, path, payload);
        Arrays.fill(responseBuff, (byte) 0);
        if (currentSocket.getInputStream().read(responseBuff) > 0) {
          var responseStr = new String(responseBuff).trim();
          int statusCode = parseHttpStatus(responseStr);
          if (statusCode < 200 || statusCode >= 300) {
            throw new IOException(format("HTTP error response: [%d] %s", statusCode, responseStr));
          }
        }
      }
    } catch (Exception e) {
      var res = new String(responseBuff).trim();
      var msg = format("Failed to send OTEL request: [%s]%n%s", path, res);
      logError(e, msg);
      closeCurrentSocket();
      throw e;
    }
  }

  private void processLogQueue() {
    var logBatch = new ArrayList<OtLogRecord>();
    try {
      logQueue.drainTo(logBatch);
      if (!logBatch.isEmpty()) {
        var payload = objectWriter.apply(OtContext.logBatchOf(logBatch));
        sendRequest("/v1/logs", payload);
      }
    } catch (Exception e) {
      logError(e, "Log processing failed");
      logQueue.addAll(logBatch);
    }
  }

  private void processSpanQueue() {
    var spanBatch = new ArrayList<OtSpan<?>>();
    try {
      spanQueue.drainTo(spanBatch);
      if (!spanBatch.isEmpty()) {
        var payload = objectWriter.apply(OtContext.spanBatchOf(spanBatch));
        sendRequest("/v1/traces", payload);
      }
    } catch (Exception e) {
      logError(e, "Span processing failed");
      spanQueue.addAll(spanBatch);
    }
  }

  public OtHttpSink start() {
    scheduler.scheduleAtFixedRate(() -> {
      processLogQueue();
      processSpanQueue();
    }, 0, flushIntervalMs, TimeUnit.MILLISECONDS);
    Runtime.getRuntime().addShutdownHook(newThread(this::shutdown));
    return this;
  }

  public void shutdown() {
    scheduler.shutdown();
    closeCurrentSocket();
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
