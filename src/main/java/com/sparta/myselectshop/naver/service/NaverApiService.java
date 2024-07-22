package com.sparta.myselectshop.naver.service;


import com.sparta.myselectshop.naver.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "NAVER API")
@Service
public class NaverApiService {

  // RestTemplate: 원격 HTTP 서비스를 호출하는 데 사용
  private final RestTemplate restTemplate;

  public NaverApiService(RestTemplateBuilder builder) {
    this.restTemplate = builder.build();
  }

  public List<ItemDto> searchItems(String query) {
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
            .fromUriString("https://openapi.naver.com")
            .path("/v1/search/shop.json")
            .queryParam("display", 15) // 15개의 데이터를 받는다
            .queryParam("query", query)
            .encode()
            .build()
            .toUri();
    log.info("uri = " + uri);

    RequestEntity<Void> requestEntity = RequestEntity
            .get(uri)
            .header("X-Naver-Client-Id", "TlgwWYXWL8TqHoar0OrE")
            .header("X-Naver-Client-Secret", "6ZfbtBIm6h")
            .build();

    ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

    log.info("NAVER API Status Code : " + responseEntity.getStatusCode());

    // Naver에서 받은 데이터를 ItemDto로 변환 후 반환
    return fromJSONtoItems(responseEntity.getBody());
  }


  public List<ItemDto> fromJSONtoItems(String responseEntity) {
    JSONObject jsonObject = new JSONObject(responseEntity);
    JSONArray items  = jsonObject.getJSONArray("items");
    List<ItemDto> itemDtoList = new ArrayList<>();

    for (Object item : items) {
      ItemDto itemDto = new ItemDto((JSONObject) item);
      itemDtoList.add(itemDto);
    }

    return itemDtoList;
  }
}