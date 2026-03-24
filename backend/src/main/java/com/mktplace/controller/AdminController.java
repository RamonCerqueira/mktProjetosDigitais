package com.mktplace.controller;

import com.mktplace.dto.AdminDtos.*;
import com.mktplace.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse overview() { return adminService.overview(); }

    @GetMapping("/users")
    public List<AdminUserSummary> users(@RequestParam(required = false) String role, @RequestParam(required = false) String status, @RequestParam(required = false) String subscription) {
        return adminService.users(role, status, subscription);
    }

    @GetMapping("/users/{id}")
    public AdminUserDetail userDetail(@PathVariable Long id) { return adminService.userDetail(id); }

    @PostMapping("/users/{id}/block")
    public AdminUserSummary block(@PathVariable Long id) { return adminService.blockUser(id); }

    @PostMapping("/users/{id}/unblock")
    public AdminUserSummary unblock(@PathVariable Long id) { return adminService.unblockUser(id); }

    @PostMapping("/users/{id}/roles")
    public AdminUserSummary roles(@PathVariable Long id, @RequestBody UpdateRolesRequest request) { return adminService.updateRoles(id, request); }

    @PostMapping("/users/{id}/cancel")
    public AdminUserSummary cancel(@PathVariable Long id) { return adminService.cancelAccount(id); }

    @GetMapping("/projects")
    public List<AdminProjectResponse> projects(@RequestParam(required = false) String status) { return adminService.projects(status); }

    @PostMapping("/projects/{id}/moderate")
    public AdminProjectResponse moderate(@PathVariable Long id, @RequestBody ProjectModerationRequest request) { return adminService.moderateProject(id, request); }

    @DeleteMapping("/projects/{id}")
    public void hideSuspicious(@PathVariable Long id) { adminService.removeSuspiciousProject(id); }

    @GetMapping("/transactions")
    public List<AdminTransactionResponse> transactions(@RequestParam(required = false) String status) { return adminService.transactions(status); }

    @GetMapping("/audit-logs")
    public List<AuditEntry> audit(@RequestParam(defaultValue = "100") int limit) { return adminService.auditLogs(limit); }
}
