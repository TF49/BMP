package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.dto.AgentVenueDTO;
import com.badminton.bmp.modules.agent.service.AgentVenueToolService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link AgentVenueToolService} 默认实现。
 *
 * <p>复用 {@link VenueService} 读取场馆数据；场馆管理员只能看到自己所属场馆，
 * 其余角色可浏览全部场馆。所有入参经过白名单与边界校验。</p>
 */
@Service
public class AgentVenueToolServiceImpl implements AgentVenueToolService {

    private static final int MAX_LIMIT = 50;
    private static final Set<Integer> ALLOWED_STATUS = Set.of(0, 1, 2);

    private final VenueService venueService;

    public AgentVenueToolServiceImpl(VenueService venueService) {
        this.venueService = venueService;
    }

    @Override
    public List<AgentVenueDTO> listVenues(Integer status, int limit) {
        if (status != null && !ALLOWED_STATUS.contains(status)) {
            throw new BusinessException(400, "场馆状态参数不合法");
        }
        int effectiveLimit = clampLimit(limit);

        List<Venue> source = resolveScopedVenues();
        List<AgentVenueDTO> result = new ArrayList<>();
        for (Venue venue : source) {
            if (venue == null) {
                continue;
            }
            if (status != null && !status.equals(venue.getStatus())) {
                continue;
            }
            result.add(toDto(venue));
            if (result.size() >= effectiveLimit) {
                break;
            }
        }
        return result;
    }

    /** 场馆管理员限定本场馆，其余角色可浏览全部场馆。 */
    private List<Venue> resolveScopedVenues() {
        if (SecurityUtils.isVenueManager()) {
            Long venueId = SecurityUtils.getCurrentUserVenueId();
            if (venueId == null) {
                return new ArrayList<>();
            }
            Venue venue = venueService.findById(venueId);
            List<Venue> scoped = new ArrayList<>();
            if (venue != null) {
                scoped.add(venue);
            }
            return scoped;
        }
        return venueService.findAll();
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AgentVenueDTO toDto(Venue venue) {
        return new AgentVenueDTO(
                venue.getId(),
                venue.getVenueName(),
                venue.getAddress(),
                venue.getBusinessHours(),
                venue.getStatus(),
                statusText(venue.getStatus()));
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "关闭";
            case 1 -> "营业中";
            case 2 -> "暂停";
            default -> "未知";
        };
    }
}
