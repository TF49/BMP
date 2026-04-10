package com.badminton.bmp.modules.venue.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.entity.VenueImage;
import com.badminton.bmp.modules.venue.entity.VenueSchedule;
import com.badminton.bmp.modules.venue.entity.VenueStatusLog;
import com.badminton.bmp.modules.venue.service.VenueImageService;
import com.badminton.bmp.modules.venue.service.VenueScheduleService;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.badminton.bmp.modules.venue.service.VenueStatusLogService;
import com.badminton.bmp.utils.ImageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 场馆管理Controller
 * 处理场馆相关的请求
 * 参照UserController实现方式
 */
@Tag(name = "场馆管理模块", description = "场馆 CRUD、图片、营业时间、状态日志与统计")
@RestController
@RequestMapping("/api/venue")
public class VenueController extends BaseController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueImageService venueImageService;

    @Autowired
    private VenueScheduleService venueScheduleService;

    @Autowired
    private VenueStatusLogService venueStatusLogService;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident();
    }

    /**
     * 判断当前用户是否有权限管理指定场馆
     * 超管可管理所有场馆；场馆管理员仅能管理其所属场馆
     * @param venueId 场馆ID
     * @return 是否有管理权限
     */
    private boolean canManageVenue(Long venueId) {
        if (isAdmin()) {
            return true;
        }
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long myVenueId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            return myVenueId != null && myVenueId.equals(venueId);
        }
        return false;
    }


    @Operation(summary = "上传场馆图片（单张）", description = "需管理员权限，返回图片 URL")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> uploadVenueImage(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "venueId", required = false) Long venueId) {
        try {
            // 权限验证：仅管理员可上传
            if (!com.badminton.bmp.common.util.SecurityUtils.isPresident()
                    && !com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
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

            if (venueId != null && !canManageVenue(venueId)) {
                return error("权限不足，无法为该场馆上传图片");
            }

            Set<String> allowedExt = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
            if (ext.isEmpty() || !allowedExt.contains(ext)) {
                return error("图片格式不支持，请上传 jpg/jpeg/png/webp 格式");
            }

            Path venueDir = Paths.get(uploadDir, "venues").toAbsolutePath().normalize();
            Files.createDirectories(venueDir);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = venueDir.resolve(filename).normalize();

            // 防止路径穿越
            if (!target.startsWith(venueDir)) {
                return error("非法文件路径");
            }

            file.transferTo(target.toFile());

            // 生成缩略图
            String thumbnailPath = ImageUtils.getThumbnailPath(target.toString());
            ImageUtils.generateThumbnail(target.toString(), thumbnailPath);

            String url = "/api/uploads/venues/" + filename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("thumbnailUrl", url.replace(filename, filename.replace("." + ext, "_thumb." + ext)));
            return success(result);
        } catch (Exception e) {
            return error("上传图片时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "多图片上传", description = "可选 venueId 直接关联场馆，imageType 默认 DETAIL")
    @PostMapping(value = "/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> uploadVenueImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "imageType", required = false, defaultValue = "DETAIL") String imageType) {
        try {
            // 权限验证：仅管理员可上传
            if (!com.badminton.bmp.common.util.SecurityUtils.isPresident()
                    && !com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (files == null || files.length == 0) {
                return error("请选择要上传的图片文件");
            }

            Set<String> allowedExt = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
            if (venueId != null && !canManageVenue(venueId)) {
                return error("权限不足，无法为该场馆上传图片");
            }
            List<Map<String, Object>> uploadedImages = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            Path venueDir = Paths.get(uploadDir, "venues").toAbsolutePath().normalize();
            Files.createDirectories(venueDir);

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
                    Path target = venueDir.resolve(filename).normalize();

                    // 防止路径穿越
                    if (!target.startsWith(venueDir)) {
                        errors.add(originalFilename + ": 非法文件路径");
                        continue;
                    }

                    file.transferTo(target.toFile());

                    // 生成缩略图
                    String thumbnailPath = ImageUtils.getThumbnailPath(target.toString());
                    ImageUtils.generateThumbnail(target.toString(), thumbnailPath);

                    String url = "/api/uploads/venues/" + filename;
                    String thumbnailUrl = url.replace(filename, filename.replace("." + ext, "_thumb." + ext));

                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("url", url);
                    imageInfo.put("thumbnailUrl", thumbnailUrl);
                    imageInfo.put("filename", filename);
                    uploadedImages.add(imageInfo);

                    // 如果提供了venueId，直接保存到数据库
                    if (venueId != null) {
                        VenueImage venueImage = new VenueImage();
                        venueImage.setVenueId(venueId);
                        venueImage.setImageUrl(url);
                        venueImage.setImageType(imageType);
                        venueImage.setSortOrder(uploadedImages.size() - 1);
                        venueImage.setCreateTime(LocalDateTime.now());
                        venueImageService.add(venueImage);
                        // 关键：把数据库生成的图片ID回传给前端
                        // 否则前端删除详情图会用随机uid当id，导致删除不生效/刷新后复活
                        imageInfo.put("id", venueImage.getId());
                        imageInfo.put("venueId", venueId);
                        imageInfo.put("imageType", imageType);
                    }
                } catch (Exception e) {
                    errors.add(file.getOriginalFilename() + ": " + e.getMessage());
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

    @Operation(summary = "获取场馆图片列表")
    @GetMapping("/{venueId}/images")
    public Result<Object> getVenueImages(@Parameter(description = "场馆ID", required = true) @PathVariable("venueId") Long venueId) {
        try {
            List<VenueImage> images = venueImageService.findByVenueId(venueId);
            return success(images);
        } catch (Exception e) {
            return error("获取场馆图片时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "绑定已上传图片到场馆", description = "将未入库的图片 URL 关联到指定场馆")
    @PostMapping("/{venueId}/images/bind")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> bindVenueImages(@PathVariable("venueId") Long venueId,
                                          @RequestBody List<Map<String, Object>> images) {
        try {
            // 权限验证：超管可操作；场馆管理员仅能对自己所属场馆执行绑定
            if (!canManageVenue(venueId)) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (venueId == null) {
                return error("场馆ID不能为空");
            }

            if (images == null || images.isEmpty()) {
                return success(null);
            }

            List<VenueImage> toSave = new ArrayList<>();
            int index = 0;

            for (Map<String, Object> imgMap : images) {
                if (imgMap == null) {
                    continue;
                }

                Object urlObj = imgMap.get("imageUrl");
                if (urlObj == null) {
                    urlObj = imgMap.get("url");
                }
                if (urlObj == null) {
                    continue;
                }
                String imageUrl = String.valueOf(urlObj);
                if (imageUrl.trim().isEmpty()) {
                    continue;
                }

                String imageType = "DETAIL";
                Object typeObj = imgMap.get("imageType");
                if (typeObj != null) {
                    imageType = String.valueOf(typeObj);
                }

                Integer sortOrder = null;
                Object sortOrderObj = imgMap.get("sortOrder");
                if (sortOrderObj instanceof Number) {
                    sortOrder = ((Number) sortOrderObj).intValue();
                } else if (sortOrderObj != null) {
                    try {
                        sortOrder = Integer.parseInt(sortOrderObj.toString());
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (sortOrder == null) {
                    sortOrder = index;
                }

                VenueImage image = new VenueImage();
                image.setVenueId(venueId);
                image.setImageUrl(imageUrl);
                image.setImageType(imageType);
                image.setSortOrder(sortOrder);
                image.setCreateTime(LocalDateTime.now());

                toSave.add(image);
                index++;
            }

            if (toSave.isEmpty()) {
                return success(null);
            }

            int savedCount = venueImageService.batchAdd(toSave);
            Map<String, Object> result = new HashMap<>();
            result.put("savedCount", savedCount);
            return success(result);
        } catch (Exception e) {
            return error("绑定场馆图片时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除场馆图片")
    @DeleteMapping("/image/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteVenueImage(@PathVariable("id") Long id) {
        try {
            // 权限验证：超管可删除所有图片；场馆管理员仅能删除自己场馆下的图片
            com.badminton.bmp.modules.venue.entity.VenueImage image = venueImageService.findById(id);
            if (image == null) {
                return error("图片不存在");
            }
            Long imageVenueId = image.getVenueId();
            if (!canManageVenue(imageVenueId)) {
                return error("权限不足，仅管理员可执行此操作");
            }

            boolean deleted = venueImageService.deleteById(id);
            if (deleted) {
                return success(null);
            } else {
                return error("删除图片失败");
            }
        } catch (Exception e) {
            return error("删除图片时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新图片排序")
    @PutMapping("/image/{id}/sort")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateImageSortOrder(@PathVariable("id") Long id, @RequestParam("sortOrder") Integer sortOrder) {
        try {
            // 权限验证：超管可更新所有图片排序；场馆管理员仅能更新自己场馆下的图片
            com.badminton.bmp.modules.venue.entity.VenueImage image = venueImageService.findById(id);
            if (image == null) {
                return error("图片不存在");
            }
            Long imageVenueId = image.getVenueId();
            if (!canManageVenue(imageVenueId)) {
                return error("权限不足，仅管理员可执行此操作");
            }

            int result = venueImageService.updateSortOrder(id, sortOrder);
            if (result > 0) {
                return success(null);
            } else {
                return error("更新排序失败");
            }
        } catch (Exception e) {
            return error("更新排序时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆列表", description = "支持按名称、地址、状态搜索与分页，按角色过滤可见范围")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllVenues(
            @RequestParam(value = "venueName", required = false) String venueName,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "status", required = false) Integer status,
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
            if (venueName != null && venueName.trim().isEmpty()) {
                venueName = null;
            }
            if (address != null && address.trim().isEmpty()) {
                address = null;
            }

            // 根据角色控制可见场馆范围
            List<Venue> venues;
            int total;

            if (com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
                // 协会会长：可查看所有场馆（原逻辑）
                venues = venueService.findByVenueNameOrAddress(venueName, address, status, page, size);
                total = venueService.countByVenueNameOrAddress(venueName, address, status);
            } else if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                // 场馆管理员：只能看到自己绑定的场馆
                Long myVenueId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
                venues = new ArrayList<>();
                total = 0;
                if (myVenueId != null) {
                    Venue v = venueService.findById(myVenueId);
                    if (v != null) {
                        boolean match = true;
                        if (venueName != null) {
                            String name = v.getVenueName() != null ? v.getVenueName() : "";
                            match = name.contains(venueName);
                        }
                        if (match && address != null) {
                            String addr = v.getAddress() != null ? v.getAddress() : "";
                            match = addr.contains(address);
                        }
                        if (match && status != null) {
                            match = status.equals(v.getStatus());
                        }
                        if (match) {
                            venues.add(v);
                            total = 1;
                        }
                    }
                }
            } else {
                // 普通会员等其他角色：允许查看营业中的场馆列表
                // 如果未显式传入状态，则默认只展示营业中(1)的场馆
                Integer queryStatus = status;
                if (queryStatus == null) {
                    queryStatus = 1;
                }
                venues = venueService.findByVenueNameOrAddress(venueName, address, queryStatus, page, size);
                total = venueService.countByVenueNameOrAddress(venueName, address, queryStatus);
            }

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", venues);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆详情", description = "根据ID获取单个场馆信息")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Venue> getVenueInfo(@PathVariable("id") Long id) {
        try {
            Venue venue = venueService.findById(id);
            if (venue != null) {
                return success(venue);
            } else {
                return error("场馆不存在");
            }
        } catch (Exception e) {
            return error("获取场馆信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "添加场馆", description = "需协会会长权限")
    @PostMapping("/add")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> addVenue(@Valid @RequestBody Venue venue) {
        try {
            // 权限验证：仅管理员可添加场馆
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加场馆
            int result = venueService.add(venue);

            if (result > 0) {
                // 返回创建的场馆ID，便于前端保存图片和营业时间配置
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", venue.getId());
                return success(resultData);
            } else {
                return error("添加场馆失败");
            }
        } catch (Exception e) {
            return error("添加场馆时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新场馆信息")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateVenue(@Valid @RequestBody Venue venue) {
        try {
            // 权限验证：超管可更新所有场馆；场馆管理员仅能更新自己所属场馆
            if (venue.getId() == null) {
                return error("场馆ID不能为空");
            }
            if (!canManageVenue(venue.getId())) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 检查场馆是否存在
            Venue existingVenue = venueService.findById(venue.getId());
            if (existingVenue == null) {
                return error("场馆不存在");
            }

            // 更新场馆信息
            int result = venueService.update(venue);

            if (result > 0) {
                return success(null);
            } else {
                return error("场馆信息更新失败");
            }
        } catch (Exception e) {
            return error("更新场馆信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除场馆", description = "需协会会长权限，有关联场地/赛事时不可删")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> deleteVenue(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除场馆
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 检查场馆是否存在
            Venue existingVenue = venueService.findById(id);
            if (existingVenue == null) {
                return error("场馆不存在");
            }

            // 检查是否有关联的场地
            int courtCount = venueService.countCourtsByVenueId(id);
            if (courtCount > 0) {
                return error("该场馆下存在 " + courtCount + " 个场地，无法删除。请先删除或转移相关场地后再删除场馆");
            }

            // 检查是否有关联的赛事
            int tournamentCount = venueService.countTournamentsByVenueId(id);
            if (tournamentCount > 0) {
                return error("该场馆下存在 " + tournamentCount + " 个赛事，无法删除。请先删除或转移相关赛事后再删除场馆");
            }

            // 删除场馆
            int result = venueService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除场馆失败");
            }
        } catch (Exception e) {
            return error("删除场馆时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "获取场馆营业时间配置")
    @GetMapping("/{venueId}/schedules")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueSchedules(@PathVariable("venueId") Long venueId) {
        try {
            // 权限验证：超管可查看所有；场馆管理员仅能查看自己所属场馆的营业时间
            if (!canManageVenue(venueId)) {
                if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                    // 场馆管理员试图查看非本人场馆：返回空列表
                    return success(new ArrayList<>());
                }
                return error("权限不足，仅管理员可执行此操作");
            }

            List<VenueSchedule> schedules = venueScheduleService.findByVenueId(venueId);
            return success(schedules);
        } catch (Exception e) {
            return error("获取营业时间时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "保存场馆营业时间配置")
    @PostMapping("/{venueId}/schedules")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> saveVenueSchedules(@PathVariable("venueId") Long venueId, @Valid @RequestBody List<VenueSchedule> schedules) {
        try {
            // 权限验证：超管可配置所有场馆；场馆管理员仅能配置自己所属场馆
            if (!canManageVenue(venueId)) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 验证时间逻辑（开始时间必须小于结束时间）
            for (VenueSchedule schedule : schedules) {
                if (!venueScheduleService.validateBusinessHours(
                        schedule.getStartTime().toString(), schedule.getEndTime().toString())) {
                    return error("营业时间无效：" + schedule.getScheduleType() + " 的开始时间必须小于结束时间");
                }
            }

            int count = venueScheduleService.batchSave(venueId, schedules);
            Map<String, Object> result = new HashMap<>();
            result.put("savedCount", count);
            return success(result);
        } catch (Exception e) {
            return error("保存营业时间时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除营业时间配置")
    @DeleteMapping("/schedule/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteVenueSchedule(@PathVariable("id") Long id) {
        try {
            // 权限验证：超管可删除所有时间表；场馆管理员仅能删除自己所属场馆的时间表
            VenueSchedule schedule = venueScheduleService.findById(id);
            if (schedule == null) {
                return error("营业时间配置不存在");
            }
            Long venueId = schedule.getVenueId();
            if (!canManageVenue(venueId)) {
                return error("权限不足，仅管理员可执行此操作");
            }

            int result = venueScheduleService.deleteById(id);
            if (result > 0) {
                return success(null);
            } else {
                return error("删除营业时间配置失败");
            }
        } catch (Exception e) {
            return error("删除营业时间配置时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "获取场馆状态变更历史")
    @GetMapping("/{venueId}/status-logs")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueStatusLogs(@PathVariable("venueId") Long venueId) {
        try {
            // 权限验证：超管可查看所有；场馆管理员仅能查看自己所属场馆的日志
            if (!canManageVenue(venueId)) {
                if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                    // 场馆管理员试图查看非本人场馆：按约定返回空列表（200）
                    return success(new ArrayList<>());
                }
                return error("权限不足，仅管理员可执行此操作");
            }

            List<VenueStatusLog> logs = venueStatusLogService.findByVenueId(venueId);
            return success(logs);
        } catch (Exception e) {
            return error("获取状态变更历史时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆统计", description = "总场馆数、营业中、暂停、关闭")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueStatistics() {
        try {
            Map<String, Object> statistics = venueService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }
}
