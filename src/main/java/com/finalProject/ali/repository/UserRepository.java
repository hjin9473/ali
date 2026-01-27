package com.finalProject.ali.repository;

import com.finalProject.ali.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // 1. 회원가입/수정 (save)
    // 2. 아이디로 조회 (findById)
    // 3. 커스텀 조회: 이름과 이메일로 회원 찾기 (아이디 찾기 기능용)
    // SQL: select * from users where name = ? and email = ?
    Optional<User> findByNameAndEmail(String name, String email);

    // 4. 아이디 중복 체크 (이미 존재하는지?)
    boolean existsByUserId(String userId);
}