package com.sparta.myselectshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "api_use_time")
// 사용시간 누적
public class ApiUseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne // 외래키 + 단방향
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Long totalTime;

  public ApiUseTime(User user, Long totalTime) {
    this.user = user;
    this.totalTime = totalTime;
  }

  public void addUseTime(long useTime) {
    this.totalTime += useTime;
  }
}