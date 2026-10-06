package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.util.UploadPathResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传控制器 - 上传宠物图片和用户头像，含三级安全校验
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:./upload}")
    private String uploadPath;

    private String absoluteUploadPath;

    // ========== 三级校验常量 ==========

    /** 允许的后缀名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    /** 允许的 MIME Type */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    /** 魔数签名: 格式 -> 可能的文件头字节列表 */
    private static final Map<String, List<byte[]>> MAGIC_SIGNATURES = new LinkedHashMap<>();
    static {
        MAGIC_SIGNATURES.put("jpg",  List.of(new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}));
        MAGIC_SIGNATURES.put("jpeg", List.of(new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}));
        MAGIC_SIGNATURES.put("png",  List.of(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47}));
        MAGIC_SIGNATURES.put("gif",  List.of(new byte[]{0x47, 0x49, 0x46, 0x38}));
        MAGIC_SIGNATURES.put("webp", List.of(new byte[]{0x52, 0x49, 0x46, 0x46}));
        MAGIC_SIGNATURES.put("bmp",  List.of(new byte[]{0x42, 0x4D}));
        // HEIC/HEIF (iPhone 照片)
        MAGIC_SIGNATURES.put("heic", List.of(
                new byte[]{0, 0, 0, 0x20, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63}));
        MAGIC_SIGNATURES.put("heif", List.of(
                new byte[]{0, 0, 0, 0x20, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63}));
    }

    /** 根据魔数反推实际格式 */
    private static final Map<String, String> MAGIC_TO_EXT = new LinkedHashMap<>();
    static {
        MAGIC_TO_EXT.put("jpg", "jpg");
        MAGIC_TO_EXT.put("png", "png");
        MAGIC_TO_EXT.put("gif", "gif");
        MAGIC_TO_EXT.put("webp", "webp");
        MAGIC_TO_EXT.put("bmp", "bmp");
        MAGIC_TO_EXT.put("heic", "heic");
        MAGIC_TO_EXT.put("heif", "heic");
    }

    /** 初始化上传目录的绝对路径 */
    @PostConstruct
    public void init() {
        absoluteUploadPath = UploadPathResolver.resolveAndCreate(uploadPath).toString();
        System.out.println("上传目录: " + absoluteUploadPath);
    }

    /** 上传宠物图片，返回可访问的 URL */
    @PostMapping("/pet-image")
    public Result<String> uploadPetImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, absoluteUploadPath + "/pet/", "/profile/pet/");
    }

    /** 上传用户头像，返回可访问的 URL */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, absoluteUploadPath + "/avatar/", "/profile/avatar/");
    }

    // ======================== 核心上传方法 ========================

    private Result<String> uploadFile(MultipartFile file, String dir, String urlPrefix) {
        if (file.isEmpty()) {
            return Result.badRequest("文件为空");
        }

        // ——— 第一级：扩展名校验 ———
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.badRequest("不支持的文件格式，仅支持 JPG/PNG/GIF/WEBP/BMP");
        }

        // ——— 第二级：MIME Type 校验 ———
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return Result.badRequest("文件类型不合法，仅允许上传图片文件");
        }

        // ——— 第三级：Magic Number（文件头魔数）校验 ———
        try {
            Result<String> magicResult = validateMagicNumber(file, ext);
            if (magicResult != null) return magicResult;
        } catch (IOException e) {
            return Result.error(500, "文件读取失败, 无法完成安全校验");
        }

        // ——— 通过校验，保存文件 ———
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String suffix = "." + ext;
        if (ext.isEmpty() && originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + suffix;

        try {
            file.transferTo(new File(uploadDir, filename));
            String url = urlPrefix + filename;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 第三级：Magic Number 校验
     * 读取文件头前 8 字节，与实际扩展名对应的魔数比对
     */
    private Result<String> validateMagicNumber(MultipartFile file, String ext) throws IOException {
        List<byte[]> signatures = MAGIC_SIGNATURES.get(ext);
        if (signatures == null || signatures.isEmpty()) {
            return Result.badRequest("无法识别的文件格式");
        }

        byte[] fileHeader;
        try (InputStream is = file.getInputStream()) {
            fileHeader = is.readNBytes(8);
        }

        boolean match = false;
        for (byte[] sig : signatures) {
            if (fileHeader.length >= sig.length) {
                boolean sigMatch = true;
                for (int i = 0; i < sig.length; i++) {
                    if (fileHeader[i] != sig[i]) {
                        sigMatch = false;
                        break;
                    }
                }
                if (sigMatch) {
                    match = true;
                    break;
                }
            }
        }

        if (!match) {
            // 实际检测一下文件魔数，告诉用户真实格式
            String realType = detectRealType(fileHeader);
            if (realType != null && !realType.equalsIgnoreCase(ext)) {
                return Result.badRequest("文件实际是 " + realType.toUpperCase() + " 格式，但扩展名是 " + ext + "。请将文件另存为 " + realType + " 格式后再上传");
            }
            return Result.badRequest("文件格式无法识别，请确认是 JPG/PNG/GIF/WEBP/BMP 图片");
        }
        return null;
    }

    /** 通过文件头反推真实格式 */
    private String detectRealType(byte[] header) {
        if (header == null || header.length < 3) return null;
        for (Map.Entry<String, String> e : MAGIC_TO_EXT.entrySet()) {
            List<byte[]> sigs = MAGIC_SIGNATURES.get(e.getKey());
            if (sigs == null) continue;
            for (byte[] sig : sigs) {
                if (header.length < sig.length) continue;
                boolean match = true;
                for (int i = 0; i < sig.length; i++) {
                    if (header[i] != sig[i]) { match = false; break; }
                }
                if (match) return e.getValue();
            }
        }
        return null;
    }
}
