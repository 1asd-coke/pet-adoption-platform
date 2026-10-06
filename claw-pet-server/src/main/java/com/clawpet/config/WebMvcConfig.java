package com.clawpet.config;

import com.clawpet.util.UploadPathResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web MVC 配置 - 静态资源映射，将上传目录映射为 /profile/** 访问路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:./upload}")
    private String uploadPath;

    private Path absoluteUploadPath;

    /**
     * 初始化：将相对路径解析为与「启动位置」无关的绝对路径
     */
    @PostConstruct
    public void init() {
        absoluteUploadPath = UploadPathResolver.resolveAndCreate(uploadPath);
        System.out.println("静态资源目录: " + absoluteUploadPath);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /profile/** 映射到上传目录的物理路径
        String dir = absoluteUploadPath.toString().replace('\\', '/');
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:" + dir);
    }
}
