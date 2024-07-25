package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final FolderRepository folderRepository;
  private final ProductFolderRepository productFolderRepository;

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
  @Transactional(readOnly = true)
  // productList.map(ProductResponseDto::new)에서 지연로딩 발생 함으로 Transactional 필수
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
    // 형변환: productList -> ProductResponseDto / Page 에서 converter 제공
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

  // 관심 상품에 폴더를 추가
  public void addFolder(Long productId, Long folderId, User user) {

    Product product = productRepository.findById(productId).orElseThrow(()->
            new NullPointerException("해당 상품이 존재하지 않습니다."));

    Folder folder= folderRepository.findById(folderId).orElseThrow(()->
            new NullPointerException("해당 폴더가 존재하지 않습니다."));

    // 해당 상품과, 폴더가 유저가 등록한 상품이 맞는지 검사
    // product.getUser().getId(): 해당 상품을 등록한 모든 유저 id를 가져온다
    if (!product.getUser().getId().equals(user.getId())
    || !folder.getUser().getId().equals(user.getId())){
      // user가 등록한 상품이나 폴더가 아닐경우 실행
      throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다");
    }

    // 중복 확인: ProductFolder에 해당하는 product_id, folder_id값을 가지는 데이터가 찾아지면 중복되는 데이터이다
    Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product, folder);

    // isPresent(): 객체가 값을 포함하고 있는지 여부를 확인, 주로 null을 처리하는데 사용
    if (overlapFolder.isPresent())  {
      // 값이 있어서 true가 반환 됐다면, 중복된 데이터
      throw new IllegalArgumentException("중복된 폴더 입니다");
    }

    // 검증이 끝났다면 ProductFolder에 등록
    productFolderRepository.save(new ProductFolder(product, folder));

  }

  public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
    // 페이징 처리
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    // Product와 ProductFolder는 양방향 관계,
    // 즉, Product에서 User 객체를 찾고 / ProductFolderList_FolderId: ProductFolderList의 FolderId에 해당하는 Product(productRepository)를 찾아라
    Page<Product> productList= productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);

    // 형변환: productList -> ProductResponseDto / Page 에서 converter 제공
    Page<ProductResponseDto> responseDtos = productList.map(ProductResponseDto::new);
    return responseDtos;
  }
}
