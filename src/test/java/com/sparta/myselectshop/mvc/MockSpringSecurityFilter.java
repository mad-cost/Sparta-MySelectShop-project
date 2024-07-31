//package com.sparta.myselectshop.mvc;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.io.IOException;
//
//public class MockSpringSecurityFilter implements Filter {
//  @Override
//  public void init(FilterConfig filterConfig) {}
//
//  // Security가 동작을 하면, 테스트하는데 방해가 되기 때문에 MockSecurity를 사용
//  @Override
//  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//    SecurityContextHolder.getContext()
//            .setAuthentication((Authentication) ((HttpServletRequest) req).getUserPrincipal());
//    chain.doFilter(req, res);
//  }
//
//  @Override
//  public void destroy() {
//    SecurityContextHolder.clearContext();
//  }
//}