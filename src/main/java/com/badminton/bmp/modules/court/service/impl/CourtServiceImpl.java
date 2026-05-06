package com.badminton.bmp.modules.court.service.impl;

import com.badminton.bmp.modules.court.cache.CourtEntityCache;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.court.service.CourtService;
import org.springframework.cache.annotation.CacheEvict;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.booking.mapper.BookingCourtMapper;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 场地业务实现类
 */
@Service
public class CourtServiceImpl implements CourtService {

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private CourtEntityCache courtEntityCache;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingCourtMapper bookingCourtMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Court findById(Long id) {
        Court court = courtEntityCache.getById(id);
        if (court == null) {
            return null;
        }
        
        // 数据范围过滤：根据角色检查访问权限
        if (SecurityUtils.isPresident()) {
            // PRESIDENT: 可以访问所有数据
            return court;
        } else if (SecurityUtils.isVenueManager()) {
            // VENUE_MANAGER: 只能访问自己场馆的数据
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId != null && currentVenueId.equals(court.getVenueId())) {
                return court;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // USER: 只能访问可用场地（status = 1）
            if (court.getStatus() != null && court.getStatus() == 1) {
                return court;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public List<Court> findAll(Long venueId, Integer status, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 1000) {
            size = 1000;
        }
        int offset = (page - 1) * size;
        // 数据范围过滤：根据当前用户角色调整查询参数
        if (SecurityUtils.isPresident()) {
            // 协会会长：使用传入的 venueId（可能为 null）
            return courtMapper.findAll(venueId, status, keyword, offset, size);
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能查询自己场馆的数据
            Long vId = SecurityUtils.getCurrentUserVenueId();
            return courtMapper.findAll(vId, status, keyword, offset, size);
        } else {
            // 普通用户：按所选场馆过滤，展示该场馆下全部场地（前端根据状态展示可用/不可用并禁止选择不可用）
            return courtMapper.findAll(venueId, status, keyword, offset, size);
        }
    }

    @Override
    public int count(Long venueId, Integer status, String keyword) {
        if (SecurityUtils.isPresident()) {
            return courtMapper.count(venueId, status, keyword);
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            return courtMapper.count(vId, status, keyword);
        } else {
            // 普通用户：按场馆过滤，统计该场馆下全部场地
            return courtMapper.count(venueId, status, keyword);
        }
    }

    @Override
    @Transactional
    public int add(Court court) {
        // 所有权/场馆归属校验：VM只能为自己场馆添加场地
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new RuntimeException("权限不足，只能为自己场馆添加场地");
                }
            } else {
                throw new RuntimeException("权限不足，只有管理员可以添加场地");
            }
        }
        // 验证场馆是否存在
        Venue venue = venueMapper.findById(court.getVenueId());
        if (venue == null) {
            throw new RuntimeException("所选场馆不存在");
        }

        // 验证同一场馆下场地编号是否已存在
        if (courtMapper.existsByVenueIdAndCode(court.getVenueId(), court.getCourtCode(), null)) {
            throw new RuntimeException("该场馆下已存在相同编号的场地");
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        court.setCreateTime(now);
        court.setUpdateTime(now);

        // 设置默认状态为空闲（1）
        if (court.getStatus() == null) {
            court.setStatus(1);
        }

        // 设置默认计费方式为按小时（HOUR）
        if (court.getBillingType() == null || court.getBillingType().trim().isEmpty()) {
            court.setBillingType("HOUR");
        }

        applyDefaultMarketingFlags(court);
        validateMarketingConfig(court);

        return courtMapper.insert(court);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "court", key = "#court.id")
    public int update(Court court) {
        // 查询原有场地信息
        Court existingCourt = courtEntityCache.getById(court.getId());
        if (existingCourt == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // 所有权/场馆归属校验：VM只能修改自己场馆的场地
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || !currentVenueId.equals(existingCourt.getVenueId())) {
                    throw new RuntimeException("权限不足，只能修改自己场馆的场地");
                }
                // VM不能修改venueId
                if (court.getVenueId() != null && !court.getVenueId().equals(existingCourt.getVenueId())) {
                    throw new RuntimeException("权限不足，不能修改场地的所属场馆");
                }
            } else {
                throw new RuntimeException("权限不足，只有管理员可以修改场地");
            }
        }

        // 如果修改了场馆，验证新场馆是否存在
        if (court.getVenueId() != null && !court.getVenueId().equals(existingCourt.getVenueId())) {
            Venue venue = venueMapper.findById(court.getVenueId());
            if (venue == null) {
                throw new RuntimeException("所选场馆不存在");
            }
        }

        // 验证同一场馆下场地编号是否已存在（排除自身）
        Long venueId = court.getVenueId() != null ? court.getVenueId() : existingCourt.getVenueId();
        String courtCode = court.getCourtCode() != null ? court.getCourtCode() : existingCourt.getCourtCode();
        if (courtMapper.existsByVenueIdAndCode(venueId, courtCode, court.getId())) {
            throw new RuntimeException("该场馆下已存在相同编号的场地");
        }

        // 保留原有值（如果未传入）
        if (court.getVenueId() == null) {
            court.setVenueId(existingCourt.getVenueId());
        }
        if (court.getCourtCode() == null || court.getCourtCode().trim().isEmpty()) {
            court.setCourtCode(existingCourt.getCourtCode());
        }
        if (court.getCourtName() == null) {
            court.setCourtName(existingCourt.getCourtName());
        }
        if (court.getBillingType() == null) {
            court.setBillingType(existingCourt.getBillingType());
        }
        if (court.getPricePerHour() == null) {
            court.setPricePerHour(existingCourt.getPricePerHour());
        }
        if (court.getPackagePricePerHour() == null) {
            court.setPackagePricePerHour(existingCourt.getPackagePricePerHour());
        }
        if (court.getSharedPricePerHour() == null) {
            court.setSharedPricePerHour(existingCourt.getSharedPricePerHour());
        }
        if (court.getSharedPricePerTime() == null) {
            court.setSharedPricePerTime(existingCourt.getSharedPricePerTime());
        }
        if (court.getEnablePackageHour() == null) {
            court.setEnablePackageHour(existingCourt.getEnablePackageHour());
        }
        if (court.getEnableSharedHour() == null) {
            court.setEnableSharedHour(existingCourt.getEnableSharedHour());
        }
        if (court.getEnableSharedTime() == null) {
            court.setEnableSharedTime(existingCourt.getEnableSharedTime());
        }
        if (court.getStatus() == null) {
            court.setStatus(existingCourt.getStatus());
        }

        applyDefaultMarketingFlags(court);
        validateMarketingConfig(court);

        // 设置更新时间
        court.setUpdateTime(LocalDateTime.now());

        return courtMapper.update(court);
    }

    @Override
    @CacheEvict(cacheNames = "court", key = "#id")
    public int deleteById(Long id) {
        // 验证场地是否存在
        Court court = courtEntityCache.getById(id);
        if (court == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // 所有权/场馆归属校验：VM只能删除自己场馆的场地
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new RuntimeException("权限不足，只能删除自己场馆的场地");
                }
            } else {
                throw new RuntimeException("权限不足，只有管理员可以删除场地");
            }
        }

        // 检查是否有未完成的预约
        List<Integer> activeStatuses = Arrays.asList(1, 2, 3); // 待支付、已支付、进行中
        int activeBookingCount = bookingCourtMapper.countByCourtIdAndStatusIn(id, activeStatuses);
        if (activeBookingCount > 0) {
            throw new RuntimeException(
                String.format("该场地存在%d条未完成的预约记录，无法删除。请先处理这些预约记录后再试。", activeBookingCount)
            );
        }

        // 检查是否有未完成的课程
        List<Integer> activeCourseStatuses = Arrays.asList(1, 2); // 报名中、进行中
        int activeCourseCount = courseMapper.countByCourtIdAndStatusIn(id, activeCourseStatuses);
        if (activeCourseCount > 0) {
            throw new RuntimeException(
                String.format("该场地存在%d条未完成的课程，无法删除。请先处理这些课程后再试。", activeCourseCount)
            );
        }
        
        return courtMapper.deleteById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // 验证场地是否存在
        Court court = courtMapper.findById(id);
        if (court == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的场地状态
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new RuntimeException("权限不足，只能修改自己场馆的场地状态");
                }
            } else {
                // USER: 不能修改场地状态
                throw new RuntimeException("权限不足，只有管理员可以修改场地状态");
            }
        }

        // 验证状态值是否有效（0-维护中，1-空闲，2-预约中，3-使用中）
        if (status == null || status < 0 || status > 3) {
            throw new RuntimeException("无效的状态值，必须是0（维护中）、1（空闲）、2（预约中）或3（使用中）");
        }

        return courtMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 数据范围过滤：根据角色调整统计范围
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else if (!SecurityUtils.isPresident()) {
            // USER: 只统计可用场地
            // 这里简化处理，USER只能看到可用场地数量
            venueFilter = null; // 但需要按status过滤
        }
        
        // 获取总场地数（根据角色过滤）
        int total;
        if (venueFilter != null) {
            total = courtMapper.countByVenueId(venueFilter);
        } else if (!SecurityUtils.isPresident()) {
            // USER: 只统计可用场地
            total = courtMapper.count(null, 1, null);
        } else {
            total = courtMapper.countAll();
        }
        statistics.put("total", total);

        // 获取各状态的场地数量（根据角色过滤）
        List<Map<String, Object>> statusCounts;
        if (venueFilter != null) {
            // VM: 只统计自己场馆的场地
            statusCounts = new ArrayList<>();
            for (int status = 0; status <= 3; status++) {
                int count = courtMapper.count(venueFilter, status, null);
                if (count > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("status", status);
                    item.put("count", count);
                    statusCounts.add(item);
                }
            }
        } else if (!SecurityUtils.isPresident()) {
            // USER: 只统计可用场地
            statusCounts = new ArrayList<>();
            int availableCount = courtMapper.count(null, 1, null);
            if (availableCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("status", 1);
                item.put("count", availableCount);
                statusCounts.add(item);
            }
        } else {
            // PRESIDENT: 统计所有场地
            statusCounts = courtMapper.countByStatus();
        }
        
        // 初始化各状态数量
        int maintenance = 0;  // 维护中（0）
        int available = 0;    // 空闲（1）
        int reserved = 0;     // 预约中（2）
        int inUse = 0;        // 使用中（3）

        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");
            
            if (statusObj != null && countObj != null) {
                int statusValue = ((Number) statusObj).intValue();
                int countValue = ((Number) countObj).intValue();
                
                switch (statusValue) {
                    case 0:
                        maintenance = countValue;
                        break;
                    case 1:
                        available = countValue;
                        break;
                    case 2:
                        reserved = countValue;
                        break;
                    case 3:
                        inUse = countValue;
                        break;
                }
            }
        }

        statistics.put("maintenance", maintenance);
        statistics.put("available", available);
        statistics.put("reserved", reserved);
        statistics.put("inUse", inUse);

        // 今日新增场地数（仅管理员维度统计）
        int newToday = 0;
        if (SecurityUtils.isPresident() || SecurityUtils.isVenueManager()) {
            newToday = courtMapper.countCreatedOnDate(venueFilter, LocalDate.now());
        }
        statistics.put("newToday", newToday);

        return statistics;
    }

    @Override
    public int countByVenueId(Long venueId) {
        return courtMapper.countByVenueId(venueId);
    }

    @Override
    public List<CourtBookingUserDTO> getTodayBookingUsers(Long courtId, String date) {
        // 敏感信息：只有管理员可以访问
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new RuntimeException("权限不足，只有管理员可以查看预约用户信息");
        }
        
        // 验证场地是否存在
        Court court = courtMapper.findById(courtId);
        if (court == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // VENUE_MANAGER: 只能查看自己场馆的场地预约信息
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId == null || !currentVenueId.equals(court.getVenueId())) {
                throw new RuntimeException("权限不足，只能查看自己场馆的场地预约信息");
            }
        }

        // 查询指定日期的预约信息，未传或无效时使用服务器当前日期
        LocalDate queryDate = parseDateOrToday(date);
        List<Map<String, Object>> bookingList = courtMapper.findTodayBookingUsers(courtId, queryDate);

        // 转换为DTO并进行姓名脱敏
        List<CourtBookingUserDTO> result = new ArrayList<>();
        for (Map<String, Object> booking : bookingList) {
            CourtBookingUserDTO dto = new CourtBookingUserDTO();
            
            // 设置预约ID（MySQL TINYINT/BIGINT 可能被 JDBC 映射为 Number 或 Boolean，需安全转换）
            Object bookingIdObj = booking.get("booking_id");
            if (bookingIdObj != null) {
                dto.setBookingId(toLong(bookingIdObj));
            }

            // 设置姓名（脱敏处理）
            String memberName = (String) booking.get("member_name");
            dto.setMemberName(maskName(memberName));

            // 设置会员类型
            dto.setMemberType((String) booking.get("member_type"));

            // 设置会员等级（TINYINT(1) 可能被映射为 Boolean）
            Object memberLevelObj = booking.get("member_level");
            if (memberLevelObj != null) {
                dto.setMemberLevel(toInt(memberLevelObj));
            }

            // 设置开始时间
            Object startTimeObj = booking.get("start_time");
            dto.setStartTime(formatTime(startTimeObj));

            // 设置结束时间
            Object endTimeObj = booking.get("end_time");
            dto.setEndTime(formatTime(endTimeObj));

            // 设置状态（TINYINT(1) 可能被映射为 Boolean）
            Object statusObj = booking.get("status");
            if (statusObj != null) {
                int status = toInt(statusObj);
                dto.setStatus(status);
                dto.setStatusText(getBookingStatusText(status));
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    public Map<Long, Integer> getTodayBookingCounts(List<Long> courtIds, String date) {
        Map<Long, Integer> result = new HashMap<>();
        
        if (courtIds == null || courtIds.isEmpty()) {
            return result;
        }

        LocalDate queryDate = parseDateOrToday(date);
        List<Map<String, Object>> counts = courtMapper.countTodayBookingsByCourtIds(courtIds, queryDate);

        for (Map<String, Object> item : counts) {
            Object courtIdObj = item.get("court_id");
            Object countObj = item.get("booking_count");
            
            if (courtIdObj != null && countObj != null) {
                Long courtId = toLong(courtIdObj);
                Integer count = toInt(countObj);
                result.put(courtId, count);
            }
        }

        return result;
    }

    /**
     * 安全转换为 int。MySQL TINYINT(1) 可能被 JDBC 映射为 Boolean，需兼容处理。
     */
    private int toInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        }
        return 0;
    }

    /**
     * 安全转换为 long。兼容 Number 与 Boolean（Boolean 转为 0/1）。
     */
    private long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1L : 0L;
        }
        return 0L;
    }

    /**
     * 解析日期字符串，无效或空则返回服务器当前日期
     * 用于「当天使用者」查询，避免时区导致与用户本地日期不一致
     */
    private LocalDate parseDateOrToday(String date) {
        if (date == null || date.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    /**
     * 姓名脱敏处理
     * 规则：
     * - 2字姓名：保留首字，其余用*替代（张三 -> 张*）
     * - 3字及以上姓名：保留首尾字，中间用*替代（张三丰 -> 张*丰）
     * @param name 原始姓名
     * @return 脱敏后的姓名
     */
    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        
        int length = name.length();
        
        if (length == 1) {
            return name;
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else {
            // 3字及以上：保留首尾，中间用*替代
            StringBuilder sb = new StringBuilder();
            sb.append(name.charAt(0));
            for (int i = 1; i < length - 1; i++) {
                sb.append("*");
            }
            sb.append(name.charAt(length - 1));
            return sb.toString();
        }
    }

    /**
     * 格式化时间对象为 HH:mm 格式字符串
     * @param timeObj 时间对象（可能是 LocalTime、Time 或 String）
     * @return 格式化后的时间字符串
     */
    private String formatTime(Object timeObj) {
        if (timeObj == null) {
            return "";
        }
        
        if (timeObj instanceof LocalTime) {
            LocalTime time = (LocalTime) timeObj;
            return String.format("%02d:%02d", time.getHour(), time.getMinute());
        } else if (timeObj instanceof Time) {
            Time time = (Time) timeObj;
            return time.toString().substring(0, 5); // 取 HH:mm 部分
        } else if (timeObj instanceof String) {
            String timeStr = (String) timeObj;
            if (timeStr.length() >= 5) {
                return timeStr.substring(0, 5);
            }
            return timeStr;
        }
        
        return timeObj.toString();
    }

    /**
     * 获取预约状态文本
     * @param status 状态值
     * @return 状态文本
     */
    private String getBookingStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "进行中";
            case 4: return "已完成";
            default: return "未知";
        }
    }

    private void applyDefaultMarketingFlags(Court court) {
        if (court.getEnablePackageHour() == null) {
            court.setEnablePackageHour(Boolean.TRUE);
        }
        if (court.getEnableSharedHour() == null) {
            court.setEnableSharedHour(Boolean.TRUE);
        }
        if (court.getEnableSharedTime() == null) {
            court.setEnableSharedTime(Boolean.TRUE);
        }
    }

    private void validateMarketingConfig(Court court) {
        boolean enablePackageHour = Boolean.TRUE.equals(court.getEnablePackageHour());
        boolean enableSharedHour = Boolean.TRUE.equals(court.getEnableSharedHour());
        boolean enableSharedTime = Boolean.TRUE.equals(court.getEnableSharedTime());

        if (!enablePackageHour && !enableSharedHour && !enableSharedTime) {
            throw new RuntimeException("至少需要开放一种营销方式");
        }
        if (enablePackageHour && (court.getPackagePricePerHour() == null || court.getPackagePricePerHour().doubleValue() <= 0)) {
            throw new RuntimeException("开放包场按小时后，包场每小时价格必须大于0");
        }
        if (enableSharedHour && (court.getSharedPricePerHour() == null || court.getSharedPricePerHour().doubleValue() <= 0)) {
            throw new RuntimeException("开放拼场按小时后，拼场每小时价格必须大于0");
        }
        if (enableSharedTime && (court.getSharedPricePerTime() == null || court.getSharedPricePerTime().doubleValue() <= 0)) {
            throw new RuntimeException("开放拼场按次后，拼场按次价格必须大于0");
        }
    }

    @Override
    public void recomputeCourtStatus(Long courtId) {
        if (courtId == null) {
            return;
        }
        Court court = courtMapper.findById(courtId);
        if (court == null) {
            return;
        }
        // 维护中(0) 不随预约变更
        if (court.getStatus() != null && court.getStatus() == 0) {
            return;
        }
        List<BookingCourt> activeBookings = bookingCourtMapper.findActiveBookingDetailsByCourtId(courtId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        // 是否有“进行中”且当前时刻在预约时段内的预约
        for (BookingCourt b : activeBookings) {
            if (b.getBookingStatus() != null && b.getBookingStatus() == 3
                    && today.equals(b.getBookingDate())
                    && b.getStartTime() != null && b.getEndTime() != null
                    && !now.isBefore(b.getStartTime()) && now.isBefore(b.getEndTime())) {
                courtMapper.updateStatus(courtId, 3); // 使用中
                return;
            }
        }
        if (!activeBookings.isEmpty()) {
            courtMapper.updateStatus(courtId, 2); // 预约中
        } else {
            courtMapper.updateStatus(courtId, 1); // 空闲
        }
    }

    @Override
    public List<Map<String, Object>> getTimelineToday(String date) {
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        List<Court> courts = courtMapper.findAll(venueFilter, null, null, 0, 20);
        LocalDate queryDate = parseDateOrToday(date);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Court court : courts) {
            Map<String, Object> item = new HashMap<>();
            item.put("courtId", court.getId());
            item.put("id", court.getId());
            item.put("courtName", court.getCourtName() != null ? court.getCourtName() : court.getCourtCode());
            item.put("name", court.getCourtName() != null ? court.getCourtName() : court.getCourtCode());

            List<Map<String, Object>> timeSlots = new ArrayList<>();
            List<Map<String, Object>> bookingList = courtMapper.findTodayBookingUsers(court.getId(), queryDate);
            boolean[] busy = new boolean[24];
            for (Map<String, Object> row : bookingList) {
                Object statusObj = row.get("status");
                int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : 0;
                if (status != 0 && status != 1) {
                    int startH = parseHourFromMap(row.get("start_time"));
                    int endH = parseHourFromMap(row.get("end_time"));
                    if (endH <= startH) endH = startH + 1;
                    for (int h = startH; h < endH && h < 24; h++) {
                        busy[h] = true;
                    }
                }
            }
            int i = 0;
            while (i < 24) {
                String slotStatus = busy[i] ? "busy" : "free";
                int j = i + 1;
                while (j < 24 && busy[j] == busy[i]) j++;
                Map<String, Object> slot = new HashMap<>();
                slot.put("status", slotStatus);
                slot.put("duration", j - i);
                timeSlots.add(slot);
                i = j;
            }
            item.put("timeSlots", timeSlots);
            item.put("slots", timeSlots);
            result.add(item);
        }
        return result;
    }

    private int parseHourFromMap(Object timeObj) {
        if (timeObj == null) return 0;
        String s = timeObj.toString().trim();
        if (s.length() >= 2) {
            try {
                return Integer.parseInt(s.substring(0, 2));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

}
