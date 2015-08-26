package com.yixiang.wlyx.model;

public class TestBean {
  private String message;

  public void sayHell() {
    System.out.println(getMessage());
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
