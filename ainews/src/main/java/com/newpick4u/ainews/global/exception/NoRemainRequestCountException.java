package com.newpick4u.ainews.global.exception;

public class NoRemainRequestCountException extends RuntimeException {

  public NoRemainRequestCountException() {
    super("Exceeded Gemini Max Request Count Per Min -> Move To DLQ");
  }
}
