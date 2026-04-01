package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.entity.AdminUser;
import com.philitee.filter.service.AdminRoleService;
import com.philitee.filter.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        QueryWrapper<AdminUser> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword).or().like("real_name", keyword).or().like("email", keyword));
        }
        wrapper.orderByDesc("COALESCE(updated_at, created_at)");
        IPage<AdminUser> userPage = adminUserService.page(new Page<>(page, size), wrapper);
        userPage.getRecords().forEach(user -> {
            AdminUser detail = adminUserService.getUserDetail(user.getId());
            if (detail != null) user.setRoles(detail.getRoles());
        });
        model.addAttribute("userPage", userPage);
        return "admin/user-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("user", new AdminUser());
        model.addAttribute("roles", adminRoleService.list());
        return "admin/user-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        AdminUser user = adminUserService.getUserDetail(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", adminRoleService.list());
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String save(AdminUser user,
                       @RequestParam(required = false) List<Long> roleIds,
                       RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null) {
                adminUserService.createUser(user, roleIds);
                redirectAttributes.addFlashAttribute("message", "User created successfully");
            } else {
                adminUserService.updateUser(user, roleIds);
                redirectAttributes.addFlashAttribute("message", "User updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            AdminUser user = adminUserService.getById(id);
            if (user != null && "admin".equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete the built-in admin account");
                return "redirect:/admin/users";
            }
            adminUserService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            AdminUser user = adminUserService.getById(id);
            if (user != null) {
                user.setStatus(user.getStatus() == 1 ? 0 : 1);
                adminUserService.updateById(user);
                redirectAttributes.addFlashAttribute("message", user.getStatus() == 1 ? "User enabled successfully" : "User disabled successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}