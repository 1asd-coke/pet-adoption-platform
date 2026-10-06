package com.clawpet.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clawpet.entity.FollowupRecord;
import com.clawpet.entity.PetImage;
import com.clawpet.entity.ShelterInfo;
import com.clawpet.entity.User;
import com.clawpet.mapper.FollowupRecordMapper;
import com.clawpet.mapper.PetImageMapper;
import com.clawpet.mapper.ShelterInfoMapper;
import com.clawpet.mapper.UserMapper;
import com.clawpet.util.UploadPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件清理定时任务 - 定期清理数据库中已不引用的孤儿图片文件
 *
 * <p><b>安全约定</b>：凡是数据库里出现过的 /profile/ 路径，一律视为"被引用"，绝不删除。
 * 新增任何存图片路径的列时，必须同步加到 {@link #collectReferencedUrls()} 里，
 * 否则该列引用的图片会在宽限期后被静默删除（历史上 shelter_info.image 就踩过这个坑）。</p>
 */
@Component
public class FileCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupTask.class);

    /** 从任意文本（含 JSON 数组、逗号分隔）中抽出 /profile/ 开头的图片路径 */
    private static final Pattern PROFILE_PATH = Pattern.compile("/profile/[\\w\\-./]+");

    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;
    private final ShelterInfoMapper shelterInfoMapper;
    private final FollowupRecordMapper followupRecordMapper;

    @Value("${upload.path:./upload}")
    private String uploadPath;

    @Value("${cleanup.orphan-grace-period-days:7}")
    private long gracePeriodDays;

    @Value("${cleanup.enabled:true}")
    private boolean enabled;

    public FileCleanupTask(PetImageMapper petImageMapper,
                           UserMapper userMapper,
                           ShelterInfoMapper shelterInfoMapper,
                           FollowupRecordMapper followupRecordMapper) {
        this.petImageMapper = petImageMapper;
        this.userMapper = userMapper;
        this.shelterInfoMapper = shelterInfoMapper;
        this.followupRecordMapper = followupRecordMapper;
    }

    /** 上传目录的绝对路径，与启动时的工作目录无关（避免扫错目录把图片全删了） */
    private File uploadDir() {
        return UploadPathResolver.resolve(uploadPath).toFile();
    }

    /**
     * 定时清理：默认每天凌晨 0 点执行（时间由 cleanup.cron 配置）
     */
    @Scheduled(cron = "${cleanup.cron:0 0 0 * * ?}")
    public void scheduledCleanup() {
        if (!enabled) {
            log.info("[文件清理] 已禁用，跳过本次清理");
            return;
        }
        log.info("[文件清理] 开始定时清理孤儿文件...");
        CleanupResult result = doCleanup(false);
        log.info("[文件清理] 完成。删除 {} 个孤儿文件，释放 {} KB，耗时 {} ms",
                result.deletedCount, result.freedBytes / 1024, result.costMs);
    }

    /**
     * 手动触发清理（由 Controller 调用）
     *
     * @param dryRun true = 只列出将要删除的文件，不真正删除
     */
    public CleanupResult cleanupNow(boolean dryRun) {
        if (!enabled) {
            log.warn("[文件清理] 已禁用，手动触发无效");
            return new CleanupResult(0, 0, 0, Collections.emptyList(), true);
        }
        log.info("[文件清理] 手动触发清理{}...", dryRun ? "（预演，不删除）" : "");
        CleanupResult result = doCleanup(dryRun);
        log.info("[文件清理] 手动清理{}完成。{} {} 个文件，{} {} KB",
                dryRun ? "预演" : "", dryRun ? "将删除" : "删除",
                result.deletedCount, dryRun ? "预计释放" : "释放", result.freedBytes / 1024);
        return result;
    }

    /** 统计当前孤儿文件情况 */
    public CleanupStats getStats() {
        File petDir = new File(uploadDir(), "pet");
        File avatarDir = new File(uploadDir(), "avatar");

        Set<String> referenced = collectReferencedUrls();

        long orphanPetFiles = countOrphans(petDir, referenced);
        long orphanAvatarFiles = countOrphans(avatarDir, referenced);

        long totalPetSize = getDirSize(petDir);
        long totalAvatarSize = getDirSize(avatarDir);

        return new CleanupStats(
                orphanPetFiles + orphanAvatarFiles,
                countFiles(petDir) + countFiles(avatarDir),
                totalPetSize + totalAvatarSize,
                orphanPetFiles, orphanAvatarFiles,
                totalPetSize, totalAvatarSize
        );
    }

    // ========== 内部实现 ==========

    private CleanupResult doCleanup(boolean dryRun) {
        long start = System.currentTimeMillis();

        File petDir = new File(uploadDir(), "pet");
        File avatarDir = new File(uploadDir(), "avatar");

        Set<String> referenced = collectReferencedUrls();

        // 安全阀：一条引用都查不到，说明大概率是连错了库/表被清空了。
        // 此时删文件是不可逆的灾难，宁可不删（代价只是清理不到，可以人工处理）。
        // 预演模式不删任何文件，因此不中止，如实列出"会删什么"即可。
        if (referenced.isEmpty()) {
            if (!dryRun) {
                log.error("[文件清理] 数据库未返回任何被引用的图片路径，已中止本次清理以防误删。"
                        + "请确认 DB_URL 指向正确的库、pet_image/users/shelter_info 有数据。目录：{} / {}",
                        petDir.getAbsolutePath(), avatarDir.getAbsolutePath());
                return new CleanupResult(0, 0, System.currentTimeMillis() - start,
                        Collections.emptyList(), true);
            }
            log.warn("[文件清理] 预演：数据库未返回任何被引用的图片路径，下方清单按「全部都是孤儿」计算，请谨慎对待。");
        }

        List<String> removed = new ArrayList<>();
        long freed = 0;

        OrphanResult petResult = processOrphans(petDir, referenced, gracePeriodDays, dryRun);
        removed.addAll(petResult.names);
        freed += petResult.bytes;

        OrphanResult avatarResult = processOrphans(avatarDir, referenced, gracePeriodDays, dryRun);
        removed.addAll(avatarResult.names);
        freed += avatarResult.bytes;

        long cost = System.currentTimeMillis() - start;
        return new CleanupResult(removed.size(), freed, cost, removed, false);
    }

    /**
     * 收集所有"被数据库引用"的图片路径。
     *
     * <p>注意：这里返回的是**并集**，不分 pet/avatar 目录。因为比对时拼的是
     * {@code /profile/<目录名>/<文件名>}，跨目录的 URL 不可能误匹配，
     * 而并集在语义上更安全（宁可多保护、不可误删）。</p>
     */
    private Set<String> collectReferencedUrls() {
        Set<String> urls = new LinkedHashSet<>();

        // 1. pet_image.url —— 宠物相册
        petImageMapper.selectList(new QueryWrapper<PetImage>().select("url")).stream()
                .map(PetImage::getUrl)
                .filter(Objects::nonNull)
                .forEach(urls::add);

        // 2. users.avatar —— 用户头像
        userMapper.selectList(new QueryWrapper<User>().select("avatar")
                        .isNotNull("avatar").ne("avatar", "")).stream()
                .map(User::getAvatar)
                .filter(Objects::nonNull)
                .forEach(urls::add);

        // 3. shelter_info.image —— 收容所图片（首页 hero 图用的就是它）
        shelterInfoMapper.selectList(new QueryWrapper<ShelterInfo>().select("image")).stream()
                .map(ShelterInfo::getImage)
                .filter(Objects::nonNull)
                .forEach(urls::add);

        // 4. followup_record.images —— 回访图片，格式不固定（JSON 数组 / 逗号分隔），统一用正则抽
        followupRecordMapper.selectList(new QueryWrapper<FollowupRecord>().select("images")).stream()
                .map(FollowupRecord::getImages)
                .filter(Objects::nonNull)
                .forEach(raw -> {
                    Matcher m = PROFILE_PATH.matcher(raw);
                    while (m.find()) {
                        urls.add(m.group());
                    }
                });

        log.debug("[文件清理] 数据库中受保护的图片路径共 {} 条", urls.size());
        return urls;
    }

    /**
     * 遍历目录，删除不在 referencedUrls 中且超过宽限期的文件
     *
     * @param dryRun true = 只统计不删除
     */
    private OrphanResult processOrphans(File dir, Set<String> referencedUrls, long graceDays, boolean dryRun) {
        OrphanResult result = new OrphanResult();
        if (!dir.isDirectory()) return result;

        long cutoff = System.currentTimeMillis() - graceDays * 24L * 3600 * 1000;

        File[] files = dir.listFiles();
        if (files == null) return result;

        for (File file : files) {
            // 跳过目录与说明文件
            if (file.isDirectory()
                    || file.getName().equals(".gitkeep")
                    || file.getName().equals("README.txt")) {
                continue;
            }
            // 文件在数据库中被引用 → 跳过
            String relativeUrl = "/profile/" + dir.getName() + "/" + file.getName();
            if (referencedUrls.contains(relativeUrl)) {
                continue;
            }
            // 刚上传不久的文件，给宽限期（防止并发写入时短暂不一致）
            if (file.lastModified() > cutoff) {
                continue;
            }

            if (dryRun) {
                result.names.add(relativeUrl);
                result.bytes += file.length();
                continue;
            }

            long size = file.length();
            if (file.delete()) {
                result.names.add(relativeUrl);
                result.bytes += size;
                log.debug("[文件清理] 已删除孤儿文件: {} ({} bytes)", file.getAbsolutePath(), size);
            } else {
                log.warn("[文件清理] 无法删除文件: {}", file.getAbsolutePath());
            }
        }
        return result;
    }

    /** 统计孤儿文件数量（不删除，不受宽限期影响） */
    private long countOrphans(File dir, Set<String> referencedUrls) {
        if (!dir.isDirectory()) return 0;
        return java.util.Arrays.stream(Optional.ofNullable(dir.listFiles()).orElse(new File[0]))
                .filter(f -> !f.isDirectory())
                .filter(f -> !f.getName().equals(".gitkeep"))
                .filter(f -> !f.getName().equals("README.txt"))
                .filter(f -> !referencedUrls.contains("/profile/" + dir.getName() + "/" + f.getName()))
                .count();
    }

    /** 计算目录下所有文件的总大小 */
    private long getDirSize(File dir) {
        if (!dir.isDirectory()) return 0;
        return java.util.Arrays.stream(Optional.ofNullable(dir.listFiles()).orElse(new File[0]))
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum();
    }

    /** 统计目录下的文件数量（排除隐藏文件） */
    private long countFiles(File dir) {
        if (!dir.isDirectory()) return 0;
        return java.util.Arrays.stream(Optional.ofNullable(dir.listFiles()).orElse(new File[0]))
                .filter(f -> f.isFile() && !f.getName().startsWith("."))
                .count();
    }

    // ========== 结果记录类 ==========

    /** 清理结果：删除数量、释放空间、耗时、受影响文件清单 */
    public static class CleanupResult {
        public final int deletedCount;
        public final long freedBytes;
        public final long costMs;
        /** 被删除（dryRun 时为"将被删除"）的文件相对路径 */
        public final List<String> files;
        /** true 表示本次未真正执行删除（预演，或安全阀中止） */
        public final boolean skipped;

        public CleanupResult(int deletedCount, long freedBytes, long costMs,
                             List<String> files, boolean skipped) {
            this.deletedCount = deletedCount;
            this.freedBytes = freedBytes;
            this.costMs = costMs;
            this.files = files == null ? Collections.emptyList() : files;
            this.skipped = skipped;
        }
    }

    /** 单个目录的扫描结果 */
    private static class OrphanResult {
        final List<String> names = new ArrayList<>();
        long bytes = 0;
    }

    /** 文件存储统计：总数、总大小、孤儿数量 */
    public static class CleanupStats {
        public final long orphanCount;
        public final long totalFiles;
        public final long totalBytes;
        public final long petOrphanCount;
        public final long avatarOrphanCount;
        public final long petBytes;
        public final long avatarBytes;

        public CleanupStats(long orphanCount, long totalFiles, long totalBytes,
                            long petOrphanCount, long avatarOrphanCount,
                            long petBytes, long avatarBytes) {
            this.orphanCount = orphanCount;
            this.totalFiles = totalFiles;
            this.totalBytes = totalBytes;
            this.petOrphanCount = petOrphanCount;
            this.avatarOrphanCount = avatarOrphanCount;
            this.petBytes = petBytes;
            this.avatarBytes = avatarBytes;
        }
    }
}
