package com.finalProject.ali.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // userDAO.xml의 table명과 일치
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id // PK (기본키)
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // MyBatis의 #{userId} 대응 (String 타입)

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String phone;
    private String birth;
    private String address;

    @Column(name = "profile_img")
    private String profileImg;

    // 가입 상태 (ACTIVE, BANNED 등) -> 기본값 'ACTIVE'
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 엔티티가 저장되기 전(PrePersist)에 실행
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
    }

    // 정보 수정용
    public void updateInfo(String email, String name, String phone, String address, String profileImg) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.profileImg = profileImg;
        this.updatedAt = LocalDateTime.now();
    }
}