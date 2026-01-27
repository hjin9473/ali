package com.finalProject.ali.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qna")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호 자동 증가 (Auto Increment)
    private Long qnaId;

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 내용 (긴 글)

    @Column(nullable = false)
    private String writerId; // 작성자 ID (User 테이블의 ID만 저장)

    // --- 관리자 답변 영역 ---
    @Column(columnDefinition = "TEXT")
    private String answer; // 관리자 답변 내용

    private LocalDateTime answeredAt; // 답변 달린 시간

    // 답변 상태 (WAITING: 대기중, COMPLETED: 답변완료)
    @Builder.Default
    private String status = "WAITING";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 관리자 답변 등록용 메소드 (Setter 대신 사용)
    public void registerAnswer(String answer) {
        this.answer = answer;
        this.answeredAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }
}