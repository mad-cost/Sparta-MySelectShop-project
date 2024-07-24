package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public static final int MIN_MY_PRICE = 100; // 최저가 상수 설정

  public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
    Product product = productRepository.save(new Product(requestDto, user));
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
  public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
    // 정렬 방법: 받아온 파라미터가 true -> 오름차순, false -> 내림차순
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    // Sort객체 만들기: Sort.by(direction, 파라미터 정렬 항목)
    Sort sort = Sort.by(direction, sortBy);
    // Pageable객체 만들기: PageRequest(인터페이스 구현체).of(현재 페이지, 데이터 노출 개수, 정렬 방법)
    Pageable pageable = PageRequest.of(page, size, sort);

    // 유저 권한 구별
    UserRoleEnum userRoleEnum = user.getRole();

    Page<Product> productList; // interface Page

    if (userRoleEnum == UserRoleEnum.USER) { // 일반 유저
      productList = productRepository.findAllByUser(user, pageable);
    }else { // 관리자
      productList = productRepository.findAll(pageable);
    }
    // "<U> Page<U> map(Function<? super T, ? extends U> converter);"의 map()사용하여 변환
    // Page<Product> -> Page<ProductResponseDto>
    return productList.map(ProductResponseDto::new);
  }

  @Transactional
  public void updateBySearch(Long id, ItemDto itemDto) {
    Product product = productRepository.findById(id).orElseThrow(()->
            new NullPointerException("해당 상품은 존재하지 않습니다"));
    product.updateByItemDto(itemDto); // DirtyChecking

  }

  // 관리자는 모든 계정에서 등록한 상품을 볼 수 있다
  public List<ProductResponseDto> getAllProducts() {
    List<Product> productList = productRepository.findAll();
    // 반환해줄 Dto 생성
    List<ProductResponseDto> responseDtoList = new ArrayList<>();

    for (Product product : productList){
      responseDtoList.add(new ProductResponseDto(product));
    }

    return responseDtoList;

  }
}
