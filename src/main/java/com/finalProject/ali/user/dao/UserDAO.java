package com.finalProject.ali.user.dao;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDAO {
    void insertUser(UserDTO userDTO); // 회원가입

    UserDTO findByUserId(String userId);

    // 회원 정보 수정
    void updateUser(UserDTO userDTO);
    void updateSupplier(SupplierDTO supplierDTO);

    // 비밀번호 변경
    int updatePassword(Map<String, String> params);

    // 구매자/판매자 찾기
    SupplierDTO findSupplierByUserId(String userId);
    void insertSupplier(SupplierDTO supplierDTO);

    String findIdByPhone(@Param("name") String name, @Param("phone") String phone);
    String findIdByEmail(@Param("name") String name, @Param("email") String email);

    // 만료되지 않은 토큰이 있는지 확인하고 해당 유저의 ID를 가져옴
    String getUserIdByToken(@Param("token") String token, @Param("now") java.time.LocalDateTime now);

    // 비밀번호 변경 후 토큰 즉시삭제
    void deleteResetToken(@Param("token") String token);

    // 판매자 승인
    List<SupplierDTO> findPendingSuppliers();
    void updateUserRole(@Param("userId") String userId, @Param("role") String role);
    void updateSupplierStatus(@Param("supplierId") String supplierId,
                              @Param("status") String status,
                              @Param("memo") String memo);


    // 전체 회원 리스트 조회
    List<UserDTO> findAllUsers();
    // 계정 상태 변경
    void updateUserStatus(@Param("userId") String userId,
                          @Param("status") String status,
                          @Param("reason") String reason);

    // 권한 추가 (예: 회원가입 시 ROLE_USER 부여)
    void insertUserRole(@Param("userId") String userId, @Param("roleName") String roleName);

    // 권한 삭제 (예: 판매자 권한 박탈)
    void deleteUserRole(@Param("userId") String userId, @Param("roleName") String roleName);

    String findNameByUserId(@Param("userId") String userId);

    // 판매자 정보 삭제 (권한 해제 시)
    void deleteSupplier(String userId);

    // 대시보드 통계용
    int countAllUsers();          // 전체 회원 수
    int countTodayUsers();        // 오늘 가입 수
    int countPendingSuppliers();

    // [관리자] 필터 및 페이징 적용 리스트
    List<UserDTO> findAllUsersWithPaging(com.finalProject.ali.admin.dto.UserSearchDTO searchDTO);

    // [관리자] 필터 적용 전체 개수
    int countUsersWithPaging(com.finalProject.ali.admin.dto.UserSearchDTO searchDTO);

    // [관리자] 대시보드 차트용 일별 통계
    List<Map<String, Object>> getDailySignupStats();

    String findProfileImgByUserId(@Param("userId") String userId);
}