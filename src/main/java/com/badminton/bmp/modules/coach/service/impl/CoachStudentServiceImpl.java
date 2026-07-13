package com.badminton.bmp.modules.coach.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ServiceUnavailableException;
import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery;
import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import com.badminton.bmp.modules.coach.service.CoachStudentService;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListItemVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListResponseVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentPageVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import com.badminton.bmp.modules.coach.support.CoachStudentLastSuccessCache;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CoachStudentServiceImpl implements CoachStudentService {
    private static final int RELATION_RETENTION_DAYS = 180;
    private static final Set<String> SORT_FIELDS = Set.of(
            "lastLessonTime", "attendanceRate", "totalPaidAmount", "totalHours");

    @Autowired
    private CoachStudentMapper coachStudentMapper;
    @Autowired
    private CoachStudentRelationService relationService;
    @Autowired
    private CoachStudentLastSuccessCache lastSuccessCache;
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Value("${coach.risk-threshold.attendance-rate:70}")
    private int riskAttendanceRate = 70;
    @Value("${coach.risk-threshold.inactive-days:14}")
    private int riskInactiveDays = 14;

    @Override
    public CoachStudentListResponseVO listStudents(Long coachId, CoachStudentListQuery query) {
        requireCoachId(coachId);
        CoachStudentListQuery normalized = validateListQuery(query);
        String key = "list:" + coachId + ':' + normalized;
        return queryWithFallback(key, "学员列表查询暂时不可用，请稍后重试", () -> {
            int offset = (normalized.getPage() - 1) * normalized.getSize();
            List<CoachStudentListItemVO> students = coachStudentMapper.findStudents(
                    coachId, normalized, RELATION_RETENTION_DAYS, riskAttendanceRate, riskInactiveDays, offset);
            if (students == null) students = List.of();
            students.forEach(this::applyRisk);
            int total = coachStudentMapper.countStudents(
                    coachId, normalized, RELATION_RETENTION_DAYS, riskAttendanceRate, riskInactiveDays);
            Map<String, Object> summary = coachStudentMapper.summarizeStudents(coachId, RELATION_RETENTION_DAYS);

            CoachStudentListResponseVO response = new CoachStudentListResponseVO();
            response.setPage(page(students, total, normalized.getPage(), normalized.getSize()));
            response.setTotalStudents(number(summary, "totalStudents", total));
            response.setTodayStudents(number(summary, "todayStudents", 0));
            response.setRiskStudents(coachStudentMapper.countRiskStudents(
                    coachId, RELATION_RETENTION_DAYS, riskAttendanceRate, riskInactiveDays));
            BigDecimal pageAverage = students.isEmpty() ? BigDecimal.ZERO : students.stream()
                    .map(item -> item.getAttendanceRate() == null ? BigDecimal.ZERO : item.getAttendanceRate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(students.size()), 2, RoundingMode.HALF_UP);
            response.setAverageAttendanceRate(decimal(summary, "averageAttendanceRate", pageAverage));
            return response;
        });
    }

    @Override
    public CoachStudentDetailVO getStudentDetail(Long coachId, Long memberId) {
        ensureCoachCanViewStudent(coachId, memberId);
        String key = "detail:" + coachId + ':' + memberId;
        return queryWithFallback(key, "学员详情查询暂时不可用，请稍后重试", () -> {
            CoachStudentDetailVO detail = coachStudentMapper.findStudentDetail(coachId, memberId);
            if (detail == null) throw new BusinessException(404, "学员不存在");
            detail.setRisk(isRisk(detail.getAttendedCount(), detail.getAbsentCount(),
                    detail.getAttendanceRate(), detail.getLastLessonTime()));
            return detail;
        });
    }

    @Override
    public List<CoachStudentProgressVO> getStudentProgress(Long coachId, Long memberId) {
        ensureCoachCanViewStudent(coachId, memberId);
        String key = "progress:" + coachId + ':' + memberId;
        List<CoachStudentProgressVO> cached = cacheValue("coachStudentProgress", key);
        if (cached != null) return cached;
        List<CoachStudentProgressVO> result = queryWithFallback(key, "训练进度查询暂时不可用，请稍后重试", () -> {
            List<CoachStudentProgressVO> rows = coachStudentMapper.findProgress(coachId, memberId);
            return rows == null ? List.of() : rows;
        });
        putCacheValue("coachStudentProgress", key, result);
        return result;
    }

    @Override
    public CoachStudentPageVO<CoachStudentScheduleVO> getStudentSchedule(
            Long coachId, Long memberId, CoachStudentScheduleQuery query) {
        ensureCoachCanViewStudent(coachId, memberId);
        CoachStudentScheduleQuery normalized = validateScheduleQuery(query);
        String key = "schedule:" + coachId + ':' + memberId + ':' + normalized;
        return queryWithFallback(key, "课程安排查询暂时不可用，请稍后重试", () -> {
            int offset = (normalized.getPage() - 1) * normalized.getSize();
            List<CoachStudentScheduleVO> rows = coachStudentMapper.findSchedule(
                    coachId, memberId, normalized, offset);
            int total = coachStudentMapper.countSchedule(coachId, memberId, normalized);
            return page(rows == null ? List.of() : rows, total, normalized.getPage(), normalized.getSize());
        });
    }

    @Override
    public CoachStudentPageVO<CoachStudentAttendanceVO> getStudentAttendance(
            Long coachId, Long memberId, CoachStudentScheduleQuery query) {
        ensureCoachCanViewStudent(coachId, memberId);
        CoachStudentScheduleQuery normalized = validateScheduleQuery(query);
        String key = "attendance:" + coachId + ':' + memberId + ':' + normalized;
        CoachStudentPageVO<CoachStudentAttendanceVO> cached = cacheValue("coachStudentAttendance", key);
        if (cached != null) return cached;
        CoachStudentPageVO<CoachStudentAttendanceVO> result = queryWithFallback(
                key, "出勤记录查询暂时不可用，请稍后重试", () -> {
            int offset = (normalized.getPage() - 1) * normalized.getSize();
            List<CoachStudentAttendanceVO> rows = coachStudentMapper.findAttendance(
                    coachId, memberId, normalized, offset);
            int total = coachStudentMapper.countAttendance(coachId, memberId, normalized);
            return page(
                    rows == null ? List.of() : rows, total, normalized.getPage(), normalized.getSize());
        });
        putCacheValue("coachStudentAttendance", key, result);
        return result;
    }

    @Override
    public CoachStudentPageVO<CoachStudentConsumeRecordVO> getStudentConsumeRecords(
            Long coachId, Long memberId, int page, int size) {
        ensureCoachCanViewStudent(coachId, memberId);
        validatePage(page, size);
        String key = "consume:" + coachId + ':' + memberId + ':' + page + ':' + size;
        return queryWithFallback(key, "消费记录查询暂时不可用，请稍后重试", () -> {
            List<CoachStudentConsumeRecordVO> rows = coachStudentMapper.findConsumeRecordsForCoach(
                    coachId, memberId, (page - 1) * size, size);
            int total = coachStudentMapper.countConsumeRecordsForCoach(coachId, memberId);
            return page(rows == null ? List.of() : rows, total, page, size);
        });
    }

    private void ensureCoachCanViewStudent(Long coachId, Long memberId) {
        requireCoachId(coachId);
        if (memberId == null || memberId <= 0) throw new BusinessException("学员ID必须大于0");
        if (!relationService.canCoachViewStudent(coachId, memberId)) {
            throw new AccessDeniedException("无权查看该学员信息");
        }
    }

    private CoachStudentListQuery validateListQuery(CoachStudentListQuery query) {
        CoachStudentListQuery value = query == null ? new CoachStudentListQuery() : query;
        validatePage(value.getPage(), value.getSize());
        if (value.getKeyword() != null) {
            value.setKeyword(value.getKeyword().trim());
            if (value.getKeyword().length() > 50) throw new BusinessException("关键词最长50个字符");
            if (value.getKeyword().isEmpty()) value.setKeyword(null);
        }
        if (value.getMemberType() != null && !Set.of("NORMAL", "MEMBER").contains(value.getMemberType())) {
            throw new BusinessException("会员类型不合法");
        }
        if (!SORT_FIELDS.contains(value.getOrderBy())) throw new BusinessException("排序字段不合法");
        value.setOrderDirection(value.getOrderDirection() == null ? "DESC" : value.getOrderDirection().toUpperCase());
        if (!Set.of("ASC", "DESC").contains(value.getOrderDirection())) throw new BusinessException("排序方向不合法");
        return value;
    }

    private CoachStudentScheduleQuery validateScheduleQuery(CoachStudentScheduleQuery query) {
        CoachStudentScheduleQuery value = query == null ? new CoachStudentScheduleQuery() : query;
        validatePage(value.getPage(), value.getSize());
        if (value.getStatus() != null && (value.getStatus() < 0 || value.getStatus() > 4)) {
            throw new BusinessException("预约状态不合法");
        }
        if (value.getAttendanceStatus() != null
                && (value.getAttendanceStatus() < 0 || value.getAttendanceStatus() > 3)) {
            throw new BusinessException("考勤状态不合法");
        }
        if (value.getStartDate() != null && value.getEndDate() != null) {
            if (value.getStartDate().isAfter(value.getEndDate())) throw new BusinessException("开始日期不能晚于结束日期");
            if (ChronoUnit.DAYS.between(value.getStartDate(), value.getEndDate()) > 180) {
                throw new BusinessException("日期跨度最多180天");
            }
        }
        return value;
    }

    private void validatePage(int page, int size) {
        if (page < 1) throw new BusinessException("页码不能小于1");
        if (size < 1 || size > 100) throw new BusinessException("每页数量必须在1到100之间");
    }

    private void requireCoachId(Long coachId) {
        if (coachId == null || coachId <= 0) throw new BusinessException("未绑定教练档案");
    }

    private void applyRisk(CoachStudentListItemVO item) {
        item.setRisk(isRisk(item.getAttendedCount(), item.getAbsentCount(),
                item.getAttendanceRate(), item.getLastLessonTime()));
    }

    private boolean isRisk(Integer attended, Integer absent, BigDecimal rate, LocalDateTime lastLesson) {
        int denominator = (attended == null ? 0 : attended) + (absent == null ? 0 : absent);
        if (denominator > 0 && (rate == null || rate.compareTo(BigDecimal.valueOf(riskAttendanceRate)) < 0)) {
            return true;
        }
        return lastLesson != null && lastLesson.toLocalDate().isBefore(LocalDate.now().minusDays(riskInactiveDays));
    }

    private int number(Map<String, Object> map, String key, int fallback) {
        if (map == null) return fallback;
        Object value = map.get(key);
        if (value == null) value = map.get(key.toUpperCase());
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private BigDecimal decimal(Map<String, Object> map, String key, BigDecimal fallback) {
        if (map == null) return fallback;
        Object value = map.get(key);
        if (value == null) value = map.get(key.toUpperCase());
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return fallback;
    }

    private <T> T queryWithFallback(String key, String unavailableMessage, Supplier<T> query) {
        try {
            T result = query.get();
            if (lastSuccessCache != null) lastSuccessCache.remember(key, result);
            return result;
        } catch (BusinessException | AccessDeniedException expected) {
            throw expected;
        } catch (RuntimeException databaseFailure) {
            Object cached = lastSuccessCache == null ? null : lastSuccessCache.get(key);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                T stale = (T) cached;
                return stale;
            }
            throw new ServiceUnavailableException(unavailableMessage);
        }
    }

    private <T> T cacheValue(String cacheName, String key) {
        if (cacheManager == null) return null;
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return null;
        Object value = cache.get(key, Object.class);
        if (value == null) return null;
        @SuppressWarnings("unchecked")
        T typed = (T) value;
        return typed;
    }

    private void putCacheValue(String cacheName, String key, Object value) {
        if (cacheManager == null || value == null) return;
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.put(key, value);
    }

    private <T> CoachStudentPageVO<T> page(List<T> rows, int total, int page, int size) {
        return new CoachStudentPageVO<>(rows, total, page, size, (total + size - 1) / size);
    }
}
