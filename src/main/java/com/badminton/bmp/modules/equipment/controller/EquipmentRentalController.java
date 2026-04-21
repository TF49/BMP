package com.badminton.bmp.modules.equipment.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.entity.EquipmentRental;
import com.badminton.bmp.modules.equipment.service.EquipmentRentalService;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.service.MemberService;
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

@Tag(name = "器材租借模块", description = "租借 CRUD、支付、归还、统计")
@RestController
@RequestMapping("/api/equipment/rental")
public class EquipmentRentalController extends BaseController {

    @Autowired
    private EquipmentRentalService equipmentRentalService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private MemberService memberService;

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    /**
     * 获取所有器材租借记录，支持搜索和分页
     */
    @Operation(summary = "租借列表", description = "支持分页与筛选")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllRentals(
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "equipmentId", required = false) Long equipmentId,
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

            // 调用查询方法
            List<EquipmentRental> rentals = equipmentRentalService.findAll(memberId, equipmentId, status, keyword, page, size);
            // 统计符合条件的租借记录总数
            int total = equipmentRentalService.count(memberId, equipmentId, status, keyword);

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", rentals);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取器材租借记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取器材租借记录信息
     */
    @Operation(summary = "租借详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<EquipmentRental> getRentalInfo(@PathVariable("id") Long id) {
        try {
            EquipmentRental rental = equipmentRentalService.findById(id);
            if (rental != null) {
                return success(rental);
            } else {
                return error("租借记录不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取租借记录信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "新增租借")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> addRental(@Valid @RequestBody EquipmentRental rental) {
        try {
            // 权限验证：仅管理员可添加租借记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加租借记录
            int result = equipmentRentalService.add(rental);

            if (result > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", rental.getId());
                resultData.put("rentalNo", rental.getRentalNo());
                return success(resultData);
            } else {
                return error("添加租借记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加租借记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新器材租借记录（需要ADMIN权限）
     */
    @Operation(summary = "更新租借")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateRental(@Valid @RequestBody EquipmentRental rental) {
        try {
            // 权限验证：仅管理员可更新租借记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (rental.getId() == null) {
                return error("租借记录ID不能为空");
            }

            // 更新租借记录
            int result = equipmentRentalService.update(rental);

            if (result > 0) {
                return success(null);
            } else {
                return error("租借记录更新失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新租借记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除器材租借记录（需要ADMIN权限）
     */
    @Operation(summary = "删除/取消租借")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteRental(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除租借记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除租借记录
            int result = equipmentRentalService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除租借记录失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除租借记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新器材租借状态（需要ADMIN权限）
     */
    @Operation(summary = "更新租借状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateRentalStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("租借记录ID不能为空");
            }

            // 更新状态
            int result = equipmentRentalService.updateStatus(id, status);

            if (result > 0) {
                return success(null);
            } else {
                return error("更新租借状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新租借状态时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取器材租借统计信息
     * @return 统计信息（总租借数、已取消、租借中、已归还、逾期）
     */
    @Operation(summary = "租借统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getRentalStatistics() {
        try {
            Map<String, Object> statistics = equipmentRentalService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "租借支付", description = "支付方式：仅支持 BALANCE")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> processPayment(@RequestParam("rentalId") Long rentalId, @RequestParam("paymentMethod") String paymentMethod) {
        try {
            if (rentalId == null) {
                return error("租借ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }

            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            // 处理支付
            int result = equipmentRentalService.processPayment(rentalId, paymentMethod);

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

    /**
     * 处理退款（需要ADMIN权限）
     * @param rentalId 租借ID
     * @return 处理结果
     */
    @Operation(summary = "租借退款")
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processRefund(@RequestParam("rentalId") Long rentalId) {
        try {
            // 权限验证：仅管理员可处理退款
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (rentalId == null) {
                return error("租借ID不能为空");
            }

            // 处理退款
            int result = equipmentRentalService.processRefund(rentalId);

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

    /**
     * 获取器材下拉列表（供选择器材使用）
     *
     * 说明：
     * - 只返回「正常」状态的器材（status = 1），避免把停用/维护中的器材展示给租借业务。
     * - 为了下拉数据尽量完整，这里一次性取第一页、最多 100 条数据，
     *   对应 {@link com.badminton.bmp.modules.equipment.service.impl.EquipmentServiceImpl#findAll}
     *   中的分页限制（size 最大 100）。
     *
     * @return 器材列表（仅包含ID、名称、编号、可用数量及当前租价/押金）
     */
    @Operation(summary = "器材下拉列表")
    @GetMapping("/equipments")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getEquipmentList() {
        try {
            // 只查询正常状态(status = 1)的器材，第一页，最多 100 条（服务层会限制 size <= 100）
            List<Equipment> equipments = equipmentService.findAll(null, null, 1, null, 1, 100);
            List<Map<String, Object>> equipmentList = equipments.stream()
                    .map(equipment -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", equipment.getId());
                        item.put("equipmentName", equipment.getEquipmentName());
                        item.put("equipmentCode", equipment.getEquipmentCode());
                        item.put("availableQuantity", equipment.getAvailableQuantity());
                        item.put("rentalPrice", equipment.getRentalPrice());
                        item.put("rentalDeposit", equipment.getRentalDeposit());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(equipmentList);
        } catch (Exception e) {
            return error("获取器材列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取会员下拉列表（供器材租借选择会员使用）
     *
     * 处理方式参考场地预约模块：
     * - 支持按姓名或手机号模糊搜索；
     * - 关键字为空时返回最近的部分会员（最多 1000 条），避免一次性加载全部造成性能问题；
     *
     * @param keyword 姓名或手机号关键字
     * @return 会员列表（仅包含ID、姓名和手机号）
     */
    @Operation(summary = "会员下拉列表")
    @GetMapping("/members")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getMemberList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            String nameOrPhone = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            // 按姓名或手机号模糊匹配，分页参数与会员模块保持一致：page=1, size=1000
            List<Member> members = memberService.findByConditions(nameOrPhone, null, null, null, 1, 1, 1000);
            List<Map<String, Object>> memberList = members.stream()
                    .map(member -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", member.getId());
                        item.put("memberName", member.getMemberName());
                        item.put("phone", member.getPhone());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(memberList);
        } catch (Exception e) {
            return error("获取会员列表时发生错误：" + e.getMessage());
        }
    }
}
