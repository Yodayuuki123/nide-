package com.philitee.filter.config;

import com.philitee.filter.entity.AdminUser;
import com.philitee.filter.service.AdminUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登录成功处理器：更新最后登录时间
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminUserService adminUserService;

    public LoginSuccessHandler(@Lazy AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        AdminUser user = adminUserService.getUserByUsername(username);
        if (user != null) {
            adminUserService.updateLastLoginTime(user.getId());
        }

        setDefaultTargetUrl("/admin/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
