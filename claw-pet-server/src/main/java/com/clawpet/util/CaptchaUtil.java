package com.clawpet.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * 算术验证码工具 —— 纯 Java2D 绘制，零外部依赖
 *
 * 返回格式：{ imageBase64, expressionText, answer }
 * 例：expressionText = "3 + 5 = ?", answer = "8"
 */
public class CaptchaUtil {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 42;
    private static final Random RANDOM = new SecureRandom();

    /** 生成一道算术题 */
    public static CaptchaResult generate() {
        int a = RANDOM.nextInt(10) + 1;   // 1~10
        int b = RANDOM.nextInt(10) + 1;   // 1~10
        int op = RANDOM.nextInt(3);       // 0:+  1:-  2:*
        int answer;
        String expr;

        switch (op) {
            case 0: // 加法
                answer = a + b;
                expr = a + " + " + b + " = ?";
                break;
            case 1: // 减法（确保大减小）
                if (a < b) { int t = a; a = b; b = t; }
                answer = a - b;
                expr = a + " - " + b + " = ?";
                break;
            default: // 乘法（小数字 1~9）
                a = RANDOM.nextInt(9) + 1;
                b = RANDOM.nextInt(9) + 1;
                answer = a * b;
                expr = a + " × " + b + " = ?";
                break;
        }

        String imageBase64 = drawImage(expr);
        return new CaptchaResult(imageBase64, String.valueOf(answer));
    }

    /** 用 Java2D 绘制验证码图片，返回 Base64（不含 data:image 前缀） */
    private static String drawImage(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 噪点
        g2d.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 80; i++) {
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g2d.fillRect(x, y, 2, 2);
        }

        // 干扰线
        g2d.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 3; i++) {
            g2d.setColor(new Color(200 + RANDOM.nextInt(56), 200 + RANDOM.nextInt(56), 200 + RANDOM.nextInt(56)));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 绘制文字（每个字符随机颜色/旋转/位置）
        Font font = new Font("Arial", Font.BOLD, 22);
        g2d.setFont(font);
        char[] chars = text.toCharArray();
        int x = 10;
        for (char c : chars) {
            int colorR = 30 + RANDOM.nextInt(120);
            int colorG = 30 + RANDOM.nextInt(120);
            int colorB = 30 + RANDOM.nextInt(120);
            g2d.setColor(new Color(colorR, colorG, colorB));

            AffineTransform old = g2d.getTransform();
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4; // ±0.2 弧度
            g2d.translate(x, 28 + RANDOM.nextInt(6) - 3);
            g2d.rotate(angle);
            g2d.drawString(String.valueOf(c), 0, 0);
            g2d.setTransform(old);

            x += g2d.getFontMetrics().charWidth(c) + 2;
        }

        // 外边框
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        g2d.dispose();

        // 转 Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "JPEG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /** 验证码结果：Base64 图片 + 正确答案 */
    public static class CaptchaResult {
        private final String imageBase64;
        private final String answer;

        public CaptchaResult(String imageBase64, String answer) {
            this.imageBase64 = imageBase64;
            this.answer = answer;
        }

        public String getImageBase64() { return imageBase64; }
        public String getAnswer() { return answer; }
    }
}
