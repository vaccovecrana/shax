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

  private static final String OtThread = "otel-http-sink";

  private static int      FlushIntervalMs = 5000;
  private static Duration ClientTimeout   = Duration.ofSeconds(3);

  private final ShObjectWriter              objectWriter = new ShObjectWriter(true, false);
  private final BlockingQueue<OtLogRecord>  logQueue = new LinkedBlockingQueue<>();
  private final BlockingQueue<OtSpan<?>>    spanQueue = new LinkedBlockingQueue<>();
  private final URI                         collectorUri;
  private final ScheduledExecutorService    scheduler = Executors.newSingleThreadScheduledExecutor(this);
  private final byte[]                      responseBuff = new byte[2048];

  private volatile Socket currentSocket = null;

  public OtHttpSink(URI collectorUri) {
    this.collectorUri = collectorUri;
  }

  @Override public Thread newThread(Runnable r) {
    return new Thread(r, format("%s-%s", OtThread, toHexString(r.hashCode())));
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

  private void createNewSocket() {
    try {
      var scheme = collectorUri.getScheme();
      var isHttps = "https".equalsIgnoreCase(scheme);
      int port = collectorUri.getPort() == -1
        ? (isHttps ? 443 : 80)
        : collectorUri.getPort();
      if (isHttps) {
        var sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        currentSocket = sslSocketFactory.createSocket();
      } else {
        currentSocket = new Socket();
      }
      currentSocket.connect(new InetSocketAddress(collectorUri.getHost(), port), (int) ClientTimeout.toMillis());
      currentSocket.setSoTimeout((int) ClientTimeout.toMillis());
    } catch (IOException e) {
      logError(e, "Failed to create Socket");
      closeCurrentSocket();
    }
  }

  private void sendHttpRequest(BufferedWriter out, String path, String payload) throws IOException {
    out.write("POST " + path + " HTTP/1.1\r\n");
    out.write("Host: " + collectorUri.getHost() + "\r\n");
    out.write("Content-Type: application/json\r\n");
    out.write("Content-Length: " + payload.length() + "\r\n");
    out.write("\r\n");
    out.write(payload);
    out.flush();
  }

  private void sendRequest(String path, String payload) {
    try {
      if (currentSocket == null || currentSocket.isClosed()) {
        createNewSocket();
      }
      if (currentSocket != null) {
        var out = new BufferedWriter(new OutputStreamWriter(currentSocket.getOutputStream()));
        sendHttpRequest(out, path, payload);
        Arrays.fill(responseBuff, (byte) 0);
        currentSocket.getInputStream().read(responseBuff);
      }
    } catch (IOException e) {
      var msg = format(
        "Failed to send OTEL request: [%s] - %s%n%s",
        path, e.getMessage(),
        responseBuff[0] != 0 ? new String(responseBuff).trim() : "?"
      );
      err.println(messageFormat(ERROR, currentTimeMillis(), currentThread().getName(), msg));
      closeCurrentSocket();
    }
  }

  private void logError(Exception e, String message) {
    err.println(messageFormat(
      ERROR, currentTimeMillis(), currentThread().getName(),
      format("%s: %s - %s", message, e.getClass().getSimpleName(), e.getMessage())
    ));
    e.printStackTrace(err);
  }

  private void processLogQueue() {
    try {
      var logBatch = new ArrayList<OtLogRecord>();
      logQueue.drainTo(logBatch);
      if (!logBatch.isEmpty()) {
        var payload = objectWriter.apply(OtContext.logBatchOf(logBatch));
        sendRequest("/v1/logs", payload);
      }
    } catch (Exception e) {
      logError(e, "Log processing failed");
    }
  }

  private void processSpanQueue() {
    try {
      var spanBatch = new ArrayList<OtSpan<?>>();
      spanQueue.drainTo(spanBatch);
      if (!spanBatch.isEmpty()) {
        var payload = objectWriter.apply(OtContext.spanBatchOf(spanBatch));
        sendRequest("/v1/traces", payload);
      }
    } catch (Exception e) {
      logError(e, "Span processing failed");
    }
  }

  public OtHttpSink start() {
    scheduler.scheduleAtFixedRate(() -> {
      processLogQueue();
      processSpanQueue();
    }, 0, FlushIntervalMs, TimeUnit.MILLISECONDS);
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
