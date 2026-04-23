package com.badminton.bmp.modules.stringing.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.stringing.entity.StringingService;
import com.badminton.bmp.modules.stringing.service.StringingServiceService;
import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.badminton.bmp.common.util.SecurityUtils;

@Tag(name = "穿线服务模块", description = "穿线服务 CRUD、统计")
@RestController
@RequestMapping("/api/stringing")
public class StringingServiceController extends BaseController {

    @Autowired
    private StringingServiceService stringingServiceService;

    @Autowired
    private EquipmentService equipmentService;

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    /**
     * 获取所有穿线服务记录，支持搜索和分页
     * @param venueId 场馆ID（可选，会长按场馆筛选）
     * @param createTimeStart 创建时间起（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param createTimeEnd 创建时间止（可选）
     */
    @Operation(summary = "穿线服务列表", description = "支持分页与筛选")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllServices(
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "createTimeStart", required = false) String createTimeStart,
            @RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
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

            // 调用查询方法（Service会自动进行权限过滤）
            List<StringingService> services = stringingServiceService.findAll(
                    memberId, userId, status, keyword, venueId, createTimeStart, createTimeEnd, page, size);
            int total = stringingServiceService.count(
                    memberId, userId, status, keyword, venueId, createTimeStart, createTimeEnd);

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", services);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取穿线服务记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取穿线服务记录信息
     */
    @Operation(summary = "穿线服务详情（按ID）")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<StringingService> getServiceInfo(@PathVariable("id") Long id) {
        try {
            StringingService service = stringingServiceService.findById(id);
            if (service != null) {
                return success(service);
            } else {
                return error("服务记录不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取服务记录信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 根据服务单号获取穿线服务记录信息
     */
    @Operation(summary = "穿线服务详情（按单号）")
    @GetMapping("/info/no/{serviceNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<StringingService> getServiceInfoByNo(@PathVariable("serviceNo") String serviceNo) {
        try {
            StringingService service = stringingServiceService.findByServiceNo(serviceNo);
            if (service != null) {
                return success(service);
            } else {
                return error("服务记录不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取服务记录信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加穿线服务申请（所有登录用户都可以申请）
     */
    @Operation(summary = "新增穿线服务")
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> addService(@Valid @RequestBody StringingService service) {
        try {
            // 验证线材信息（业务逻辑，非字段级校验）
            if (service.getIsOwnString() == null || service.getIsOwnString() == 0) {
                if (service.getStringId() == null &&
                    (service.getStringName() == null || service.getStringName().trim().isEmpty())) {
                    return error("不自带线材时，必须选择系统线材或填写线材名称");
                }
            } else {
                if (service.getStringName() == null || service.getStringName().trim().isEmpty()) {
                    return error("自带线材时，必须填写线材名称");
                }
            }

            // 添加服务记录
            int result = stringingServiceService.add(service);

            if (result > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", service.getId());
                resultData.put("serviceNo", service.getServiceNo());
                resultData.put("servicePrice", service.getServicePrice());
                return success(resultData);
            } else {
                return error("添加服务记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加服务记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新穿线服务记录（需要管理员权限）
     */
    @Operation(summary = "更新穿线服务")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateService(@Valid @RequestBody StringingService service) {
        try {
            // 权限验证：仅管理员可更新服务记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (service.getId() == null) {
                return error("服务记录ID不能为空");
            }

            // 更新服务记录
            int result = stringingServiceService.update(service);

            if (result > 0) {
                return success(null);
            } else {
                return error("服务记录更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新服务记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除穿线服务记录（需要管理员权限）
     */
    @Operation(summary = "删除穿线服务")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteService(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除服务记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除服务记录
            int result = stringingServiceService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除服务记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除服务记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新穿线服务状态（需要管理员权限）
     */
    @Operation(summary = "更新穿线服务状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateServiceStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("服务记录ID不能为空");
            }

            // 更新状态
            int result = stringingServiceService.updateStatus(id, status);

            if (result > 0) {
                return success(null);
            } else {
                return error("更新服务状态失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新服务状态时发生错误：" + e.getMessage());
        }
    }

    /**
     * 用户取消自己的穿线服务
     */
    @Operation(summary = "用户取消穿线服务")
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> cancelServiceByUser(@RequestParam("serviceId") Long serviceId) {
        try {
            if (serviceId == null) {
                return error("服务记录ID不能为空");
            }

            int result = stringingServiceService.cancelByUser(serviceId);
            if (result > 0) {
                return success(null);
            } else {
                return error("取消穿线服务失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("取消穿线服务时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取穿线服务统计信息
     * @return 统计信息（总服务数、等待、正在、完成、已取消）
     */
    @Operation(summary = "穿线服务统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getServiceStatistics() {
        try {
            Map<String, Object> statistics = stringingServiceService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取可选的线材列表（从sys_equipment中equipment_type为STRING的器材）
     * @return 线材列表（仅包含ID和名称，只返回正常状态的线材）
     */
    @Operation(summary = "球线下拉列表")
    @GetMapping("/strings")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getStringOptions() {
        try {
            List<Equipment> strings = equipmentService.findAll(null, "STRING", 1, null, 1, 200);
            List<Map<String, Object>> stringList = strings.stream()
                    .map(equipment -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", equipment.getId());
                        item.put("equipmentName", equipment.getEquipmentName());
                        item.put("equipmentCode", equipment.getEquipmentCode());
                        item.put("price", equipment.getPrice());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(stringList);
        } catch (Exception e) {
            return error("获取线材列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 计算服务价格（用于前端预览）
     * @param stringId 线材ID（可选）
     * @param isOwnString 是否自带线材（0-否，1-是）
     * @return 计算后的价格
     */
    @Operation(summary = "计算穿线价格")
    @GetMapping("/calculate-price")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> calculatePrice(
            @RequestParam(value = "stringId", required = false) Long stringId,
            @RequestParam(value = "isOwnString", required = false, defaultValue = "0") Integer isOwnString) {
        try {
            BigDecimal price = stringingServiceService.calculatePrice(stringId, isOwnString);
            Map<String, Object> result = new HashMap<>();
            result.put("price", price);
            return success(result);
        } catch (Exception e) {
            return error("计算价格时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理支付（需要管理员权限，与器材租借/预约管理逻辑一致）
     * @param serviceId 穿线服务ID
     * @param paymentMethod 支付方式（仅支持 BALANCE）
     * @return 处理结果
     */
    @Operation(summary = "穿线服务支付")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processPayment(@RequestParam("serviceId") Long serviceId, @RequestParam("paymentMethod") String paymentMethod) {
        try {
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }
            if (serviceId == null) {
                return error("服务ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }
            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }
            int result = stringingServiceService.processPayment(serviceId, paymentMethod);
            if (result > 0) {
                return success(null);
            } else {
                return error("支付处理失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("支付处理时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "普通用户支付本人穿线服务")
    @PostMapping("/member/payment")
    @PreAuthorize("hasAnyRole('USER','MEMBER','PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processMemberPayment(@RequestParam("serviceId") Long serviceId,
                                               @RequestParam("paymentMethod") String paymentMethod) {
        try {
            if (serviceId == null) {
                return error("服务ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }
            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            if (isAdmin()) {
                int result = stringingServiceService.processPayment(serviceId, paymentMethod);
                return result > 0 ? success(null) : error("支付处理失败");
            }

            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null || current.getId() == null) {
                return error("未登录或Token无效");
            }

            int result = stringingServiceService.processMemberPayment(serviceId, paymentMethod, current.getId());
            return result > 0 ? success(null) : error("支付处理失败");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("支付处理时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理退款（需要管理员权限，与器材租借/预约管理逻辑一致）
     * @param serviceId 穿线服务ID
     * @return 处理结果
     */
    @Operation(summary = "穿线服务退款")
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processRefund(@RequestParam("serviceId") Long serviceId) {
        try {
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }
            if (serviceId == null) {
                return error("服务ID不能为空");
            }
            int result = stringingServiceService.processRefund(serviceId);
            if (result > 0) {
                return success(null);
            } else {
                return error("退款处理失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("退款处理时发生错误：" + e.getMessage());
        }
    }
}
