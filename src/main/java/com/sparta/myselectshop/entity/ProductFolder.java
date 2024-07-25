package com.sparta.myselectshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
// N:M 관계의 중간 테이블 / 외래키의 주인
// 상품과 상품_폴더의 관계는 양방향 관계
// 폴더에서는 상품을 조회하지 않는다 -> 폴더와 상품_폴더의 관계는 따로 맺지 않음
@Table(name = "product_folder")
public class ProductFolder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "folder_id", nullable = false)
  private Folder folder;

  public ProductFolder(Product product, Folder folder) {
    this.product = product;
    this.folder = folder;
  }
}