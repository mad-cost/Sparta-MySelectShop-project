package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {
  private final ProductService productService;

  // 관심상품 제품 등록
  @PostMapping("/products")
  public ProductResponseDto createProduct(
          // 일반적으로 RESTful 웹 서비스에서 클라이언트가 서버로 데이터를 전송할 때 사용
          @RequestBody // HTTP 요청의 본문(body)을 자바 객체로 변환하는 데 사용
          ProductRequestDto requestDto
  ){
    return productService.createProduct(requestDto);
  }

  // 관심상품 희망 최저가
  @PutMapping("/products/{id}")
  public ProductResponseDto updateProduct(
          @PathVariable
          Long id,
          @RequestBody
          ProductMypriceRequestDto requestDto // myprice(희망 금액)
  ){
    return productService.updateProduct(id, requestDto);
    // basic.js의 success가 실행되어 '/'로 이동
  }


}
