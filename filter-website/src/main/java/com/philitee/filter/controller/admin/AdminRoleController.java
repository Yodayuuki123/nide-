package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        QueryWrapper<AdminRole> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("display_name", keyword).or().like("description", keyword));
        }
        wrapper.orderByDesc("COALESCE(updated_at, created_at)");
        IPage<AdminRole> rolePage = adminRoleService.page(new Page<>(page, size), wrapper);
        rolePage.getRecords().forEach(role -> {
            AdminRole detail = adminRoleService.getRoleDetail(role.getId());
            if (detail != null) role.setPermissions(detail.getPermissions());
        });
        model.addAttribute("rolePage", rolePage);
        return "admin/role-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("role", new AdminRole());
        Map<String, List<AdminPermission>> permissionMap = adminRoleService.getPermissionsByModule();
        model.addAttribute("permissionMap", permissionMap);
        return "admin/role-form";
    }

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

    @PostMapping("/save")
    public String save(AdminRole role,
                       @RequestParam(required = false) List<Long> permissionIds,
                       RedirectAttributes redirectAttributes) {
        try {
            if (role.getId() == null) {
                adminRoleService.saveRole(role, permissionIds);
                redirectAttributes.addFlashAttribute("message", "Role created successfully");
            } else {
                adminRoleService.updateRole(role, permissionIds);
                redirectAttributes.addFlashAttribute("message", "Role updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminRoleService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "Role deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}