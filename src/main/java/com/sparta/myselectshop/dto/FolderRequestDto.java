package com.sparta.myselectshop.dto;

import lombok.Getter;

import java.util.List;

@Getter
// Body 부분에 폴더의 이름이 여러개 들어 온다
public class FolderRequestDto {
  List<String> folderNames;
}