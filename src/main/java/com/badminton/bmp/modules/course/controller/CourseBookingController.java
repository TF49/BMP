package com.badminton.bmp.modules.course.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.common.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Tag(name = "课程预约模块", description = "课程预约 CRUD、支付、统计")
@RestController
@RequestMapping("/api/course/booking")
public class CourseBookingController extends BaseController {
    @Autowired
    private CourseBookingService courseBookingService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoachService coachService;

    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "我的课程预约明细", description = "COACH 角色：仅返回当前教练所教课程的预约记录")
    @GetMapping("/for-coach")
    @PreAuthorize("hasRole('COACH')")
    public Result<Object> getBookingsForCoach(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("data", java.util.Collections.emptyList());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                empty.put("pages", 0);
                return success(empty);
            }
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            List<CourseBooking> bookings = courseBookingService.findAllForCoach(coachId, courseId, status, keyword, page, size);
            int total = courseBookingService.countForCoach(coachId, courseId, status, keyword);
            Map<String, Object> result = new HashMap<>();
            result.put("data", bookings);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (Exception e) {
            return error("获取预约明细时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "教练端预约详情", description = "COACH 角色：仅返回当前教练所教课程下的预约详情")
    @GetMapping("/for-coach/{id}")
    @PreAuthorize("hasRole('COACH')")
    public Result<CourseBooking> getBookingDetailForCoach(@PathVariable("id") Long id) {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                return error("未绑定教练档案，请联系管理员在教练管理中关联账号");
            }
            CourseBooking booking = courseBookingService.findByIdForCoach(coachId, id);
            return booking != null ? success(booking) : error("预约记录不存在或无权查看");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("获取预约详情时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "教练端更新预约状态", description = "COACH 角色：仅可对自己课程的预约执行签到、完成或取消")
    @PutMapping("/for-coach/status")
    @PreAuthorize("hasRole('COACH')")
    public Result<Object> updateBookingStatusForCoach(@RequestBody CoachBookingStatusRequest request) {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                return error("未绑定教练档案，请联系管理员在教练管理中关联账号");
            }
            if (request == null || request.getId() == null) {
                return error("预约记录ID不能为空");
            }
            int result = courseBookingService.updateStatusForCoach(coachId, request.getId(), request.getStatus(), request.getRemark());
            return result > 0 ? success(null) : error("更新预约状态失败");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新预约状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程预约列表", description = "支持会员/课程/状态/关键词筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllBookings(
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            List<CourseBooking> bookings = courseBookingService.findAll(memberId, courseId, status, keyword, page, size);
            int total = courseBookingService.count(memberId, courseId, status, keyword);
            Map<String, Object> result = new HashMap<>();
            result.put("data", bookings);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取课程预约记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程预约详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<CourseBooking> getBookingInfo(@PathVariable("id") Long id) {
        try {
            CourseBooking booking = courseBookingService.findById(id);
            return booking != null ? success(booking) : error("预约记录不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取预约记录信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "新增课程预约")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> addBooking(@Valid @RequestBody CourseBookingCreateRequest request) {
        try {
            CourseBooking booking = new CourseBooking();
            booking.setMemberId(request.getMemberId());
            booking.setCourseId(request.getCourseId());
            booking.setOrderAmount(request.getOrderAmount());
            booking.setStatus(request.getStatus());
            booking.setRemark(request.getRemark());

            // 验证必填字段（管理员必填会员；普通用户可为空，由后端从当前用户补全）
            if (isAdmin() && booking.getMemberId() == null) {
                return error("会员ID不能为空");
            }

            // 添加预约记录
            int result = courseBookingService.add(booking);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", booking.getId());
                data.put("bookingNo", booking.getBookingNo());
                return success(data);
            } else {
                return error("添加预约记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加预约记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新课程预约")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateBooking(@Valid @RequestBody CourseBooking booking) {
        try {

            if (booking.getId() == null) {
                return error("预约记录ID不能为空");
            }
            
            // 更新预约记录
            int result = courseBookingService.update(booking);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("预约记录更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新预约记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除课程预约")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> deleteBooking(@PathVariable("id") Long id) {
        try {

            // 删除预约记录
            int result = courseBookingService.deleteById(id);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("删除预约记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除预约记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新课程预约状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateBookingStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {

            if (id == null) {
                return error("预约记录ID不能为空");
            }
            
            // 验证状态值是否有效
            if (status == null || status < 0 || status > 4) {
                return error("状态值无效，必须是0（已取消）、1（待支付）、2（已支付）、3（进行中）或4（已完成）");
            }
            
            // 更新预约状态
            int result = courseBookingService.updateStatus(id, status);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("更新预约状态失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新预约状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程预约统计")
    @GetMapping("/statistics")
    public Result<Object> getBookingStatistics() {
        try {
            return success(courseBookingService.getStatistics());
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理支付（需要ADMIN权限）
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式（仅支持 BALANCE）
     * @return 处理结果
     */
    @Operation(summary = "课程预约支付")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processPayment(@RequestParam("bookingId") Long bookingId, @RequestParam("paymentMethod") String paymentMethod) {
        try {
            // 权限验证：仅管理员可处理支付
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (bookingId == null) {
                return error("预约ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }

            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            // 处理支付
            int result = courseBookingService.processPayment(bookingId, paymentMethod);

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

    @Operation(summary = "普通用户支付本人课程预约")
    @PostMapping("/member/payment")
    @PreAuthorize("hasAnyRole('USER','MEMBER','PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processMemberPayment(@RequestParam("bookingId") Long bookingId,
                                               @RequestParam("paymentMethod") String paymentMethod) {
        try {
            if (bookingId == null) {
                return error("预约ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }

            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            if (isAdmin()) {
                int result = courseBookingService.processPayment(bookingId, paymentMethod);
                return result > 0 ? success(null) : error("支付处理失败");
            }

            User current = SecurityUtils.getCurrentUser();
            if (current == null || current.getId() == null) {
                return error("未登录或Token无效");
            }

            int result = courseBookingService.processMemberPayment(bookingId, paymentMethod, current.getId());
            return result > 0 ? success(null) : error("支付处理失败");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("支付处理时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理退款（需要ADMIN权限）
     * @param bookingId 预约ID
     * @return 处理结果
     */
    @Operation(summary = "课程预约退款")
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processRefund(@RequestParam("bookingId") Long bookingId) {
        try {
            // 权限验证：仅管理员可处理退款
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (bookingId == null) {
                return error("预约ID不能为空");
            }

            // 处理退款
            int result = courseBookingService.processRefund(bookingId);

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

    @Operation(summary = "课程下拉列表")
    @GetMapping("/courses")
    public Result<Object> getCourseList() {
        try {
            List<Course> courses = courseService.findAll(null, null, null, null, null, null, 1, 500);
            List<Map<String, Object>> courseList = courses.stream()
                    .map(course -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", course.getId());
                        item.put("courseName", course.getCourseName());
                        item.put("coachName", course.getCoachName());
                        item.put("courseDate", course.getCourseDate());
                        item.put("status", course.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(courseList);
        } catch (Exception e) {
            return error("获取课程列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取会员下拉列表（供选择会员使用，支持姓名关键词筛选）
     * @param keyword 可选，按会员姓名模糊搜索
     * @return 会员列表（仅包含ID、姓名、手机号）
     */
    @Operation(summary = "会员下拉列表")
    @GetMapping("/members")
    public Result<Object> getMemberList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            String nameOrPhone = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            List<Member> members = memberService.findByConditions(nameOrPhone, null, null, null, null, 1, 1000);
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

    @Data
    private static class CourseBookingCreateRequest {
        private Long memberId;
        @NotNull(message = "课程ID不能为空")
        private Long courseId;
        private java.math.BigDecimal orderAmount;
        private Integer status;
        private String remark;
    }

    @Data
    private static class CoachBookingStatusRequest {
        @NotNull(message = "预约ID不能为空")
        private Long id;
        @NotNull(message = "状态不能为空")
        private Integer status;
        private String remark;
    }
}
