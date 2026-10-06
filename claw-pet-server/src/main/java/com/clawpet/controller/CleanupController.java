package com.clawpet.controller;

import com.clawpet.common.Result;
import com.clawpet.task.FileCleanupTask;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 清理/重置控制器（仅管理员）- 数据清理、缓存管理、演示数据重置
 */
@RestController
@RequestMapping("/api/admin/cleanup")
public class CleanupController {

    private final FileCleanupTask fileCleanupTask;
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    public CleanupController(FileCleanupTask fileCleanupTask, DataSource dataSource, RedisTemplate<String, Object> redisTemplate) {
        this.fileCleanupTask = fileCleanupTask;
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 手动触发孤儿文件清理
     *
     * @param dryRun true = 只列出将要删除的文件，不真正删除（推荐先跑一次预演再执行）
     */
    @PostMapping("/orphans")
    public Result<Map<String, Object>> manualCleanup(
            @RequestParam(value = "dryRun", required = false, defaultValue = "false") boolean dryRun) {
        FileCleanupTask.CleanupResult result = fileCleanupTask.cleanupNow(dryRun);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dryRun", dryRun);
        data.put("deletedCount", result.deletedCount);
        data.put("freedBytes", result.freedBytes);
        data.put("freedDisplay", formatBytes(result.freedBytes));
        data.put("costMs", result.costMs);
        // 受影响文件清单：预演时用于人工确认，正式清理时作为审计记录
        data.put("files", result.files);
        String msg;
        if (result.skipped) {
            msg = "未执行删除：清理功能被禁用，或未查到任何被引用的图片（请检查数据库连接）";
        } else if (dryRun) {
            msg = "预演完成，共 " + result.deletedCount + " 个文件将被删除";
        } else {
            msg = "清理完成，已删除 " + result.deletedCount + " 个文件";
        }
        return Result.success(msg, data);
    }

    /**
     * 查看文件存储统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        FileCleanupTask.CleanupStats stats = fileCleanupTask.getStats();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalFiles", stats.totalFiles);
        data.put("totalSize", formatBytes(stats.totalBytes));
        data.put("orphanCount", stats.orphanCount);
        data.put("petSize", formatBytes(stats.petBytes));
        data.put("petOrphanCount", stats.petOrphanCount);
        data.put("avatarSize", formatBytes(stats.avatarBytes));
        data.put("avatarOrphanCount", stats.avatarOrphanCount);
        return Result.success(data);
    }

    /** 将字节数格式化为人类可读的容量单位 */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return new DecimalFormat("#.##").format(bytes / 1024.0) + " KB";
        return new DecimalFormat("#.##").format(bytes / (1024.0 * 1024)) + " MB";
    }

    /**
     * 统一重置：清空业务数据 + 宠物数据，重新插入 23 只演示宠物
     */
    @PostMapping("/reset-all")
    public Result<String> resetAll() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 清空业务表
            String[] bizTables = {"pet_comment", "pet_favorite", "adopt_application",
                                  "adopt_record", "followup_record", "notification",
                                  "user_security_question", "pet_category", "adoption_story"};
            for (String t : bizTables) {
                stmt.execute("DELETE FROM " + t);
                stmt.execute("ALTER TABLE " + t + " AUTO_INCREMENT = 1");
            }
            // 清空宠物表
            stmt.execute("DELETE FROM pet_image");
            stmt.execute("DELETE FROM pet_info");
            stmt.execute("ALTER TABLE pet_info AUTO_INCREMENT = 1");
            stmt.execute("ALTER TABLE pet_image AUTO_INCREMENT = 1");
            // 清除除了 admin 和 user 以外的所有用户
            stmt.execute("DELETE FROM user_security_question WHERE user_id > 2");
            stmt.execute("DELETE FROM users WHERE id > 2");
            stmt.execute("ALTER TABLE users AUTO_INCREMENT = 3");
            stmt.execute("ALTER TABLE user_security_question AUTO_INCREMENT = 1");
        } catch (Exception e) {
            return Result.error(500, "重置失败: " + e.getMessage());
        }
        clearCaches();
        return Result.success("已清空所有数据");
    }

    /** 从 SQL 文件导入演示数据（白名单：分类 / 宠物 / 宠物图片 / 收容所图片） */
    @PostMapping("/reload-demo")
    public Result<String> reloadDemo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "请选择 SQL 文件");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] stmts = sql.split(";");
            int count = 0;
            for (String s : stmts) {
                String clean = s.replaceAll("(?m)^--.*", "").trim();
                if (clean.isEmpty()) continue;
                String upper = clean.toUpperCase();
                    // 只放行演示数据需要的四类语句，其它一律忽略（不做过滤就是给 .sql 文件开后门）
                    boolean allowed =
                        upper.startsWith("INSERT INTO PET_CATEGORY") ||
                        upper.startsWith("INSERT INTO PET_INFO") ||
                        // 少了这一条，"重置数据 → 加载演示数据"之后 15 只宠物全都没有图片
                        upper.startsWith("INSERT INTO PET_IMAGE") ||
                        upper.matches("(?s)^UPDATE\\s+SHELTER_INFO\\s+SET\\s+IMAGE\\s*=.*");
                    if (allowed) {
                    stmt.execute(clean);
                    count++;
                }
            }
            clearCaches();
            return Result.success("已导入 " + count + " 条 SQL 语句");
        } catch (Exception e) {
            return Result.error(500, "导入失败: " + e.getMessage());
        }
    }

    /**
     * 重置业务数据：清空评论/收藏/申请/通知/回访/密保，保留宠物数据
     */
    @PostMapping("/reset-business")
    public Result<Map<String, Object>> resetBusiness() {
        long start = System.currentTimeMillis();
        int total = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String[] tables = {"pet_comment", "pet_favorite", "adopt_application",
                               "adopt_record", "followup_record", "notification",
                               "user_security_question"};
            for (String t : tables) {
                stmt.execute("DELETE FROM " + t);
                total += stmt.getUpdateCount();
            }
        } catch (Exception e) {
            return Result.error(500, "重置失败: " + e.getMessage());
        }
        clearCaches();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deleted", total);
        data.put("costMs", System.currentTimeMillis() - start);
        return Result.success("业务数据已清空", data);
    }

    /** 清除所有 Redis 缓存 */
    private void clearCaches() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }

    /**
     * 重置演示数据（清空业务数据，重新插入初始宠物/图片）
     */
    @PostMapping("/reset-demo")
    public Result<Map<String, Object>> resetDemo() {
        long start = System.currentTimeMillis();
        int totalStatements = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 读取 classpath 上的 03-reset-demo.sql
            try (InputStream is = getClass().getResourceAsStream("/sql/03-reset-demo.sql")) {
                if (is == null) {
                    return Result.error(500, "重置脚本未找到");
                }
                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                // 按分号分割多语句执行
                String[] statements = sql.split(";\n");
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                    stmt.execute(trimmed);
                    totalStatements++;
                }
            }
        } catch (Exception e) {
            return Result.error(500, "重置失败: " + e.getMessage());
        }
        long cost = System.currentTimeMillis() - start;

        // 清除所有相关缓存（关键！否则前后台数据不一致）
        Set<String> cacheKeys = redisTemplate.keys("statsDashboard::*");
        if (cacheKeys != null) redisTemplate.delete(cacheKeys);
        cacheKeys = redisTemplate.keys("statsHome::*");
        if (cacheKeys != null) redisTemplate.delete(cacheKeys);
        cacheKeys = redisTemplate.keys("petList::*");
        if (cacheKeys != null) redisTemplate.delete(cacheKeys);
        cacheKeys = redisTemplate.keys("petDetail::*");
        if (cacheKeys != null) redisTemplate.delete(cacheKeys);
        cacheKeys = redisTemplate.keys("categoryList::*");
        if (cacheKeys != null) redisTemplate.delete(cacheKeys);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("executed", totalStatements);
        data.put("costMs", cost);
        return Result.success("演示数据重置完成", data);
    }
}
