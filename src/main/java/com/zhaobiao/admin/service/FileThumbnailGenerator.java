package com.zhaobiao.admin.service;

import com.zhaobiao.admin.entity.FileThumbnailStatus;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Component
public class FileThumbnailGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileThumbnailGenerator.class);

    public static final String THUMBNAIL_CONTENT_TYPE = "image/jpeg";

    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 240;
    private static final int PDF_DPI = 120;

    public FileThumbnailPayload generate(String originalName, String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            return typeCover(originalName, contentType, FileThumbnailStatus.FAILED);
        }
        try {
            if (isImage(originalName, contentType)) {
                return imageThumbnail(content);
            }
            if (isPdf(originalName, contentType)) {
                return pdfThumbnail(content);
            }
            return typeCover(originalName, contentType, FileThumbnailStatus.UNSUPPORTED);
        } catch (Exception ex) {
            LOGGER.warn("生成附件缩略图失败: originalName={}, contentType={}, contentLength={}, reason={}",
                    originalName, contentType, content.length, ex.toString());
            LOGGER.debug("生成附件缩略图失败详情", ex);
            return typeCover(originalName, contentType, FileThumbnailStatus.FAILED);
        }
    }

    private FileThumbnailPayload imageThumbnail(byte[] content) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(content));
        if (source == null) {
            throw new IOException("Unsupported image content");
        }
        BufferedImage thumbnail = resize(source);
        return new FileThumbnailPayload(
                writeJpeg(thumbnail),
                THUMBNAIL_CONTENT_TYPE,
                thumbnail.getWidth(),
                thumbnail.getHeight(),
                FileThumbnailStatus.READY
        );
    }

    private FileThumbnailPayload pdfThumbnail(byte[] content) throws IOException {
        try (PDDocument document = PDDocument.load(content)) {
            if (document.getNumberOfPages() <= 0) {
                throw new IOException("PDF has no pages");
            }
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage rendered = renderer.renderImageWithDPI(0, PDF_DPI, ImageType.RGB);
            BufferedImage thumbnail = resize(rendered);
            return new FileThumbnailPayload(
                    writeJpeg(thumbnail),
                    THUMBNAIL_CONTENT_TYPE,
                    thumbnail.getWidth(),
                    thumbnail.getHeight(),
                    FileThumbnailStatus.READY
            );
        }
    }

    private FileThumbnailPayload typeCover(String originalName,
                                           String contentType,
                                           FileThumbnailStatus status) {
        try {
            BufferedImage image = new BufferedImage(MAX_WIDTH, MAX_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setColor(new Color(246, 248, 251));
                graphics.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);
                graphics.setColor(new Color(210, 218, 228));
                graphics.fillRoundRect(74, 34, 172, 172, 18, 18);
                graphics.setColor(Color.WHITE);
                graphics.fillRoundRect(88, 48, 144, 144, 12, 12);
                graphics.setColor(new Color(37, 99, 235));
                graphics.fillRect(88, 142, 144, 50);
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
                drawCentered(graphics, resolveLabel(originalName, contentType), 88, 142, 144, 50);
            } finally {
                graphics.dispose();
            }
            return new FileThumbnailPayload(writeJpeg(image), THUMBNAIL_CONTENT_TYPE, MAX_WIDTH, MAX_HEIGHT, status);
        } catch (IOException ex) {
            throw new IllegalStateException("Generate type cover failed", ex);
        }
    }

    private BufferedImage resize(BufferedImage source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return source;
        }
        double scale = Math.min((double) MAX_WIDTH / sourceWidth, (double) MAX_HEIGHT / sourceHeight);
        scale = Math.min(scale, 1.0d);
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private byte[] writeJpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", output)) {
            throw new IOException("No JPEG writer available");
        }
        return output.toByteArray();
    }

    private void drawCentered(Graphics2D graphics, String text, int x, int y, int width, int height) {
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        graphics.drawString(text, textX, textY);
    }

    private boolean isImage(String originalName, String contentType) {
        String normalizedContentType = normalize(contentType);
        if (normalizedContentType != null && normalizedContentType.startsWith("image/")) {
            return true;
        }
        String extension = extension(originalName);
        return "jpg".equals(extension)
                || "jpeg".equals(extension)
                || "png".equals(extension)
                || "gif".equals(extension)
                || "bmp".equals(extension);
    }

    private boolean isPdf(String originalName, String contentType) {
        String normalizedContentType = normalize(contentType);
        return "application/pdf".equals(normalizedContentType) || "pdf".equals(extension(originalName));
    }

    private String resolveLabel(String originalName, String contentType) {
        String extension = extension(originalName);
        if (StringUtils.hasText(extension)) {
            String label = extension.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
            return normalizeLabel(label);
        }
        String normalizedContentType = normalize(contentType);
        if (normalizedContentType != null && normalizedContentType.contains("/")) {
            String subtype = normalizedContentType.substring(normalizedContentType.indexOf('/') + 1)
                    .replaceAll("[^A-Za-z0-9]", "")
                    .toUpperCase(Locale.ROOT);
            return normalizeLabel(subtype);
        }
        return "FILE";
    }

    private String normalizeLabel(String label) {
        if (!StringUtils.hasText(label)) {
            return "FILE";
        }
        return label.length() > 8 ? label.substring(0, 8) : label;
    }

    private String extension(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return "";
        }
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }
}
