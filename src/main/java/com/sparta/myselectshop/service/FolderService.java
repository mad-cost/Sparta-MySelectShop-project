package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
  private final FolderRepository folderRepository;


  // 받아온 폴더의 중복을 검사하고, 중복하지 않은 폴더 저장하기
  public void addFolder(List<String> folderNames, User user) {

    // 입력한 유저에 해당하는 입력한 폴더 모두 찾기
    List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);

    List<Folder> folderList= new ArrayList<>();

    // 중복 되지 않은 폴더 등록 하기
    for (String folderName : folderNames){
      // 받아온 폴더(folderNames)와 찾아온 폴더(existFolderList)가 같은지 검사
      if (!isExistFolderName(folderName, existFolderList)){
        // 중복되지 않은 폴더라면 저장
        Folder folder = new Folder(folderName, user);
        folderList.add(folder);
      } else{
        throw new IllegalArgumentException("폴더명이 중복 되었습니다.");
      }
    }

    folderRepository.saveAll(folderList);
  }

  // 받아온 폴더(folderNames)와 찾아온 폴더(existFolderList)가 같은지 검사
  private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
    for (Folder existFolder : existFolderList){
      if (folderName.equals(existFolder.getName())){
        return true;
      }
    }
    return false;
  }

  public List<FolderResponseDto> getFolders(User user) {
    List<Folder> folderList = folderRepository.findAllByUser(user);
    List<FolderResponseDto> responseDtoList = new ArrayList<>();

    for (Folder folder : folderList){
      responseDtoList.add(new FolderResponseDto(folder));
    }

    return responseDtoList;
  }
}
