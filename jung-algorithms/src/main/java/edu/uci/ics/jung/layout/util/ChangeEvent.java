package edu.uci.ics.jung.layout.util;

/** Created by tanelso on 11/14/17. */
public class ChangeEvent<T> {

  private final T source;

  public ChangeEvent(T source) {
    this.source = source;
  }

  public T getSource() {
    return source;
  }
}
