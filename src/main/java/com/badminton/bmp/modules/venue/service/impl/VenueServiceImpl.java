package com.badminton.bmp.modules.venue.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.modules.venue.cache.VenueEntityCache;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.badminton.bmp.modules.venue.service.VenueStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 场馆业务实现类
 * 参照UserServiceImpl实现方式
 */
@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private VenueEntityCache venueEntityCache;

    @Autowired
    private VenueStatusLogService venueStatusLogService;

    @Override
    public Venue findById(Long id) {
        Venue venue = venueEntityCache.getById(id);
        if (venue == null) {
            return null;
        }

        // 数据范围过滤：场馆信息读取规则
        if (com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            return venue;
        } else if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(venue.getId())) {
                return venue;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：允许查看场馆详情
            return venue;
        }
    }

    @Override
    public List<Venue> findAll() {
        // 数据范围过滤：场馆管理者只能查看自己归属的场馆
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                return Collections.emptyList();
            }
            Venue v = venueMapper.findById(vId);
            return (v != null) ? Collections.singletonList(v) : Collections.emptyList();
        }
        return venueMapper.findAll();
    }

    @Override
    public List<Venue> findByVenueNameOrAddress(String venueName, String address, Integer status, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        // 数据范围过滤：场馆管理者只能查看自己归属的场馆
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (venueFilter == null) {
                return Collections.emptyList();
            }
        }

        int offset = (page - 1) * size;
        return venueMapper.findByVenueNameOrAddress(venueName, address, status, venueFilter, offset, size);
    }

    @Override
    public int countByVenueNameOrAddress(String venueName, String address, Integer status) {
        // 数据范围过滤：场馆管理者只能统计自己归属的场馆
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (venueFilter == null) {
                return 0;
            }
        }
        return venueMapper.countByVenueNameOrAddress(venueName, address, status, venueFilter);
    }

    @Override
    public int add(Venue venue) {
        // 设置创建时间
        if (venue.getCreateTime() == null) {
            venue.setCreateTime(LocalDateTime.now());
        }
        // 设置更新时间
        if (venue.getUpdateTime() == null) {
            venue.setUpdateTime(LocalDateTime.now());
        }

        // 设置默认状态为营业中（1）
        if (venue.getStatus() == null) {
            venue.setStatus(1);
        }

        return venueMapper.insert(venue);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "venue", key = "#venue.id")
    public int update(Venue venue) {
        // 先查询原有场馆信息
        Venue existingVenue = venueEntityCache.getById(venue.getId());
        if (existingVenue == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }

        // 权限校验：VENUE_MANAGER只能更新自己管理的场馆
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新场馆信息");
            }
            if (!vId.equals(venue.getId())) {
                throw new BusinessException("权限不足：只能更新自己管理的场馆");
            }
        } else if (!com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以更新场馆信息");
        }

        if (existingVenue != null) {
            Integer oldStatus = existingVenue.getStatus();
            
            // 确保状态不为空，使用原有状态
            if (venue.getStatus() == null) {
                venue.setStatus(existingVenue.getStatus());
            }
            // 确保图片不为空（null），使用原有图片；允许传空字符串表示清空
            if (venue.getVenueImage() == null) {
                venue.setVenueImage(existingVenue.getVenueImage());
            }
            // 确保场馆名称不为空，使用原有名称
            if (venue.getVenueName() == null || venue.getVenueName().trim().isEmpty()) {
                venue.setVenueName(existingVenue.getVenueName());
            }
            // 设置更新时间
            venue.setUpdateTime(LocalDateTime.now());

            // 如果状态发生变化，记录日志
            Integer newStatus = venue.getStatus();
            if (oldStatus != null && newStatus != null && !oldStatus.equals(newStatus)) {
                // 获取当前操作人信息
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Long operatorId = null;
                String operatorName = null;
                if (authentication != null && authentication.isAuthenticated()) {
                    operatorName = authentication.getName();
                    // 尝试从principal中获取用户ID（如果principal是UserDetails或自定义对象）
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                        // 可以进一步扩展获取ID
                    }
                }
                venueStatusLogService.logStatusChange(
                    venue.getId(), 
                    oldStatus, 
                    newStatus, 
                    operatorId, 
                    operatorName, 
                    "状态变更：" + getStatusText(oldStatus) + " -> " + getStatusText(newStatus)
                );
                
                // 如果场馆状态变更为"关闭"（0），自动将该场馆下所有场地状态改为"维护中"（0）
                if (newStatus != null && newStatus == 0) {
                    updateCourtsStatusByVenueId(venue.getId(), 0);
                }
            }
        }
        return venueMapper.update(venue);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "关闭";
            case 1: return "营业中";
            case 2: return "暂停";
            default: return "未知";
        }
    }

    @Override
    @CacheEvict(cacheNames = "venue", key = "#id")
    public int deleteById(Long id) {
        // 先查询场馆是否存在
        Venue venue = venueEntityCache.getById(id);
        if (venue == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }

        // 权限校验：VENUE_MANAGER只能删除自己管理的场馆
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法删除场馆");
            }
            if (!vId.equals(id)) {
                throw new BusinessException("权限不足：只能删除自己管理的场馆");
            }
        } else if (!com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以删除场馆");
        }

        return venueMapper.deleteById(id);
    }

    @Override
    public int countCourtsByVenueId(Long venueId) {
        return venueMapper.countCourtsByVenueId(venueId);
    }

    @Override
    public int countTournamentsByVenueId(Long venueId) {
        return venueMapper.countTournamentsByVenueId(venueId);
    }

    @Override
    public int updateCourtsStatusByVenueId(Long venueId, Integer status) {
        return venueMapper.updateCourtsStatusByVenueId(venueId, status);
    }

    @Override
    public Map<String, Object> getStatistics() {
        // 数据范围过滤：场馆管理者只能统计自己归属的场馆
        List<Venue> allVenues;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                Map<String, Object> emptyStats = new HashMap<>();
                emptyStats.put("total", 0);
                emptyStats.put("operating", 0);
                emptyStats.put("paused", 0);
                emptyStats.put("closed", 0);
                return emptyStats;
            }
            Venue v = venueMapper.findById(vId);
            allVenues = (v != null) ? Collections.singletonList(v) : Collections.emptyList();
        } else {
            allVenues = venueMapper.findAll();
        }

        int total = allVenues.size();
        int operating = 0;  // 营业中（1）
        int paused = 0;     // 暂停（2）
        int closed = 0;     // 关闭（0）

        for (Venue venue : allVenues) {
            Integer status = venue.getStatus();
            if (status != null) {
                switch (status) {
                    case 0:
                        closed++;
                        break;
                    case 1:
                        operating++;
                        break;
                    case 2:
                        paused++;
                        break;
                }
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("operating", operating);
        statistics.put("paused", paused);
        statistics.put("closed", closed);

        // Dashboard 雷达图/堆叠图：按场馆返回指标与收入结构（当前为占位 0，后续可接入真实汇总）
        List<Map<String, Object>> venueMetrics = new ArrayList<>();
        List<Map<String, Object>> venueRevenue = new ArrayList<>();
        for (Venue v : allVenues) {
            Map<String, Object> m = new HashMap<>();
            m.put("venueId", v.getId());
            m.put("id", v.getId());
            m.put("venueName", v.getVenueName());
            m.put("name", v.getVenueName());
            m.put("utilizationRate", 0);
            m.put("utilization", 0);
            m.put("bookingRate", 0);
            m.put("booking", 0);
            m.put("satisfactionRate", 0);
            m.put("satisfaction", 0);
            m.put("revenueContribution", 0);
            m.put("revenue", 0);
            m.put("turnoverRate", 0);
            m.put("turnover", 0);
            m.put("repeatRate", 0);
            m.put("repeat", 0);
            venueMetrics.add(m);

            Map<String, Object> r = new HashMap<>();
            r.put("venueName", v.getVenueName());
            r.put("name", v.getVenueName());
            r.put("courtRevenue", 0);
            r.put("court", 0);
            r.put("courseRevenue", 0);
            r.put("course", 0);
            r.put("tournamentRevenue", 0);
            r.put("tournament", 0);
            r.put("otherRevenue", 0);
            r.put("other", 0);
            venueRevenue.add(r);
        }
        statistics.put("venueMetrics", venueMetrics);
        statistics.put("venueRevenue", venueRevenue);

        return statistics;
    }
}
