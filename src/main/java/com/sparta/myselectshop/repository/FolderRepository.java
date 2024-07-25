package com.sparta.myselectshop.repository;

import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

  /* findAllByUserAndNameIn에 대한 동적 쿼리
     select * from folder where user_id = ? and name in (?, ?, ?)
     -- ex) 1번 유저의 폴더 1, 2, 3 --
     select * from folder where user_id = 1 and name in ('1', '2', '3')
   */
  List<Folder> findAllByUserAndNameIn(User user, List<String> folderNames);

  // 유저에 해당하는 폴더 전부 가져오기
  List<Folder> findAllByUser(User user);
}
