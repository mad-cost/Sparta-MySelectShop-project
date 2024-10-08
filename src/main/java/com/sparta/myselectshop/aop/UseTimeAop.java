package com.sparta.myselectshop.aop;

import com.sparta.myselectshop.entity.ApiUseTime;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.ApiUseTimeRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j(topic = "UseTimeAop")
@Aspect // 빈(Bean) 클래스에만 적용 가능.
@Component
// SpringAOP 사용하기
public class UseTimeAop {

  private final ApiUseTimeRepository apiUseTimeRepository;

  // 생성자 주입
  public UseTimeAop(ApiUseTimeRepository apiUseTimeRepository) {
    this.apiUseTimeRepository = apiUseTimeRepository;
  }

  // 포인트 컷: 어디에 적용할 껀지
  @Pointcut("execution(* com.sparta.myselectshop.controller.ProductController.*(..))")
  private void product() {}
  @Pointcut("execution(* com.sparta.myselectshop.controller.FolderController.*(..))")
  private void folder() {}
  @Pointcut("execution(* com.sparta.myselectshop.naver.controller.NaverApiController.*(..))")
  private void naver() {}

  // @Around: 핵심기능' 수행 전과 후 (@Before + @After)
  @Around("product() || folder() || naver()") // 해당 컨트롤러의 메서드에 해당할 경우 실행
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
    // 측정 시작 시간
    long startTime = System.currentTimeMillis();

    try {
      // 핵심기능 수행
      // joinPoint.proceed(): 컨트롤러에 원래 호출하려고 했던 함수, 인수(argument) 가 전달 된다. (노션 "AOP 적용 후" 그림을 보면 이해가 쉽다)
      Object output = joinPoint.proceed();
      return output;
    } finally {
      // 측정 종료 시간
      long endTime = System.currentTimeMillis();
      // 수행시간 = 종료 시간 - 시작 시간
      long runTime = endTime - startTime;

      // 로그인 회원이 없는 경우, 수행시간 기록하지 않음
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal().getClass() == UserDetailsImpl.class) {
        // 로그인 회원 정보
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User loginUser = userDetails.getUser();

        // API 사용시간 및 DB 에 기록
        ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser).orElse(null);
        if (apiUseTime == null) {
          // 로그인 회원의 기록이 없으면
          apiUseTime = new ApiUseTime(loginUser, runTime);
        } else {
          // 로그인 회원의 기록이 이미 있으면
          apiUseTime.addUseTime(runTime);
        }

        log.info("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
        apiUseTimeRepository.save(apiUseTime);
      }
    }
  }
}