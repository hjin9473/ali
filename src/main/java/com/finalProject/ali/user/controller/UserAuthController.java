package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.dto.LoginRequestDTO;
import com.finalProject.ali.user.dto.SignupRequestDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.EmailService;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserAuthController {

    @Autowired private UserService userService;
    @Autowired private EmailService emailService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private SessionRegistry sessionRegistry;

    // 페이지 이동
    @GetMapping("/") public String indexPage() { return "index/index"; }
    @GetMapping("/register") public String registerPage() { return "user/register"; }
    @GetMapping("/login") public String loginPage() { return "user/login"; }

    // [회원가입] DTO 변경 적용
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<String> signup(@RequestBody SignupRequestDTO signupRequest, HttpSession session) {
        Boolean isVerified = (Boolean) session.getAttribute("isEmailVerified");
        String authEmail = (String) session.getAttribute("authEmail");

        if (isVerified == null || !isVerified || !signupRequest.getEmail().equals(authEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증이 필요합니다.");
        }

        // RequestDTO -> UserDTO 변환 후 서비스 호출
        userService.register(signupRequest.toUserDTO());

        session.removeAttribute("emailAuthCode");
        session.removeAttribute("isEmailVerified");
        session.removeAttribute("authEmail");

        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest, HttpSession session) {
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        // 정지 상태 확인
        UserDTO checkUser = userService.findByUserId(userId);
        if (checkUser != null && "SUSPENDED".equals(checkUser.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("정지된 계정입니다. 사유: " + checkUser.getSuspensionReason());
        }

        try {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userId, password);
            Authentication authentication = authenticationManager.authenticate(authRequest);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            sessionRegistry.registerNewSession(session.getId(), authentication.getPrincipal());
            session.setAttribute("loginUser", checkUser);

            Map<String, String> response = new java.util.HashMap<>();
            response.put("status", "success");

            // 역할 확인 로직
            String mainRole = "ROLE_USER";
            if (checkUser.getRoles() != null && !checkUser.getRoles().isEmpty()) {
                if (checkUser.getRoles().contains("ROLE_ADMIN")) mainRole = "ROLE_ADMIN";
                else if (checkUser.getRoles().contains("ROLE_SUPPLIER")) mainRole = "ROLE_SUPPLIER";
                else mainRole = checkUser.getRoles().get(0);
            }
            response.put("role", mainRole);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 이메일 인증 관련
    @PostMapping("/send-auth-code")
    @ResponseBody
    public ResponseEntity<String> sendAuthCode(@RequestParam("email") String email, HttpSession session) {
        try {
            String authCode = emailService.sendVerificationEmail(email);
            session.setAttribute("emailAuthCode", authCode);
            session.setAttribute("authEmail", email);
            session.setAttribute("isEmailVerified", false);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    @PostMapping("/verify-auth-code")
    @ResponseBody
    public ResponseEntity<String> verifyAuthCode(@RequestParam("code") String code, HttpSession session) {
        String serverCode = (String) session.getAttribute("emailAuthCode");
        if (serverCode != null && serverCode.equals(code)) {
            session.setAttribute("isEmailVerified", true);
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }
}