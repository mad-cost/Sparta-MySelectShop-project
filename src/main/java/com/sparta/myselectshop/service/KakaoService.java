package com.sparta.myselectshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.myselectshop.dto.KakaoUserInfoDto;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate;
  private final JwtUtil jwtUtil;

  public String kakaoLogin(String code) throws JsonProcessingException {
    // 1. kakaoServer에서 받은 "인가 코드"로 kakaoServer에 "액세스 토큰" 요청
    String accessToken = getToken(code);

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

    // 3. 필요시에 회원가입
    User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);

    // 4. JWT 토큰 반환
    String createToken = jwtUtil.createToken(kakaoUser.getUsername(), kakaoUser.getRole());

    return createToken;
  }

  private String getToken(String code) throws JsonProcessingException {
    log.info("인가 코드" + code);
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
            .fromUriString("https://kauth.kakao.com")
            .path("/oauth/token")
            .encode()
            .build()
            .toUri();

    // HTTP Header 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    // HTTP Body 생성
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    // body.add("client_id", "본인의 REST API키");
    body.add("client_id", "c7bf5dd927e784667f23ea0b0a203e87");
    body.add("redirect_uri", "http://localhost:8080/api/user/kakao/callback");
    body.add("code", code); // kakaoServer에서 받아온 인가 코드

    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(uri)
            .headers(headers)
            .body(body);

    // HTTP 요청 보내고, String 타입으로 받아오기
    ResponseEntity<String> response = restTemplate.exchange(
            requestEntity,
            String.class
    );

    // HTTP 응답 (JSON) -> 액세스 토큰 파싱 / response.getBody()에 엑세스 토큰 값이 들어 있다.
    JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
    // 순수 토큰값 가져오기 / .asText()를 사용하면 String 타입으로 받아오게 된다
    return jsonNode.get("access_token").asText();
  }

  // 사용자 정보 요청 메서드
  private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
    log.info("accessToken" + accessToken);
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
            .fromUriString("https://kapi.kakao.com")
            .path("/v2/user/me")
            .encode()
            .build()
            .toUri();

    // HTTP Header 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(uri)
            .headers(headers)
            .body(new LinkedMultiValueMap<>());

    // HTTP 요청 보내고, String 타입으로 받아오기
    ResponseEntity<String> response = restTemplate.exchange(
            requestEntity,
            String.class
    );

    JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
    Long id = jsonNode.get("id").asLong();
    String nickname = jsonNode.get("properties") // nickname은 properties에 nickname에 들어 있다
            .get("nickname").asText();
    String email = jsonNode.get("kakao_account") // email은 kakao_account에 들어있다
            .get("email").asText();

    log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
    return new KakaoUserInfoDto(id, nickname, email);
  }

  // 필요시에 회원가입
  private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
    // DB 에 중복된 Kakao Id 가 있는지 확인
    Long kakaoId = kakaoUserInfo.getId();
    User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null); // 값이 없을경우 null 반환

    // kakaoUser가 존재한다면 이미 로그인 했던 사용자이다.
    if (kakaoUser == null) {
      // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
      String kakaoEmail = kakaoUserInfo.getEmail();
      User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
      if (sameEmailUser != null) {
        kakaoUser = sameEmailUser;
        /* 이 경우 기존의 폼 방식으로 회원 가입 했을때의 이메일과, 소셜 로그인으로 로그인한 이메일이 같을 경우,
           폼 방식 회원가입 정보에 카카오 id만 추가해 준다 */
        // 기존 회원정보에 카카오 Id 추가
        kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
      } else {
        // 신규 사용자 소셜 로그인 회원 가입
        // password: random UUID
        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        // email: kakao email
        String email = kakaoUserInfo.getEmail();

        kakaoUser = new User(kakaoUserInfo.getNickname(), encodedPassword, email, UserRoleEnum.USER, kakaoId);
      }

      userRepository.save(kakaoUser);
    }
    return kakaoUser;
  }

}