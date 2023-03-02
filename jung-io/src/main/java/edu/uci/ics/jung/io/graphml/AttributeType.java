package edu.uci.ics.jung.io.graphml;

public enum AttributeType {
  BOOLEAN("boolean"),
  INT("int"),
  LONG("long"),
  FLOAT("float"),
  DOUBLE("double"),
  STRING("string");

  private final String value;

  AttributeType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
