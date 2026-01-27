package com.finalProject.ali.user.service;


import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String userId = "";
        String name = "";
        String email = "";

        if ("google".equals(registrationId)) {
            userId = "google_" + attributes.get("sub");
            name = (String) attributes.get("name");
            email = (String) attributes.get("email");
        } else if ("kakao".equals(registrationId)) {
            // 1. 고유 ID값 추출 (kakao_12345678)
            userId = "kakao_" + attributes.get("id");
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            name = (String) properties.get("nickname");
            email = userId + "@kakao.com";
        }else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            userId = "naver_" + response.get("id");
            name = (String) response.get("name");
            email = (String) response.get("email");
        }

        // DB 저장 및 세션 처리 로직 (기존과 동일)
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) {
            user = new UserDTO();
            user.setUserId(userId);
            user.setName(name);
            user.setEmail(email);
            user.setPassword("OAUTH_USER");
            userDAO.insertUser(user);
            try {
                userDAO.insertUserRole(user.getUserId(), "ROLE_USER");
            } catch (Exception e) {
                // 혹시 모를 중복 에러 방지
                e.printStackTrace();
            }
        }
        session.setAttribute("loginUser", user);

        List<SimpleGrantedAuthority> authorities;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            authorities = user.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // 권한이 없으면 기본 권한 부여 (안전장치)
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // OAuth2User 객체를 생성할 때 DB 권한을 넣어서 반환
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                userNameAttributeName
        );

    }
}