package io.vacco.shax.otel.schema;

public class OtStatus {

  public OtStatusCode code;
  public String message;

  public OtStatus message(String message) {
    this.message = message;
    return this;
  }

  public static OtStatus otStatus(OtStatusCode code) {
    var s = new OtStatus();
    s.code = code;
    return s;
  }

}
