package com.finalProject.ali.common.security;

import com.finalProject.ali.user.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class UserSecurity {
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/doLogin_dummy")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )


                .authorizeHttpRequests(auth -> auth
                        // 1. 정적 리소스 (CSS, JS, 이미지, 폰트 등) 전면 허용
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/upload/**", "/favicon.ico", "/error", // 기본
                                "/**/*.css",   // 모든 폴더의 css 파일 허용
                                "/**/*.js",    // 모든 폴더의 js 파일 허용
                                "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", "/**/*.svg",
                                "/**/*.html"

                        ).permitAll()

                        // 2. 누구나 접근 가능한 페이지
                        .requestMatchers(
                                "/", "/index","/products/**",
                                "/user/login", "/user/register", "/user/signup",
                                "/user/find_id", "/user/reset_pw**",
                                "/user/send**", "/user/verify**",
                                "/v3/api-docs/**","/swagger-ui/**", "/swagger-ui.html",
                                "/api/**"
                        ).permitAll()

                        .requestMatchers("/supplier/api/**").authenticated()

                        // 3. 관리자 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. 판매자 전용
                        .requestMatchers("/supplier/**").hasAnyRole("SUPPLIER", "ADMIN")

                        // 5. 나머지 요청은 로그인 필수
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/user/login?expired=true")
                        .sessionRegistry(sessionRegistry())
                );

        return http.build();
    }
}