package com.finalProject.ali.admin.controller;

import com.finalProject.ali.admin.dto.AdminDashboardDTO;
import com.finalProject.ali.admin.dto.UserSearchDTO;
import com.finalProject.ali.repository.QnaRepository;
import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private QnaRepository qnaRepository;

    // 대시보드 페이지 (통계 + 차트 통합)
    @GetMapping("/adminpage")
    public String adminpage(Model model) {

        // 1. 상단 통계 카드 데이터 (dashboard)
        AdminDashboardDTO dashboard = AdminDashboardDTO.builder()
                .totalUsers(userDAO.countAllUsers())
                .todayUsers(userDAO.countTodayUsers())
                .pendingSuppliers(userDAO.countPendingSuppliers())
                .waitingQna(qnaRepository.findByStatusOrderByCreatedAtDesc("WAITING").size())
                .build();

        // 2. [차트 기능] 최근 7일 가입자 통계 (chartLabels, chartData)
        List<Map<String, Object>> stats = userDAO.getDailySignupStats();

        // 날짜 리스트 변환 (예: "['01-01', '01-02']")
        String chartLabels = stats.stream()
                .map(m -> "'" + m.get("date").toString() + "'")
                .collect(Collectors.joining(", ", "[", "]"));

        // 숫자 리스트 변환 (예: "[5, 10]")
        String chartData = stats.stream()
                .map(m -> m.get("count").toString())
                .collect(Collectors.joining(", ", "[", "]"));

        // 3. 화면으로 데이터 전송 (변수명 HTML과 일치)
        model.addAttribute("dashboard", dashboard);     // 숫자 통계
        model.addAttribute("chartLabels", chartLabels); // 차트 날짜
        model.addAttribute("chartData", chartData);     // 차트 수치
        model.addAttribute("pageTitle", "관리자 대시보드");

        return "admin/adminpage";
    }

    @GetMapping("/supplier/list")
    public String supplierList(Model model) {
        List<SupplierDTO> pendingList = userService.getPendingSuppliers();
        model.addAttribute("suppliers", pendingList);
        model.addAttribute("activeMenu", "suppliers");
        return "admin/supplierList";
    }

    @PostMapping("/supplier/approve")
    public String approveSupplier(@RequestParam("supplierId") String supplierId,
                                  @RequestParam("userId") String userId) {
        userService.approveSupplier(supplierId, userId);
        return "redirect:/admin/supplier/list";
    }

    @PostMapping("/supplier/reject")
    public String rejectSupplier(@RequestParam("supplierId") String supplierId,
                                 @RequestParam("memo") String memo) {
        userService.updateSupplierStatus(supplierId, "REJECTED", memo);
        return "redirect:/admin/supplier/list";
    }

    @GetMapping("/users")
    public String userList(@ModelAttribute UserSearchDTO searchDTO, Model model) {
        List<UserDTO> users = userService.getUsersWithPaging(searchDTO);
        int totalCount = userService.getUsersCount(searchDTO);

        int totalPages = (int) Math.ceil((double) totalCount / searchDTO.getSize());
        int startPage = Math.max(1, searchDTO.getPage() - 4);
        int endPage = Math.min(totalPages, startPage + 9);
        if (endPage == 0) endPage = 1;

        model.addAttribute("users", users);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("activeMenu", "users");

        return "admin/userList";
    }

    @PostMapping("/users/status")
    public String updateUserStatus(@RequestParam("userId") String userId,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "reason", required = false) String reason) {
        userService.updateUserStatus(userId, status, reason);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/role")
    public String updateUserRole(@RequestParam("userId") String userId,
                                 @RequestParam("role") String role) {
        userService.setAuthority(userId, role);
        return "redirect:/admin/users";
    }
}