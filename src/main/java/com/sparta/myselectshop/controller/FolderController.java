package com.sparta.myselectshop.controller;


import com.sparta.myselectshop.dto.FolderRequestDto;
import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.exception.RestApiException;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FolderController {
  private final FolderService folderService;

  // 회원에 입력한 폴더 저장
  @PostMapping("/folders")
  public void addFolders(
          @RequestBody // Body 부분에 폴더의 이름이 여러개 들어 온다
          FolderRequestDto folderRequestDto,
          @AuthenticationPrincipal // 유저 정보
          UserDetailsImpl userDetails
  ){
    List<String> folderNames = folderRequestDto.getFolderNames();
    // 받아온 폴더의 중복을 검사하고, 중복하지 않은 폴더 저장하기
    folderService.addFolder(folderNames, userDetails.getUser());
  }

  
  // 폴더 전체 조회
  @GetMapping("/folders")
  public List<FolderResponseDto> getFolders(
          @AuthenticationPrincipal
          UserDetailsImpl userDetails
  ){
    return folderService.getFolders(userDetails.getUser());
  }

  // @ExceptionHandler: 컨트롤러에서 예외가 발생시 처리하기 위해 제공,
  // AOP를 이용한 예외 처리와 같다 -> 메서드마다 try-catch를 사용할 필요가 없다.
  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<RestApiException> handleException(IllegalArgumentException ex) {
    System.out.println("FolderController.handleException");
    RestApiException restApiException = new RestApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    // F12 / 네트워크 / Response 에서 확인이 가능하다
    return new ResponseEntity<>(
            // HTTP body
            restApiException,
            // HTTP status code
            HttpStatus.BAD_REQUEST
    );
  }

}
