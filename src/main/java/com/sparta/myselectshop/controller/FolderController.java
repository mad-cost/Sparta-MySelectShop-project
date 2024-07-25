package com.sparta.myselectshop.controller;


import com.sparta.myselectshop.dto.FolderRequestDto;
import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import lombok.RequiredArgsConstructor;
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

  // 폴더 전체 조호;
  @GetMapping("/folders")
  public List<FolderResponseDto> getFolders(
          @AuthenticationPrincipal
          UserDetailsImpl userDetails
  ){
    return folderService.getFolders(userDetails.getUser());
  }



}
