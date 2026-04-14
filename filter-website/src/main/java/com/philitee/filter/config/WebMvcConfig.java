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
 * 1. 静态资源映射（WordPress图片目录 - 双目录支持）
 * 2. 国际化配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.base-path}")
    private String uploadBasePath;

    @Value("${upload.static-web-path:}")
    private String staticWebPath;

    @Value("${upload.legacy-wordpress-path:}")
    private String legacyWordpressPath;

    @Value("${upload.proxy-enabled:false}")
    private boolean proxyEnabled;

    /**
     * 配置静态资源映射
     * proxy模式下不注册 /wp-content/uploads/** 的本地映射，
     * 全部交给 ImageProxyController 从远程服务器获取图片。
     * 非proxy模式（部署到服务器后）走本地文件映射。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!proxyEnabled) {
            // 服务器部署模式：本地文件映射，按“当前上传目录 -> 静态导出目录 -> 旧 WordPress 目录”顺序回退
            if (staticWebPath != null && !staticWebPath.isEmpty() && legacyWordpressPath != null && !legacyWordpressPath.isEmpty()) {
                registry.addResourceHandler("/wp-content/uploads/**")
                        .addResourceLocations(
                                "file:" + uploadBasePath + "/",
                                "file:" + staticWebPath + "/",
                                "file:" + legacyWordpressPath + "/"
                        )
                        .setCachePeriod(2592000);
            } else if (staticWebPath != null && !staticWebPath.isEmpty()) {
                registry.addResourceHandler("/wp-content/uploads/**")
                        .addResourceLocations(
                                "file:" + uploadBasePath + "/",
                                "file:" + staticWebPath + "/"
                        )
                        .setCachePeriod(2592000);
            } else if (legacyWordpressPath != null && !legacyWordpressPath.isEmpty()) {
                registry.addResourceHandler("/wp-content/uploads/**")
                        .addResourceLocations(
                                "file:" + uploadBasePath + "/",
                                "file:" + legacyWordpressPath + "/"
                        )
                        .setCachePeriod(2592000);
            } else {
                registry.addResourceHandler("/wp-content/uploads/**")
                        .addResourceLocations("file:" + uploadBasePath + "/")
                        .setCachePeriod(2592000);
            }

            if (legacyWordpressPath != null && !legacyWordpressPath.isEmpty()) {
                registry.addResourceHandler("/wordpress/wp-content/uploads/**")
                        .addResourceLocations(
                                "file:" + legacyWordpressPath + "/",
                                "file:" + uploadBasePath + "/"
                        )
                        .setCachePeriod(2592000);
            } else {
                registry.addResourceHandler("/wordpress/wp-content/uploads/**")
                        .addResourceLocations("file:" + uploadBasePath + "/")
                        .setCachePeriod(2592000);
            }
        }
        // proxy模式下：/wp-content/uploads/** 由 ImageProxyController 处理

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
