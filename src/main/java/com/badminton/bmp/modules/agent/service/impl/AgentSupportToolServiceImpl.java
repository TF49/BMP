package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffCreateDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportPriceDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportVenueDTO;
import com.badminton.bmp.modules.agent.service.AgentSupportToolService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@link AgentSupportToolService} 默认实现。
 */
@Service
public class AgentSupportToolServiceImpl implements AgentSupportToolService {

    private final VenueService venueService;

    public AgentSupportToolServiceImpl(VenueService venueService) {
        this.venueService = venueService;
    }

    @Override
    public AgentSupportVenueDTO getVenueRealtimeStatus(Long venueId) {
        Long targetId = venueId != null ? venueId : 1L;
        enforceVenueScope(targetId);
        Venue venue = venueService.findById(targetId);


        String venueName = venue != null ? venue.getVenueName() : "BMP智能羽毛球馆";
        boolean isOpen = venue == null || (venue.getStatus() != null && venue.getStatus() == 1);
        String status = isOpen ? "open" : "closed";

        Map<String, String> hours = new HashMap<>();
        if (venue != null && venue.getBusinessHours() != null && venue.getBusinessHours().contains("-")) {
            String[] parts = venue.getBusinessHours().split("-");
            hours.put("open", parts[0].trim());
            hours.put("close", parts[1].trim());
        } else {
            hours.put("open", "09:00");
            hours.put("close", "22:00");
        }

        int totalCourts = venue != null ? venueService.countCourtsByVenueId(targetId) : 10;
        if (totalCourts <= 0) {
            totalCourts = 10;
        }
        int availableCourts = Math.max(1, (int) (totalCourts * 0.7));

        String nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return new AgentSupportVenueDTO(
                String.valueOf(targetId),
                venueName,
                status,
                hours,
                nowIso,
                availableCourts,
                totalCourts
        );
    }

    @Override
    public AgentSupportPriceDTO getVenuePrices(Long venueId) {
        Long targetId = venueId != null ? venueId : 1L;
        enforceVenueScope(targetId);

        List<AgentSupportPriceDTO.PriceItem> priceList = new ArrayList<>();
        priceList.add(new AgentSupportPriceDTO.PriceItem("塑胶", "工作日 09:00-18:00", new BigDecimal("40.00"), "小时"));
        priceList.add(new AgentSupportPriceDTO.PriceItem("塑胶", "工作日 18:00-22:00", new BigDecimal("60.00"), "小时"));
        priceList.add(new AgentSupportPriceDTO.PriceItem("塑胶", "周末 09:00-22:00", new BigDecimal("80.00"), "小时"));
        priceList.add(new AgentSupportPriceDTO.PriceItem("木地板", "全天 09:00-22:00", new BigDecimal("100.00"), "小时"));

        String nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return new AgentSupportPriceDTO(
                String.valueOf(targetId),
                priceList,
                nowIso
        );
    }

    @Override
    public AgentSupportHandoffDTO createHandoff(AgentSupportHandoffCreateDTO createDTO, String idempotencyKey) {
        Long targetId = null;
        if (createDTO != null && createDTO.getVenueId() != null) {
            try {
                targetId = Long.parseLong(createDTO.getVenueId());
            } catch (Exception ignored) {
            }
        }
        enforceVenueScope(targetId);

        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String handoffId = "handoff_" + uuidPart;
        String nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return new AgentSupportHandoffDTO(
                handoffId,
                "pending",
                5,
                nowIso
        );
    }

    private void enforceVenueScope(Long venueId) {
        if (venueId == null) {
            return;
        }
        if (SecurityUtils.isVenueManager()) {
            Long own = SecurityUtils.getCurrentUserVenueId();
            if (own != null && !own.equals(venueId)) {
                throw new BusinessException(403, "无权访问该场馆客服数据");
            }
        }
    }
}

