package com.sparta.myselectshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Spring에서 예외를 전역적으로 처리하기 위해 사용하는 애노테이션
// Global 예외 처리 -> Controller에서 발생하는 모든 예외 처리를 GlobalExceptionHandler 클래스로 잡아 올 수 있다
public class GlobalExceptionHandler {
  // Json형태로 클라이언트에 예외를 보내줌으로 RestControllerAdvice 사용
  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<RestApiException> handleException(IllegalArgumentException ex) {
    RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(
            // HTTP body
            restApiException,
            // HTTP status code
            HttpStatus.BAD_REQUEST
    );
  }
}