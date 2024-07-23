package com.sparta.myselectshop.scheduler;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.naver.service.NaverApiService;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Scheduler")
@Component
@RequiredArgsConstructor
// SpringApplication에 @EnableScheduling 등록 해주기
public class Scheduler {

  private final NaverApiService naverApiService;
  private final ProductService productService;
  private final ProductRepository productRepository;

  // @Scheduled(cron): 지정한 특정 시간에 메시드 자동 실행
  // 초(0-59), 분(0-59), 시(0-23), 일(1-31), 월(1-12), 요일(0-7 / 0과7은 일요일) 순서
  @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시에 해당 메서드 자동 실행
  //@Scheduled(cron = "*/10 * * * * *") // 메서드 확인을 위해 10초 후 실행
  public void updatePrice() throws InterruptedException {
    log.info("가격 업데이트 실행");
    List<Product> productList = productRepository.findAll();
    // DB에서 가져온 상품을 하나씩 for문에 돌려준다
    for (Product product : productList) {
      // (Naver 제한으로 인한)1초에 한 상품 씩 조회합니다
      TimeUnit.SECONDS.sleep(1); // 1초간 지연

      // DB에서 가져온 i 번째 관심 상품의 제목으로 검색을 실행합니다.
      String title = product.getTitle();

      List<ItemDto> itemDtoList = naverApiService.searchItems(title);

      // 찾는 데이터가 하나라도 있다면
      if (itemDtoList.size() > 0) {
        // 찾아온 데이터의 가장 상단에 있는 0번째 데이터를 가져온다 (내가 찾는 데이터와 가장 일치하는 데이터)
        ItemDto itemDto = itemDtoList.get(0);
        // i 번째 관심 상품 정보를 업데이트합니다.
        Long id = product.getId();
        /* try-catch: 혹시 오류가 발생할 경우 로그만 남기고 계속 진행
           try-catch를 사용하면, for 루프 내에서 예외가 발생하더라도 루프는 계속 진행된다
           이렇게 하면 프로그램의 흐름이 중단되지 않고 예외가 발생한 상황에 대한 로그만 기록된다 */
        try {
          // DB의 기존의 데이터를 최신 가격으로 업데이트
          productService.updateBySearch(id, itemDto);
        } catch (Exception e) {
          log.error(id + " : " + e.getMessage());
        }
      }
    }

  }

}