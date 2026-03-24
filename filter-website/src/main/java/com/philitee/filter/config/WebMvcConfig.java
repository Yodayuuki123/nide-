package com.philitee.filter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Web MVC 配置类
 * 1. 静态资源映射（WordPress图片目录）
 * 2. 国际化配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.base-path}")
    private String uploadBasePath;

    /**
     * 配置静态资源映射
     * 将 /wp-content/uploads/** 请求映射到服务器上 WordPress 的物理目录
     * 实现图片文件原地不动，新系统直接读取
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // WordPress 图片目录映射（30天缓存）
        registry.addResourceHandler("/wp-content/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(2592000);

        // 静态资源（CSS/JS/Images）（30天缓存）
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(2592000);
    }

    /**
     * 国际化 - 语言解析器
     * 使用Cookie存储用户语言偏好，默认英文
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(3600 * 24 * 30); // 30天
        return resolver;
    }

    /**
     * 国际化 - 语言切换拦截器
     * 通过 ?lang=en 或 ?lang=zh_CN 切换语言
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}
