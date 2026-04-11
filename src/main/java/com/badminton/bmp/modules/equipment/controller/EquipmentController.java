package com.badminton.bmp.modules.equipment.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.entity.EquipmentImage;
import com.badminton.bmp.modules.equipment.service.EquipmentImageService;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.badminton.bmp.utils.ImageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Tag(name = "器材管理模块", description = "器材 CRUD、图片、统计")
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController extends BaseController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentImageService equipmentImageService;

    @Autowired
    private VenueService venueService;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "器材列表", description = "支持场馆/类型/状态/关键词搜索与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllEquipments(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "equipmentType", required = false) String equipmentType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // 验证分页参数
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 100) {
                size = 10;
            }

            // 将空字符串转换为null，便于后端查询
            if (keyword != null && keyword.trim().isEmpty()) {
                keyword = null;
            }
            if (equipmentType != null && equipmentType.trim().isEmpty()) {
                equipmentType = null;
            }

            // 调用查询方法
            List<Equipment> equipments = equipmentService.findAll(venueId, equipmentType, status, keyword, page, size);
            // 统计符合条件的器材总数
            int total = equipmentService.count(venueId, equipmentType, status, keyword);

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", equipments);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取器材列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取器材信息
     */
    @Operation(summary = "器材详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Equipment> getEquipmentInfo(@PathVariable("id") Long id) {
        try {
            Equipment equipment = equipmentService.findById(id);
            if (equipment != null) {
                return success(equipment);
            } else {
                return error("器材不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取器材信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加器材（需要ADMIN权限）
     */
    @Operation(summary = "添加器材")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addEquipment(@Valid @RequestBody Equipment equipment) {
        try {
            // 权限验证：仅管理员可添加器材
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加器材
            int result = equipmentService.add(equipment);

            if (result > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", equipment.getId());
                return success(resultData);
            } else {
                return error("添加器材失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            // 数据库唯一约束冲突（如器材编号重复）时返回友好提示
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    String msg = cause.getMessage();
                    if (msg != null && msg.contains("equipment_code")) {
                        return error("器材编号已存在，请更换编号");
                    }
                    return error("数据已存在，请检查后重试");
                }
                cause = cause.getCause();
            }
            return error("添加器材时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新器材信息（需要ADMIN权限）
     */
    @Operation(summary = "更新器材")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateEquipment(@Valid @RequestBody Equipment equipment) {
        try {
            // 权限验证：仅管理员可更新器材
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (equipment.getId() == null) {
                return error("器材ID不能为空");
            }

            // 更新器材信息
            int result = equipmentService.update(equipment);

            if (result > 0) {
                return success(null);
            } else {
                return error("器材信息更新失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    String msg = cause.getMessage();
                    if (msg != null && msg.contains("equipment_code")) {
                        return error("器材编号已存在，请更换编号");
                    }
                    return error("数据已存在，请检查后重试");
                }
                cause = cause.getCause();
            }
            return error("更新器材信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除器材（需要ADMIN权限）
     */
    @Operation(summary = "删除器材")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteEquipment(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除器材
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除器材
            int result = equipmentService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除器材失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除器材时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新器材状态（需要ADMIN权限）
     */
    @Operation(summary = "更新器材状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateEquipmentStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("器材ID不能为空");
            }

            // 更新状态
            int result = equipmentService.updateStatus(id, status);

            if (result > 0) {
                return success(null);
            } else {
                return error("更新器材状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新器材状态时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取器材统计信息
     * @return 统计信息（总器材数、停用、正常）
     */
    @Operation(summary = "器材统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getEquipmentStatistics() {
        try {
            Map<String, Object> statistics = equipmentService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取器材类型下拉列表（去重）
     * @return 类型列表
     */
    @Operation(summary = "器材类型选项")
    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getEquipmentTypes() {
        try {
            return success(equipmentService.getEquipmentTypes());
        } catch (Exception e) {
            return error("获取器材类型时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "低库存预警列表", description = "可用数量 ≤ 阈值的器材，threshold 默认 10")
    @GetMapping("/low-stock")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getLowStock(@RequestParam(value = "threshold", defaultValue = "10") Integer threshold) {
        try {
            return success(equipmentService.getLowStock(threshold));
        } catch (Exception e) {
            return error("获取库存预警列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 上传器材图片（需要ADMIN权限）- 单图片上传（向后兼容）
     *
     * 返回可直接访问的图片URL：/api/uploads/equipments/{filename}
     */
    @Operation(summary = "上传器材图片（单张）")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> uploadEquipmentImage(@RequestParam("file") MultipartFile file) {
        try {
            // 权限验证：仅管理员可上传
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (file == null || file.isEmpty()) {
                return error("请选择要上传的图片文件");
            }

            // 基础校验：必须是图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                return error("文件类型不支持，请上传图片文件");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot >= 0 && dot < originalFilename.length() - 1) {
                    ext = originalFilename.substring(dot + 1).toLowerCase();
                }
            }

            Set<String> allowedExt = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
            if (ext.isEmpty() || !allowedExt.contains(ext)) {
                return error("图片格式不支持，请上传 jpg/jpeg/png/webp 格式");
            }

            Path equipmentDir = Paths.get(uploadDir, "equipments").toAbsolutePath().normalize();
            Files.createDirectories(equipmentDir);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = equipmentDir.resolve(filename).normalize();

            // 防止路径穿越
            if (!target.startsWith(equipmentDir)) {
                return error("非法文件路径");
            }

            file.transferTo(target.toFile());

            // 生成缩略图
            String thumbnailPath = ImageUtils.getThumbnailPath(target.toString());
            ImageUtils.generateThumbnail(target.toString(), thumbnailPath);

            String url = "/api/uploads/equipments/" + filename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("thumbnailUrl", url.replace(filename, filename.replace("." + ext, "_thumb." + ext)));
            return success(result);
        } catch (Exception e) {
            return error("上传图片时发生错误：" + e.getMessage());
        }
    }

    /**
     * 多图片上传（需要ADMIN权限）
     * @param files 图片文件数组
     * @param equipmentId 器材ID（可选，如果提供则直接关联到器材）
     * @param imageType 图片类型（MAIN/DETAIL，默认DETAIL）
     * @return 上传结果
     */
    @Operation(summary = "上传器材图片（多张）")
    @PostMapping(value = "/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> uploadEquipmentImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "equipmentId", required = false) Long equipmentId,
            @RequestParam(value = "imageType", required = false, defaultValue = "DETAIL") String imageType) {
        try {
            // 权限验证：仅管理员可上传
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (files == null || files.length == 0) {
                return error("请选择要上传的图片文件");
            }

            Set<String> allowedExt = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
            List<Map<String, Object>> uploadedImages = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            Path equipmentDir = Paths.get(uploadDir, "equipments").toAbsolutePath().normalize();
            Files.createDirectories(equipmentDir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                try {
                    // 基础校验：必须是图片
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                        errors.add(file.getOriginalFilename() + ": 文件类型不支持");
                        continue;
                    }

                    String originalFilename = file.getOriginalFilename();
                    String ext = "";
                    if (originalFilename != null) {
                        int dot = originalFilename.lastIndexOf('.');
                        if (dot >= 0 && dot < originalFilename.length() - 1) {
                            ext = originalFilename.substring(dot + 1).toLowerCase();
                        }
                    }

                    if (ext.isEmpty() || !allowedExt.contains(ext)) {
                        errors.add(originalFilename + ": 图片格式不支持");
                        continue;
                    }

                    String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
                    Path target = equipmentDir.resolve(filename).normalize();

                    // 防止路径穿越
                    if (!target.startsWith(equipmentDir)) {
                        errors.add(originalFilename + ": 非法文件路径");
                        continue;
                    }

                    file.transferTo(target.toFile());

                    // 生成缩略图
                    String thumbnailPath = ImageUtils.getThumbnailPath(target.toString());
                    ImageUtils.generateThumbnail(target.toString(), thumbnailPath);

                    String url = "/api/uploads/equipments/" + filename;
                    String thumbnailUrl = url.replace(filename, filename.replace("." + ext, "_thumb." + ext));

                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("url", url);
                    imageInfo.put("thumbnailUrl", thumbnailUrl);
                    imageInfo.put("filename", filename);
                    uploadedImages.add(imageInfo);

                    // 如果提供了equipmentId，直接保存到数据库
                    if (equipmentId != null) {
                        EquipmentImage equipmentImage = new EquipmentImage();
                        equipmentImage.setEquipmentId(equipmentId);
                        equipmentImage.setImageUrl(url);
                        equipmentImage.setImageType(imageType);
                        equipmentImage.setSortOrder(uploadedImages.size() - 1);
                        equipmentImage.setCreateTime(LocalDateTime.now());
                        equipmentImageService.add(equipmentImage);
                    }
                } catch (Exception e) {
                    errors.add(file.getOriginalFilename() + ": " + userFriendlyErrorText("上传图片", e));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("images", uploadedImages);
            result.put("successCount", uploadedImages.size());
            result.put("totalCount", files.length);
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            return success(result);
        } catch (Exception e) {
            return error("上传图片时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取器材的所有图片
     * @param equipmentId 器材ID
     * @return 图片列表
     */
    @Operation(summary = "器材图片列表")
    @GetMapping("/{equipmentId}/images")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getEquipmentImages(@PathVariable("equipmentId") Long equipmentId) {
        try {
            List<EquipmentImage> images = equipmentImageService.findByEquipmentId(equipmentId);
            return success(images);
        } catch (Exception e) {
            return error("获取器材图片时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除图片（需要ADMIN权限）
     * @param id 图片ID
     * @return 删除结果
     */
    @Operation(summary = "删除器材图片")
    @DeleteMapping("/image/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteEquipmentImage(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            boolean deleted = equipmentImageService.deleteById(id);
            if (deleted) {
                return success(null);
            } else {
                return error("删除图片失败");
            }
        } catch (Exception e) {
            return error("删除图片时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新图片排序（需要ADMIN权限）
     * @param id 图片ID
     * @param sortOrder 排序顺序
     * @return 更新结果
     */
    @Operation(summary = "更新器材图片排序")
    @PutMapping("/image/{id}/sort")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateImageSortOrder(@PathVariable("id") Long id, @RequestParam("sortOrder") Integer sortOrder) {
        try {
            // 权限验证：仅管理员可更新
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            int result = equipmentImageService.updateSortOrder(id, sortOrder);
            if (result > 0) {
                return success(null);
            } else {
                return error("更新图片排序失败");
            }
        } catch (Exception e) {
            return error("更新图片排序时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取场馆下拉列表（供选择场馆使用）
     * @return 场馆列表（仅包含ID和名称）
     */
    @Operation(summary = "场馆下拉列表")
    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueList() {
        try {
            List<Venue> venues = venueService.findAll();
            // 只返回必要的字段：ID、名称、状态
            List<Map<String, Object>> venueList = venues.stream()
                    .map(venue -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", venue.getId());
                        item.put("venueName", venue.getVenueName());
                        item.put("status", venue.getStatus());
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return success(venueList);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }
}
