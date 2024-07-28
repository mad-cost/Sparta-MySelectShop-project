package com.sparta.myselectshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// TestCode에서 사용
@Configuration // 아래 설정을 등록하여 활성화 합니다.
@EnableJpaAuditing // 시간 자동 변경이 가능하도록 합니다.
public class JpaConfig {
}