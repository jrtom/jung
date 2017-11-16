package edu.uci.ics.jung.layout.util;

/**
 * @author Tom Nelson
 * @param <T>
 */
public class ChangeEvent<T> {

  private final T source;

  public ChangeEvent(T source) {
    this.source = source;
  }

  public T getSource() {
    return source;
  }
}
