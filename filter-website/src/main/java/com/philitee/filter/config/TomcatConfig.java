package com.philitee.filter.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat 配置：允许 URL 中包含中文字符（用于中文文件名图片访问）
 */
@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "[]|{}^\\`\"<>");
            connector.setProperty("relaxedQueryChars", "[]|{}^\\`\"<>");
            if (connector.getProtocolHandler() instanceof Http11NioProtocol protocol) {
                protocol.setRelaxedPathChars("[]|{}^\\`\"<>");
                protocol.setRelaxedQueryChars("[]|{}^\\`\"<>");
            }
        });
    }
}
