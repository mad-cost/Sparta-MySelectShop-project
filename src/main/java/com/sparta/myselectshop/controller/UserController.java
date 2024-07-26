package com.sparta.myselectshop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.myselectshop.dto.SignupRequestDto;
import com.sparta.myselectshop.dto.UserInfoDto;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import com.sparta.myselectshop.service.KakaoService;
import com.sparta.myselectshop.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

  private final UserService userService;
  private final FolderService folderService;
  private final KakaoService kakaoService;

  @GetMapping("/user/login-page")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/user/signup")
  public String signupPage() {
    return "signup";
  }

  @PostMapping("/user/signup")
  public String signup(
          @Valid
          SignupRequestDto requestDto,
          BindingResult bindingResult // 유효성 검증 실패를 담는 객체
  ) {
    // Validation 예외처리
    List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    if (fieldErrors.size() > 0) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
      }
      return "redirect:/api/user/signup";
    }

    userService.signup(requestDto);

    return "redirect:/api/user/login-page";
  }

  // 회원 관련 정보 받기
  @GetMapping("/user-info")
  @ResponseBody
  public UserInfoDto getUserInfo(
          @AuthenticationPrincipal
          UserDetailsImpl userDetails
  ) {
    String username = userDetails.getUser().getUsername();
    UserRoleEnum role = userDetails.getUser().getRole();
    boolean isAdmin = (role == UserRoleEnum.ADMIN);

    return new UserInfoDto(username, isAdmin);
  }


  // 회원이 저장판 폴더 조회
  @GetMapping("/user-folder")
  public String getUserInfo(
          Model model,
          @AuthenticationPrincipal // 회원 정보 받아오기
          UserDetailsImpl userDetails
  ) {
    model.addAttribute("folders", folderService.getFolders(userDetails.getUser()));

    return "index :: #fragment";
  }

  // 카카오에서 애플리케이션 등록 시, Redirect URI로 설정한 인가코드를 받는 controller
  @GetMapping("/user/kakao/callback")
  public String kakaoLogin(
          @RequestParam // 카카오에서 보내는 인가코드를 @RequestParam으로 받는다
          String code, // 인증 코드
          // JWT를 생성하고 쿠키에 넣어서 클라이언트에 반환 하기 위해 사용
          HttpServletResponse
                  response
  ) throws JsonProcessingException {
    // Application에서 인증 코드로 KakaoServer에 토큰을 요청하고, KakaoServer에서 토큰을 전달 받는다.
    // 이후 Application에서 KakaoServer에 토큰으로 API를 호출하고, KakaoServer에서 토큰 유효성 검사를 한 후 응답을 전달 받는다

    // JWT 생성
    String token = kakaoService.kakaoLogin(code);

    // 쿠키 생성
    Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, token.substring(7));
    cookie.setPath("/"); // 메인 페이지를 Path로 넣어준다
    // JWT담은 cookie를 브라우저의 쿠키 스토리지에 넣어준다
    response.addCookie(cookie);

    return "redirect:/"; // 메인 페이지로 Redirect
  }


}