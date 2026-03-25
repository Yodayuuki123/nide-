package com.philitee.filter.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片代理控制器
 * 本地开发时，将图片请求代理到远程服务器获取
 * 支持 HTTP→HTTPS 重定向、自签名证书
 * 当 upload.proxy-enabled=true 时激活
 */
@Controller
@ConditionalOnProperty(name = "upload.proxy-enabled", havingValue = "true", matchIfMissing = false)
public class ImageProxyController {

    @Value("${upload.proxy-remote-host:https://47.251.117.119}")
    private String remoteHost;

    // 简单内存缓存，避免重复请求远程服务器
    private final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();

    private SSLSocketFactory trustAllSslFactory;

    @PostConstruct
    public void init() throws Exception {
        // 信任所有证书（仅用于本地开发代理）
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] c, String a) {}
                    public void checkServerTrusted(X509Certificate[] c, String a) {}
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new java.security.SecureRandom());
        trustAllSslFactory = sc.getSocketFactory();
    }

    @GetMapping("/wp-content/uploads/**")
    public ResponseEntity<byte[]> proxyWpContent(HttpServletRequest request) {
        return proxyImage(request.getRequestURI());
    }

    @GetMapping("/wordpress/wp-content/uploads/**")
    public ResponseEntity<byte[]> proxyWordpressContent(HttpServletRequest request) {
        return proxyImage(request.getRequestURI());
    }

    private ResponseEntity<byte[]> proxyImage(String path) {
        try {
            // 检查缓存
            CachedImage cached = imageCache.get(path);
            if (cached != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(cached.contentType));
                headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
                return new ResponseEntity<>(cached.data, headers, HttpStatus.OK);
            }

            // 直接用 HTTPS 请求远程服务器
            String url = remoteHost + path;
            byte[] data = fetchWithRedirect(url, 3);

            if (data == null || data.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // 根据扩展名判断类型
            String contentType = guessContentType(path);

            // 缓存结果（最多缓存2000张）
            if (imageCache.size() < 2000) {
                imageCache.put(path, new CachedImage(data, contentType));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private byte[] fetchWithRedirect(String urlStr, int maxRedirects) throws Exception {
        for (int i = 0; i < maxRedirects; i++) {
            URL url = new URL(urlStr);
            HttpURLConnection conn;

            if (urlStr.startsWith("https")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setSSLSocketFactory(trustAllSslFactory);
                httpsConn.setHostnameVerifier((h, s) -> true);
                conn = httpsConn;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();

            if (code == 301 || code == 302 || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location == null) break;
                // 处理相对路径
                if (location.startsWith("/")) {
                    URL base = new URL(urlStr);
                    location = base.getProtocol() + "://" + base.getHost() + location;
                }
                urlStr = location;
                continue;
            }

            if (code == 200) {
                InputStream in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                in.close();
                conn.disconnect();
                return out.toByteArray();
            }

            conn.disconnect();
            break;
        }
        return null;
    }

    private String guessContentType(String path) {
        path = path.toLowerCase();
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".bmp")) return "image/bmp";
        if (path.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    private static class CachedImage {
        final byte[] data;
        final String contentType;
        CachedImage(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }
    }
}
