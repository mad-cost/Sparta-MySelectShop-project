package com.sparta.myselectshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "folder")
public class Folder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  // 지연 로딩: 폴더를 조회 할 때마다 매 번 유저 정보가 필요하지는 않다
  // 회원과 폴더의 관계 / 다대일 단방향
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Folder(String name, User user) {
    this.name = name;
    this.user = user;
  }
}