package org.opentripplanner.standalone.config.framework;

public enum OtpVersion {
  /** @deprecated Replace this and remove when not in use. */
  @Deprecated
  NA("na"),
  V1_5("1.5"),
  V2_0("2.0"),
  V2_1("2.1"),
  V2_2("2.2");

  private final String text;

  OtpVersion(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
