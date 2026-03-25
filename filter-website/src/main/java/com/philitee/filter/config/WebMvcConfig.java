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

    /**
     * 配置静态资源映射
     * 将 /wp-content/uploads/** 请求映射到服务器上 WordPress 的物理目录
     * 支持两个图片来源目录：
     *   1. /opt/website/wordpress/wp-content/uploads  (原WordPress目录)
     *   2. /root/static_web/filter.philitee.com/wp-content/uploads (新独立站目录)
     * 实现图片文件原地不动，新系统直接读取
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // WordPress 图片目录映射（30天缓存）
        // 同时搜索两个目录，优先查找 uploadBasePath，其次查找 staticWebPath
        if (staticWebPath != null && !staticWebPath.isEmpty()) {
            registry.addResourceHandler("/wp-content/uploads/**")
                    .addResourceLocations(
                            "file:" + uploadBasePath + "/",
                            "file:" + staticWebPath + "/"
                    )
                    .setCachePeriod(2592000);
        } else {
            registry.addResourceHandler("/wp-content/uploads/**")
                    .addResourceLocations("file:" + uploadBasePath + "/")
                    .setCachePeriod(2592000);
        }

        // 兼容旧URL中带 /wordpress/ 前缀的图片路径
        // 如：/wordpress/wp-content/uploads/2024/08/xxx.jpg
        registry.addResourceHandler("/wordpress/wp-content/uploads/**")
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
