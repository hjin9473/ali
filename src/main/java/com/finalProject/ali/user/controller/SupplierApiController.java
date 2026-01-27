package com.finalProject.ali.user.controller;


import com.finalProject.ali.user.service.BizApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/supplier/api")
public class SupplierApiController {

    @Autowired
    private BizApiService bizApiService;

    @PostMapping("/verify-biz")
    public ResponseEntity<Map<String, String>> verifyBiz(@RequestParam("cpNumber") String cpNumber, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        // 1. 실제 API를 통한 진위 확인
        boolean isValid = bizApiService.verifyBusinessNumber(cpNumber);

        if (isValid) {
            // 2. 인증번호 시뮬레이션 (세션 저장)
            String authCode = String.valueOf((int)(Math.random() * 899999) + 100000);
            session.setAttribute("bizAuthCode", authCode);
            session.setAttribute("bizCpNumber", cpNumber);

            response.put("status", "success");
            response.put("tempCode", authCode); // 테스트용으로 코드 반환 (원래는 숨겨야 함)
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/confirm-code")
    public ResponseEntity<String> confirmCode(@RequestParam("code") String code, HttpSession session) {
        // 1. 세션에 저장해뒀던 인증번호 가져오기
        String savedCode = (String) session.getAttribute("bizAuthCode");

        // 로그를 찍어서 서버에서 어떤 값을 비교하는지 확인해볼 수 있습니다.
        System.out.println("사용자 입력 코드: " + code);
        System.out.println("세션 저장 코드: " + savedCode);

        // 2. 비교 로직
        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공 시 세션에 마킹
            session.setAttribute("isBizVerified", true);
            return ResponseEntity.ok("success");
        } else {
            // 인증 실패
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }
}

