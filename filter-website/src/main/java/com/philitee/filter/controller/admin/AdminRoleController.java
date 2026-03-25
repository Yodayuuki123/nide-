package com.philitee.filter.controller.admin;

import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * 后台角色管理 Controller
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 角色列表
     */
    @GetMapping
    public String list(Model model) {
        List<AdminRole> roles = adminRoleService.getAllRolesWithPermissions();
        model.addAttribute("roles", roles);
        return "admin/role-list";
    }

    /**
     * 新增角色页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("role", new AdminRole());
        Map<String, List<AdminPermission>> permissionMap = adminRoleService.getPermissionsByModule();
        model.addAttribute("permissionMap", permissionMap);
        return "admin/role-form";
    }

    /**
     * 编辑角色页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        AdminRole role = adminRoleService.getRoleDetail(id);
        if (role == null) {
            return "redirect:/admin/roles";
        }
        model.addAttribute("role", role);
        Map<String, List<AdminPermission>> permissionMap = adminRoleService.getPermissionsByModule();
        model.addAttribute("permissionMap", permissionMap);
        return "admin/role-form";
    }

    /**
     * 保存角色
     */
    @PostMapping("/save")
    public String save(
            AdminRole role,
            @RequestParam(required = false) List<Long> permissionIds,
            RedirectAttributes redirectAttributes) {
        try {
            if (role.getId() == null) {
                adminRoleService.saveRole(role, permissionIds);
                redirectAttributes.addFlashAttribute("message", "角色创建成功");
            } else {
                adminRoleService.updateRole(role, permissionIds);
                redirectAttributes.addFlashAttribute("message", "角色更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    /**
     * 删除角色
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminRoleService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "角色删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}
