package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {
  private final ProductService productService;

  @PostMapping("/products")
  public ProductResponseDto createProduct(
          // 일반적으로 RESTful 웹 서비스에서 클라이언트가 서버로 데이터를 전송할 때 사용
          @RequestBody // HTTP 요청의 본문(body)을 자바 객체로 변환하는 데 사용
          ProductRequestDto requestDto
  ){
    return productService.createProduct(requestDto);
  }


}
