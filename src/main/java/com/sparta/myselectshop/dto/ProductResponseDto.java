package com.sparta.myselectshop.dto;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.ProductFolder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductResponseDto {
  private Long id;
  private String title;
  private String link;
  private String image;
  private int lprice;
  private int myprice;

  // 관심 상품 하나에 여러개의 폴더가 추가될 수 있다
  private List<FolderResponseDto> productFolderList = new ArrayList<>();

  public ProductResponseDto(Product product) {
    this.id = product.getId();
    this.title = product.getTitle();
    this.link = product.getLink();
    this.image = product.getImage();
    this.lprice = product.getLprice();
    this.myprice = product.getMyprice();
    // product.getProductFolderList(): product에 연결한 중간 테이블을 가져온다
    // product객체의 productFolderList은 @OneToMany이므로 default가 LAZY이다
    for (ProductFolder productFolder : product.getProductFolderList()) {
      productFolderList.add(new FolderResponseDto(productFolder.getFolder()));
    }
  }
}