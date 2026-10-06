package com.clawpet.util;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

/**
 * 上传目录解析器 —— 把配置里的相对路径统一解析成「与启动位置无关」的绝对路径。
 *
 * <p>为什么需要它：原来三处代码各写了一遍
 * {@code Paths.get(System.getProperty("user.dir"), uploadPath)}，
 * 而 {@code user.dir} 取决于启动时的当前工作目录：
 * <ul>
 *   <li>IDE 里运行 → 工作目录是 claw-pet-server 模块目录 → 图片落在 claw-pet-server/upload/ ✅</li>
 *   <li>在仓库根目录 {@code java -jar claw-pet-server/target/xxx.jar} 启动 →
 *       图片落在 &lt;仓库根&gt;/upload/，而前端读的是 claw-pet-server 那边
 *       → 表现为"上传成功但图片全打不开" ❌</li>
 * </ul>
 *
 * <p>解析顺序（谁先成功用谁）：
 * <ol>
 *   <li>代码所在位置（IDE / {@code mvn spring-boot:run} 时是 {@code target/classes}）</li>
 *   <li>{@code java.class.path}：{@code java -jar} 启动时它就是那个 jar 的路径</li>
 *   <li>当前工作目录（最后的兜底）</li>
 * </ol>
 * 拿到基准目录后再向上找含 {@code pom.xml} 的模块根；找不到就用基准目录本身
 * （独立部署时 jar 和 upload 放一起，这本来就是对的）。
 */
public final class UploadPathResolver {

    private static final String MODULE_MARKER = "pom.xml";
    private static final int MAX_ASCENT = 8;

    private UploadPathResolver() {
    }

    /** 把配置值解析为绝对路径（不创建目录） */
    public static Path resolve(String configuredPath) {
        Path configured = Paths.get(configuredPath);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }
        return baseDir().resolve(configured).normalize();
    }

    /** 解析为绝对路径并确保目录存在 */
    public static Path resolveAndCreate(String configuredPath) {
        Path dir = resolve(configuredPath);
        if (!Files.isDirectory(dir) && !dir.toFile().mkdirs()) {
            throw new IllegalStateException(
                    "无法创建上传目录: " + dir + "\n"
                            + "请检查该路径是否可写，或在配置里把 upload.path 改成绝对路径。");
        }
        return dir;
    }

    /** 应用根目录：优先「含 pom.xml 的模块根」，否则用基准目录自身 */
    static Path baseDir() {
        Path start = codeLocation();
        if (start == null) {
            start = classPathLocation();
        }
        if (start == null) {
            start = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }

        Path cursor = start;
        for (int i = 0; i < MAX_ASCENT && cursor != null; i++) {
            if (Files.exists(cursor.resolve(MODULE_MARKER))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        return start;
    }

    /**
     * 从代码来源定位：编译产出目录返回该目录，jar 返回 jar 所在目录。
     *
     * <p>⚠️ 不能直接对 {@code getCodeSource().getLocation()} 调 {@code Paths.get(URI)}：
     * Spring Boot 打出来的可执行 jar 里代码位置是
     * {@code jar:file:/app.jar!/BOOT-INF/classes!/}，这种 URI 会解析成 zip 文件系统里的
     * ZipPath，拿它和默认文件系统的路径做 resolve 会抛 {@code ProviderMismatchException}
     * （栈里只有一句 null，很难看出原因）。所以先把 {@code jar:} 前缀和 {@code !/...}
     * 后缀剥掉，还原成真正的 file: URI。
     *
     * <p>另外实测发现：Spring Boot 的 LaunchedClassLoader 下 {@code getCodeSource()}
     * 可能直接是 null，所以它只是第一优先，失败要能继续往下走。
     */
    private static Path codeLocation() {
        try {
            CodeSource codeSource = UploadPathResolver.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }

            String spec = codeSource.getLocation().toString();
            if (spec.startsWith("jar:")) {
                spec = spec.substring("jar:".length());
            }
            int separator = spec.indexOf("!/");
            if (separator >= 0) {
                spec = spec.substring(0, separator);
            }

            Path code = Paths.get(new URI(spec)).toAbsolutePath().normalize();
            return Files.isDirectory(code) ? code : code.getParent();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 {@code java.class.path} 推断：{@code java -jar} 启动时整个 classpath 就只有那一个 jar。
     * 这是 fat jar 场景下最可靠的一招（不依赖 CodeSource）。
     */
    private static Path classPathLocation() {
        try {
            String classPath = System.getProperty("java.class.path", "");
            // 多个条目说明是 IDE / mvn 那种 classpath，不是 fat jar，交给别的策略
            if (classPath.isEmpty() || classPath.contains(File.pathSeparator)) {
                return null;
            }
            Path jar = Paths.get(classPath);
            if (!jar.isAbsolute()) {
                jar = Paths.get(System.getProperty("user.dir")).resolve(jar);
            }
            jar = jar.toAbsolutePath().normalize();
            return Files.isRegularFile(jar) ? jar.getParent() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
