package com.philitee.filter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 * 前台页面完全开放，后台 /admin/** 需要登录
 * 支持基于数据库的用户认证和角色/权限控制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 前台页面完全开放
                        .requestMatchers("/", "/products/**", "/categories/**", "/tags/**",
                                "/about", "/factory", "/contact", "/search", "/news/**", "/solutions",
                                "/sitemap.xml", "/robots.txt").permitAll()
                        // 静态资源开放
                        .requestMatchers("/static/**", "/wp-content/uploads/**",
                                "/wordpress/wp-content/uploads/**",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // 语言切换开放
                        .requestMatchers("/lang/**").permitAll()
                        // 用户管理需要 ADMIN 角色
                        .requestMatchers("/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/admin/roles/**").hasRole("ADMIN")
                        // 网站设置需要设置权限
                        .requestMatchers("/admin/settings/**").hasAnyAuthority("setting:view", "setting:edit")
                        // 后台其他页面需要认证
                        .requestMatchers("/admin/**").authenticated()
                        // 其他请求开放
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .permitAll()
                )
                // 完全禁用CSRF - 前台B2B展示站无需CSRF保护
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
