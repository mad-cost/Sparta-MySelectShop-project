package com.sparta.myselectshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Spring에서 예외를 전역적으로 처리하기 위해 사용하는 애노테이션
// Global 예외 처리 -> Controller에서 발생하는 모든 예외 처리를 GlobalExceptionHandler 클래스로 잡아 올 수 있다
public class GlobalExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<RestApiException> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
    RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(
            // HTTP body
            restApiException,
            // HTTP status code
            HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler({NullPointerException.class})
  public ResponseEntity<RestApiException> nullPointerExceptionHandler(NullPointerException ex) {
    RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(
            // HTTP body
            restApiException,
            // HTTP status code
            HttpStatus.NOT_FOUND
    );
  }

  // 직접 Custom한 exception
  @ExceptionHandler({ProductNotFoundException.class})
  public ResponseEntity<RestApiException> notFoundProductExceptionHandler(ProductNotFoundException ex) {
    RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(
            // HTTP body
            restApiException,
            // HTTP status code
            HttpStatus.NOT_FOUND
    );
  }
}