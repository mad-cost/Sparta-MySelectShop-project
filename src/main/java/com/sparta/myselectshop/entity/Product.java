package com.sparta.myselectshop.entity;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.naver.dto.ItemDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // JPA가 관리할 수 있는 Entity 클래스 지정
@Getter
@Setter
@Table(name = "product") // 매핑할 테이블의 이름을 지정
@NoArgsConstructor
public class Product extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String image;

  @Column(nullable = false)
  private String link;

  @Column(nullable = false)
  private int lprice;

  @Column(nullable = false)
  private int myprice;

  // 지연 로딩: 상품을 조회 할 때마다 매 번 유저 정보가 필요하지는 않다
  @ManyToOne(fetch = FetchType.LAZY) // 단방향 연관 관계
  // nullable = false: DB에서 상품이 유저 없이 존재할 수 없다 (데이터 무결성 보장)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Product(ProductRequestDto requestDto, User user) {
    this.title = requestDto.getTitle();
    this.image = requestDto.getImage();
    this.link = requestDto.getLink();
    this.lprice = requestDto.getLprice();
    this.user = user;
  }

  public void update(ProductMypriceRequestDto requestDto) {
    this.myprice = requestDto.getMyprice();
  }

  public void updateByItemDto(ItemDto itemDto){
    // 기존에 등록되어있던 가격을 신규 가격으로 업데이트
    this.lprice = itemDto.getLprice();
  }
}