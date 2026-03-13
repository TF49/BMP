package com.badminton.bmp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片处理工具类
 * 用于生成缩略图等图片处理操作
 */
public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 缩略图默认宽度
     */
    private static final int THUMBNAIL_WIDTH = 200;

    /**
     * 缩略图默认高度
     */
    private static final int THUMBNAIL_HEIGHT = 200;

    /**
     * 生成缩略图
     * @param sourceImagePath 原图路径
     * @param thumbnailPath 缩略图保存路径
     * @param width 缩略图宽度（可选，默认200）
     * @param height 缩略图高度（可选，默认200）
     * @return 是否生成成功
     */
    public static boolean generateThumbnail(String sourceImagePath, String thumbnailPath, Integer width, Integer height) {
        try {
            File sourceFile = new File(sourceImagePath);
            if (!sourceFile.exists()) {
                return false;
            }

            // 读取原图
            BufferedImage sourceImage = ImageIO.read(sourceFile);
            if (sourceImage == null) {
                return false;
            }

            // 使用默认尺寸或指定尺寸
            int thumbWidth = (width != null && width > 0) ? width : THUMBNAIL_WIDTH;
            int thumbHeight = (height != null && height > 0) ? height : THUMBNAIL_HEIGHT;

            // 计算缩放比例，保持宽高比
            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();
            double scaleX = (double) thumbWidth / sourceWidth;
            double scaleY = (double) thumbHeight / sourceHeight;
            double scale = Math.min(scaleX, scaleY);

            // 计算实际缩略图尺寸
            int actualWidth = (int) (sourceWidth * scale);
            int actualHeight = (int) (sourceHeight * scale);

            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();

            // 设置渲染质量
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制缩放后的图片
            g.drawImage(sourceImage, 0, 0, actualWidth, actualHeight, null);
            g.dispose();

            // 确保目录存在
            Path thumbnailFilePath = Paths.get(thumbnailPath);
            Files.createDirectories(thumbnailFilePath.getParent());

            // 保存缩略图
            String format = getImageFormat(thumbnailPath);
            File thumbnailFile = new File(thumbnailPath);
            return ImageIO.write(thumbnail, format, thumbnailFile);

        } catch (IOException e) {
            log.error("生成缩略图失败: sourceImagePath={}, thumbnailPath={}", sourceImagePath, thumbnailPath, e);
            return false;
        }
    }

    /**
     * 生成缩略图（使用默认尺寸）
     * @param sourceImagePath 原图路径
     * @param thumbnailPath 缩略图保存路径
     * @return 是否生成成功
     */
    public static boolean generateThumbnail(String sourceImagePath, String thumbnailPath) {
        return generateThumbnail(sourceImagePath, thumbnailPath, null, null);
    }

    /**
     * 从文件路径获取图片格式
     * @param filePath 文件路径
     * @return 图片格式（jpg, png, webp等）
     */
    private static String getImageFormat(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return "png";
        } else if (lowerPath.endsWith(".webp")) {
            // 注意：Java标准库不支持webp，可能需要第三方库
            return "jpg"; // 降级为jpg
        } else {
            return "jpg"; // 默认jpg
        }
    }

    /**
     * 删除图片文件（包括缩略图）
     * @param imagePath 图片路径
     * @return 是否删除成功
     */
    public static boolean deleteImageFile(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                return false;
            }

            File imageFile = new File(imagePath);
            boolean deleted = false;
            if (imageFile.exists()) {
                deleted = imageFile.delete();
            }

            // 尝试删除缩略图
            String thumbnailPath = getThumbnailPath(imagePath);
            if (thumbnailPath != null) {
                File thumbnailFile = new File(thumbnailPath);
                if (thumbnailFile.exists()) {
                    thumbnailFile.delete();
                }
            }

            return deleted;
        } catch (Exception e) {
            log.error("删除图片文件失败: imagePath={}", imagePath, e);
            return false;
        }
    }

    /**
     * 获取缩略图路径
     * @param originalPath 原图路径
     * @return 缩略图路径
     */
    public static String getThumbnailPath(String originalPath) {
        if (originalPath == null || originalPath.isEmpty()) {
            return null;
        }

        // 如果路径已经是缩略图路径，直接返回
        if (originalPath.contains("_thumb.")) {
            return originalPath;
        }

        // 在原文件名后插入_thumb
        int lastDot = originalPath.lastIndexOf('.');
        if (lastDot > 0) {
            String baseName = originalPath.substring(0, lastDot);
            String extension = originalPath.substring(lastDot);
            return baseName + "_thumb" + extension;
        }

        return originalPath + "_thumb.jpg"; // 默认扩展名
    }

    /**
     * 检查图片文件是否存在
     * @param imagePath 图片路径
     * @return 是否存在
     */
    public static boolean imageFileExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        File file = new File(imagePath);
        return file.exists() && file.isFile();
    }
}
