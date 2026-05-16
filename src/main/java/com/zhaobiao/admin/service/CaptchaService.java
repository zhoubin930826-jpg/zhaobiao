package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final Set<String> SUPPORTED_SCENES = new HashSet<>(Arrays.asList("login", "register"));
    private static final int EXPIRE_MINUTES = 5;
    private static final int WIDTH = 110;
    private static final int HEIGHT = 42;
    private static final char[] CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    public CaptchaChallenge create(String scene, String captchaId) {
        String normalizedScene = normalizeScene(scene);
        String normalizedCaptchaId = normalizeCaptchaId(captchaId);
        cleanupExpired();
        String code = generateCode();
        captchaStore.put(buildKey(normalizedScene, normalizedCaptchaId),
                new CaptchaEntry(code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));
        return new CaptchaChallenge(normalizedCaptchaId, normalizedScene, code, renderImage(code));
    }

    public void validate(String scene, String captchaId, String captchaCode) {
        String normalizedScene = normalizeScene(scene);
        String normalizedCaptchaId = normalizeCaptchaId(captchaId);
        if (!StringUtils.hasText(captchaCode)) {
            throw invalidCaptcha();
        }
        CaptchaEntry entry = captchaStore.remove(buildKey(normalizedScene, normalizedCaptchaId));
        if (entry == null || entry.isExpired()) {
            throw invalidCaptcha();
        }
        if (!entry.getCode().equalsIgnoreCase(captchaCode.trim())) {
            throw invalidCaptcha();
        }
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException(400, "验证码场景不能为空");
        }
        String normalized = scene.trim().toLowerCase();
        if (!SUPPORTED_SCENES.contains(normalized)) {
            throw new BusinessException(400, "验证码场景不支持");
        }
        return normalized;
    }

    private String normalizeCaptchaId(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            throw new BusinessException(400, "验证码标识不能为空");
        }
        return captchaId.trim();
    }

    private BusinessException invalidCaptcha() {
        return new BusinessException(400, "验证码错误或已过期");
    }

    private String buildKey(String scene, String captchaId) {
        return scene + ":" + captchaId;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            builder.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)]);
        }
        return builder.toString();
    }

    private byte[] renderImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(248, 250, 252));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 6; i++) {
                graphics.setColor(randomColor(120, 210));
                graphics.setStroke(new BasicStroke(1.1f));
                graphics.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(randomColor(30, 120));
                int x = 14 + i * 22;
                int y = 29 + random.nextInt(6) - 3;
                double angle = Math.toRadians(random.nextInt(31) - 15);
                graphics.rotate(angle, x + 8, y - 8);
                graphics.drawString(String.valueOf(code.charAt(i)), x, y);
                graphics.rotate(-angle, x + 8, y - 8);
            }

            for (int i = 0; i < 40; i++) {
                graphics.setColor(randomColor(130, 230));
                graphics.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", output)) {
                throw new BusinessException(500, "生成验证码失败");
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, "生成验证码失败");
        } finally {
            graphics.dispose();
        }
    }

    private Color randomColor(int min, int max) {
        int bound = Math.max(1, max - min);
        return new Color(
                min + random.nextInt(bound),
                min + random.nextInt(bound),
                min + random.nextInt(bound)
        );
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(entry -> !entry.getValue().getExpireAt().isAfter(now));
    }

    private static class CaptchaEntry {
        private final String code;
        private final LocalDateTime expireAt;

        CaptchaEntry(String code, LocalDateTime expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }

        String getCode() {
            return code;
        }

        LocalDateTime getExpireAt() {
            return expireAt;
        }

        boolean isExpired() {
            return !expireAt.isAfter(LocalDateTime.now());
        }
    }
}
