package com.philitee.filter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 * 前台页面完全开放，后台 /admin/** 需要登录
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 前台页面完全开放
                        .requestMatchers("/", "/products/**", "/categories/**", "/tags/**",
                                "/about", "/factory", "/contact", "/search",
                                "/sitemap.xml", "/robots.txt").permitAll()
                        // 静态资源开放
                        .requestMatchers("/static/**", "/wp-content/uploads/**",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // 语言切换开放
                        .requestMatchers("/lang/**").permitAll()
                        // 后台需要认证
                        .requestMatchers("/admin/**").authenticated()
                        // 其他请求开放
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        // 后台API关闭CSRF（方便AJAX请求）
                        .ignoringRequestMatchers("/admin/api/**")
                );

        return http.build();
    }

}
