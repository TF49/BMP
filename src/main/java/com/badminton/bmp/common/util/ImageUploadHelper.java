package com.badminton.bmp.common.util;

import com.badminton.bmp.utils.ImageUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.UUID;

/**
 * 图片上传公共工具，抽取自 VenueController / EquipmentController 中重复的校验与存储逻辑。
 */
public final class ImageUploadHelper {

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));

    private ImageUploadHelper() {}

    /**
     * 校验上传文件是否为合法图片。
     * @return 错误信息，为 null 表示通过
     */
    public static String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "请选择要上传的图片文件";
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return "文件类型不支持，请上传图片文件";
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            return "图片格式不支持，请上传 jpg/jpeg/png/webp 格式";
        }
        return null;
    }

    /**
     * 将通过校验的图片文件保存到指定子目录并生成缩略图。
     *
     * @param file      已通过 {@link #validate} 校验的文件
     * @param uploadDir 应用级上传根目录（如 {@code ./uploads}）
     * @param subDir    子目录名（如 {@code "venues"} / {@code "equipments"}）
     * @param urlPrefix URL 前缀（如 {@code "/api/uploads/venues/"}）
     * @return 包含 url、thumbnailUrl、filename 的 Map
     */
    public static Map<String, Object> store(MultipartFile file, String uploadDir,
                                            String subDir, String urlPrefix) throws Exception {
        String ext = extractExtension(file.getOriginalFilename());

        Path dir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(filename).normalize();

        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("非法文件路径");
        }

        file.transferTo(target.toFile());

        String thumbnailPath = ImageUtils.getThumbnailPath(target.toString());
        ImageUtils.generateThumbnail(target.toString(), thumbnailPath);

        String url = urlPrefix + filename;
        String thumbnailUrl = url.replace(filename, filename.replace("." + ext, "_thumb." + ext));

        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("thumbnailUrl", thumbnailUrl);
        result.put("filename", filename);
        return result;
    }

    /**
     * 提取文件扩展名（小写）。
     */
    public static String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0 && dot < originalFilename.length() - 1) {
            return originalFilename.substring(dot + 1).toLowerCase();
        }
        return "";
    }
}
