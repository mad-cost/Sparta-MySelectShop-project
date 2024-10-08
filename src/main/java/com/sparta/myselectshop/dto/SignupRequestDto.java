package com.sparta.myselectshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
  @NotBlank(message = "이름 등록은 필수 항목 입니다.")
  private String username;
  @NotBlank
  private String password;
  @Email
  @NotBlank
  private String email;
  private boolean admin = false;
  private String adminToken = "";
}