package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public static final int MIN_MY_PRICE = 100; // 최저가 상수 설정

  public ProductResponseDto createProduct(ProductRequestDto requestDto) {
    Product product = productRepository.save(new Product(requestDto));
    return new ProductResponseDto(product);
  }

  @Transactional
  public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
    // 받아온 ProductMypriceRequestDto객체의 myprice필드
    int myprice = requestDto.getMyprice();
    if (myprice < MIN_MY_PRICE) {
      throw new IllegalArgumentException("유효하지 않은 관심 가격 입니다. 최소 " + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
    }
    // 받아온 아이디 상품 존재 확인
    Product product = productRepository.findById(id).orElseThrow(() ->
            new NullPointerException("해당 상품을 찾을 수 없습니다.")
    );
    // Transactional의 DirtyChecking 사용: 받아온 상품에 myprice 함께 저장
    product.update(requestDto);

    return new ProductResponseDto(product);
  }

  // 관심 상품 조회
  public List<ProductResponseDto> getProducts() {
    List<Product> productList = productRepository.findAll();
    // 반환해줄 Dto 생성
    List<ProductResponseDto> responseDtoList = new ArrayList<>();

    for (Product product : productList){
      responseDtoList.add(new ProductResponseDto(product));
    }

    return responseDtoList;
  }

  @Transactional
  public void updateBySearch(Long id, ItemDto itemDto) {
    Product product = productRepository.findById(id).orElseThrow(()->
            new NullPointerException("해당 상품은 존재하지 않습니다"));
    product.updateByItemDto(itemDto); // DirtyChecking

  }
}
