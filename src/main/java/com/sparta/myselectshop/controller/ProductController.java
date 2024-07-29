package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.ApiUseTime;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.ApiUseTimeRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {
  private final ProductService productService;
  private final ApiUseTimeRepository apiUseTimeRepository;

  // 관심상품 제품 등록
  @PostMapping("/products")
  public ProductResponseDto createProduct(
          // 일반적으로 RESTful 웹 서비스에서 클라이언트가 서버로 데이터를 전송할 때 사용
          @RequestBody // HTTP 요청의 본문(body)을 자바 객체로 변환하는 데 사용
          ProductRequestDto requestDto,
          @AuthenticationPrincipal // 회원 정보
          UserDetailsImpl userDetails
  ) {
    // 측정 시작 시간
    long startTime = System.currentTimeMillis();

    try {
      // 응답 보내기
      return productService.createProduct(requestDto, userDetails.getUser());
    } finally { // 무조건 실행
      // 측정 종료 시간
      long endTime = System.currentTimeMillis();
      // 수행시간 = 종료 시간 - 시작 시간
      long runTime = endTime - startTime;

      // 로그인 회원 정보
      User loginUser = userDetails.getUser();

      // API 사용시간 및 DB 에 기록
      ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
              .orElse(null);
      if (apiUseTime == null) {
        // 로그인 회원의 기록이 없으면
        apiUseTime = new ApiUseTime(loginUser, runTime);
      } else {
        // 로그인 회원의 기록이 이미 있으면
        apiUseTime.addUseTime(runTime);
      }

      System.out.println("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
      apiUseTimeRepository.save(apiUseTime);
    }
  }

  // 관심상품 희망 최저가
  @PutMapping("/products/{id}")
  public ProductResponseDto updateProduct(
          @PathVariable
          Long id,
          @RequestBody
          ProductMypriceRequestDto requestDto // myprice(희망 금액)
  ) {
    return productService.updateProduct(id, requestDto);
    // basic.js의 success가 실행되어 '/'로 이동
  }

  // 관심상픔(DB에 저장된 상품) 조회하기
  @GetMapping("/products")
  public Page<ProductResponseDto> getProducts(
          // 페이징 처리
          @RequestParam("page")
          int page, // 조회할 페이지 번호 (1부터 시작)
          @RequestParam("size")
          int size, // 한 페이지에 보여줄 상품 개수
          @RequestParam("sortBy")
          String sortBy, // 오름차순 버튼 or 내림차순 버튼
          @RequestParam("isAsc")
          boolean isAsc, // true, false
          // 회원 객체 가져오기
          @AuthenticationPrincipal
          UserDetailsImpl userDetails
  ) {
    return productService.getProducts(userDetails.getUser(),
            // page-1: 뷰에서는 0부터 시작하기 위해
            page - 1, size, sortBy, isAsc
    );
  }

  /* "관심 상품 조회하기"의 getProducts()메서드 에서 Admin인지 구별
  // 관리자는 모든 계정에서 등록한 상품을 볼 수 있다
  @GetMapping("/admin/products")
  public List<ProductResponseDto> getAllProducts(){
    return productService.getAllProducts();
  } */


  // 관심 폴더에 추가
  @PostMapping("/products/{productId}/folder")
  public void addFolder(
          @PathVariable
          Long productId,
          @RequestParam // ?key=value
          Long folderId,
          @AuthenticationPrincipal
          UserDetailsImpl userDetails
  ) {
    productService.addFolder(productId, folderId, userDetails.getUser());
  }

  // 폴더 클릭시 폴더에 해당하는 상품 가져오기
  @GetMapping("/folders/{folderId}/products")
  public Page<ProductResponseDto> getProductsInFolder(
          @PathVariable
          Long folderId,
          // 페이징 처리
          @RequestParam("page")
          int page, // 조회할 페이지 번호 (1부터 시작)
          @RequestParam("size")
          int size, // 한 페이지에 보여줄 상품 개수
          @RequestParam("sortBy")
          String sortBy, // 오름차순 버튼 or 내림차순 버튼
          @RequestParam("isAsc")
          boolean isAsc, // true, false
          @AuthenticationPrincipal // 유저 정보
          UserDetailsImpl userDetails
  ) {
    return productService.getProductsInFolder(
            folderId, page-1, size, sortBy, isAsc, userDetails.getUser()
    );
  }


}
