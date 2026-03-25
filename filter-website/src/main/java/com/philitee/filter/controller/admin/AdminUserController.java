package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.entity.AdminUser;
import com.philitee.filter.service.AdminRoleService;
import com.philitee.filter.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 后台用户管理 Controller
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;

    /**
     * 用户列表
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        IPage<AdminUser> userPage = adminUserService.getUserPage(page, size);
        model.addAttribute("userPage", userPage);
        return "admin/user-list";
    }

    /**
     * 新增用户页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("user", new AdminUser());
        model.addAttribute("roles", adminRoleService.list());
        return "admin/user-form";
    }

    /**
     * 编辑用户页面
     */
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

    /**
     * 保存用户（新增或更新）
     */
    @PostMapping("/save")
    public String save(
            AdminUser user,
            @RequestParam(required = false) List<Long> roleIds,
            RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null) {
                adminUserService.createUser(user, roleIds);
                redirectAttributes.addFlashAttribute("message", "用户创建成功");
            } else {
                adminUserService.updateUser(user, roleIds);
                redirectAttributes.addFlashAttribute("message", "用户更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 防止删除自己
            AdminUser user = adminUserService.getById(id);
            if (user != null && "admin".equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "不能删除超级管理员账号");
                return "redirect:/admin/users";
            }
            adminUserService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "用户删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            AdminUser user = adminUserService.getById(id);
            if (user != null) {
                user.setStatus(user.getStatus() == 1 ? 0 : 1);
                adminUserService.updateById(user);
                redirectAttributes.addFlashAttribute("message",
                        user.getStatus() == 1 ? "用户已启用" : "用户已禁用");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
