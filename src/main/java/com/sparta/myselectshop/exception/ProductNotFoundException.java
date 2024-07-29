package com.sparta.myselectshop.exception;

// custom으로 exception 만들기
public class ProductNotFoundException extends RuntimeException{
  // ctrl + o 를 통하여 오버라이드 하고싶은 메세지를 가져올 수 있다.
  public ProductNotFoundException(String message) {
    super(message);
  }
}
