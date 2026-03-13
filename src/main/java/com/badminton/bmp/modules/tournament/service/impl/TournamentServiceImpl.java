package com.badminton.bmp.modules.tournament.service.impl;

import com.badminton.bmp.modules.tournament.entity.Tournament;
import com.badminton.bmp.modules.tournament.mapper.TournamentMapper;
import com.badminton.bmp.modules.tournament.mapper.TournamentRegistrationMapper;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;

/**
 * 赛事业务实现类
 * 处理赛事相关的业务逻辑
 */
@Service
public class TournamentServiceImpl implements TournamentService {
    @Autowired
    private TournamentMapper tournamentMapper;
    @Autowired
    private VenueMapper venueMapper;
    @Autowired
    private TournamentRegistrationMapper tournamentRegistrationMapper;
    @Autowired
    private FinanceMapper financeMapper;

    /**
     * 根据ID查找赛事
     * @param id 赛事ID
     * @return 赛事对象
     */
    @Override
    public Tournament findById(Long id) {
        Tournament tournament = tournamentMapper.findById(id);
        if (tournament == null) {
            return null;
        }

        // 数据范围过滤
        if (SecurityUtils.isPresident()) {
            return tournament;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(tournament.getVenueId())) {
                return tournament;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：仅允许查看报名中或进行中的赛事（status == 1 或 2）
            if (tournament.getStatus() != null && (tournament.getStatus() == 1 || tournament.getStatus() == 2)) {
                return tournament;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    /**
     * 查找所有赛事（支持条件筛选和分页）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param type 赛事类型（SINGLE/DOUBLE/MIXED，可选）
     * @param keyword 关键词（匹配赛事名称）
     * @param startTime 开始时间（按比赛开始时间筛选，可选）
     * @param endTime 结束时间（按比赛开始时间筛选，可选）
     * @param page 页码
     * @param size 每页数量
     * @return 赛事列表
     */
    @Override
    public List<Tournament> findAll(Long venueId, Integer status, String type, String keyword,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        if (SecurityUtils.isPresident()) {
            return tournamentMapper.findAll(venueId, status, type, keyword, startTime, endTime, offset, size);
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            return tournamentMapper.findAll(vId, status, type, keyword, startTime, endTime, offset, size);
        } else {
            // 普通用户：使用传入参数（通常用于浏览）
            return tournamentMapper.findAll(venueId, status, type, keyword, startTime, endTime, offset, size);
        }
    }

    /**
     * 统计赛事数量（支持条件筛选）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param type 赛事类型（SINGLE/DOUBLE/MIXED，可选）
     * @param keyword 关键词（匹配赛事名称）
     * @param startTime 开始时间（按比赛开始时间筛选，可选）
     * @param endTime 结束时间（按比赛开始时间筛选，可选）
     * @return 赛事数量
     */
    @Override
    public int count(Long venueId, Integer status, String type, String keyword,
                     LocalDateTime startTime, LocalDateTime endTime) {
        if (SecurityUtils.isPresident()) {
            return tournamentMapper.count(venueId, status, type, keyword, startTime, endTime);
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            return tournamentMapper.count(vId, status, type, keyword, startTime, endTime);
        } else {
            return tournamentMapper.count(venueId, status, type, keyword, startTime, endTime);
        }
    }

    /**
     * 添加赛事
     * @param tournament 赛事对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int add(Tournament tournament) {
        // 验证场馆是否存在
        if (venueMapper.findById(tournament.getVenueId()) == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }
        
        // 验证时间逻辑：报名结束时间应该早于比赛开始时间，比赛结束时间应该晚于比赛开始时间
        if (tournament.getRegistrationEnd() != null && tournament.getTournamentStart() != null) {
            if (tournament.getRegistrationEnd().isAfter(tournament.getTournamentStart()) || 
                tournament.getRegistrationEnd().equals(tournament.getTournamentStart())) {
                throw new BusinessException("报名结束时间必须早于比赛开始时间");
            }
        }
        if (tournament.getTournamentStart() != null && tournament.getTournamentEnd() != null) {
            if (tournament.getTournamentEnd().isBefore(tournament.getTournamentStart()) || 
                tournament.getTournamentEnd().equals(tournament.getTournamentStart())) {
                throw new BusinessException("比赛结束时间必须晚于比赛开始时间");
            }
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        tournament.setCreateTime(now);
        tournament.setUpdateTime(now);
        
        // 设置默认值
        if (tournament.getStatus() == null) {
            tournament.setStatus(1);
        }
        if (tournament.getCurrentParticipants() == null) {
            tournament.setCurrentParticipants(0);
        }
        if (tournament.getMaxParticipants() == null) {
            tournament.setMaxParticipants(32);
        }
        if (tournament.getTournamentType() == null || tournament.getTournamentType().trim().isEmpty()) {
            tournament.setTournamentType("SINGLE");
        }
        
        return tournamentMapper.insert(tournament);
    }

    /**
     * 更新赛事信息
     * @param tournament 赛事对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int update(Tournament tournament) {
        // 验证赛事是否存在
        Tournament existing = tournamentMapper.findById(tournament.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("赛事不存在");
        }

        // 权限校验：VENUE_MANAGER只能更新自己场馆的赛事
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新赛事");
            }
            if (existing.getVenueId() == null || !vId.equals(existing.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的赛事");
            }
            // 强制固定为自己的场馆，不允许修改venueId
            if (tournament.getVenueId() != null && !tournament.getVenueId().equals(vId)) {
                throw new BusinessException("权限不足：不能修改赛事的所属场馆");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以更新赛事");
        }

        // 如果修改了场馆ID，验证场馆是否存在
        if (tournament.getVenueId() != null && venueMapper.findById(tournament.getVenueId()) == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }
        
        // 验证时间逻辑：报名结束时间应该早于比赛开始时间，比赛结束时间应该晚于比赛开始时间
        LocalDateTime registrationEnd = tournament.getRegistrationEnd() != null ? tournament.getRegistrationEnd() : existing.getRegistrationEnd();
        LocalDateTime tournamentStart = tournament.getTournamentStart() != null ? tournament.getTournamentStart() : existing.getTournamentStart();
        LocalDateTime tournamentEnd = tournament.getTournamentEnd() != null ? tournament.getTournamentEnd() : existing.getTournamentEnd();
        
        if (registrationEnd != null && tournamentStart != null) {
            if (registrationEnd.isAfter(tournamentStart) || registrationEnd.equals(tournamentStart)) {
                throw new BusinessException("报名结束时间必须早于比赛开始时间");
            }
        }
        if (tournamentStart != null && tournamentEnd != null) {
            if (tournamentEnd.isBefore(tournamentStart) || tournamentEnd.equals(tournamentStart)) {
                throw new BusinessException("比赛结束时间必须晚于比赛开始时间");
            }
        }
        
        // 如果修改了maxParticipants，确保currentParticipants不超过maxParticipants
        Integer maxParticipants = tournament.getMaxParticipants() != null ? tournament.getMaxParticipants() : existing.getMaxParticipants();
        Integer currentParticipants = existing.getCurrentParticipants() != null ? existing.getCurrentParticipants() : 0;
        if (currentParticipants > maxParticipants) {
            throw new BusinessException("当前参赛人数不能超过最大参赛人数");
        }
        // 编辑时不允许覆盖“当前人数”，仅通过报名/取消/退款等操作由 updateCurrentParticipants 更新
        tournament.setCurrentParticipants(existing.getCurrentParticipants());
        
        // 设置更新时间
        tournament.setUpdateTime(LocalDateTime.now());
        return tournamentMapper.update(tournament);
    }

    /**
     * 删除赛事（逻辑删除）
     * @param id 赛事ID
     * @return 影响行数
     */
    @Override
    public int deleteById(Long id) {
        // 验证赛事是否存在
        Tournament tournament = tournamentMapper.findById(id);
        if (tournament == null) {
            throw new ResourceNotFoundException("赛事不存在");
        }

        // 权限校验：VENUE_MANAGER只能删除自己场馆的赛事
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法删除赛事");
            }
            if (tournament.getVenueId() == null || !vId.equals(tournament.getVenueId())) {
                throw new BusinessException("权限不足：只能删除自己场馆的赛事");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以删除赛事");
        }

        return tournamentMapper.deleteById(id);
    }

    /**
     * 更新赛事状态
     * @param id 赛事ID
     * @param status 状态值（0-已取消，1-报名中，2-进行中，3-已结束）
     * @return 影响行数
     */
    @Override
    public int updateStatus(Long id, Integer status) {
        // 验证赛事是否存在
        Tournament tournament = tournamentMapper.findById(id);
        if (tournament == null) {
            throw new ResourceNotFoundException("赛事不存在");
        }
        
        // 验证状态值是否有效（与定时任务、统计一致：仅 0～3）
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("无效的状态值，必须是0（已取消）、1（报名中）、2（进行中）或3（已结束）");
        }
        
        return tournamentMapper.updateStatus(id, status);
    }

    /**
     * 获取赛事统计信息
     * @return 统计信息（总赛事数、各状态数量）
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总赛事数
        stats.put("total", tournamentMapper.countAll());
        
        // 获取各状态的赛事数量
        List<Map<String, Object>> statusCounts = tournamentMapper.countByStatus();
        
        // 初始化各状态数量（与定时任务、列表筛选一致：0已取消 1报名中 2进行中 3已结束）
        int cancelled = 0;  // 已取消（0）
        int enrolling = 0;  // 报名中（1）
        int ongoing = 0;    // 进行中（2）
        int finished = 0;   // 已结束（3）
        
        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");
            if (statusObj == null || countObj == null) continue;
            int status = ((Number) statusObj).intValue();
            int count = ((Number) countObj).intValue();
            switch (status) {
                case 0:
                    cancelled = count;
                    break;
                case 1:
                    enrolling = count;
                    break;
                case 2:
                    ongoing = count;
                    break;
                case 3:
                    finished = count;
                    break;
                default:
                    break;
            }
        }
        
        stats.put("cancelled", cancelled);
        stats.put("enrolling", enrolling);
        stats.put("ongoing", ongoing);
        stats.put("finished", finished);

        // 赛事带动效果：各赛事报名人数与收入（真实数据）
        Long venueFilter = SecurityUtils.isPresident() ? null : SecurityUtils.getCurrentUserVenueId();
        List<Tournament> tournamentList = tournamentMapper.findAll(venueFilter, null, null, null, null, null, 0, 20);
        List<Map<String, Object>> tournamentImpact = new ArrayList<>();
        if (tournamentList != null) {
            for (Tournament t : tournamentList) {
                int newMembers = tournamentRegistrationMapper.count(null, t.getId(), null, null, null, null, null, null, null);
                BigDecimal revenue = financeMapper.sumTournamentIncomeByTournamentId(t.getId());
                if (revenue == null) revenue = BigDecimal.ZERO;
                Map<String, Object> item = new HashMap<>();
                item.put("tournamentName", t.getTournamentName());
                item.put("name", t.getTournamentName());
                item.put("newMembers", newMembers);
                item.put("members", newMembers);
                item.put("revenue", revenue);
                item.put("income", revenue);
                tournamentImpact.add(item);
            }
        }
        stats.put("tournamentImpact", tournamentImpact);
        
        return stats;
    }

    /**
     * 更新赛事当前参赛人数
     * @param id 赛事ID
     * @param currentParticipants 当前参赛人数
     * @return 影响行数
     */
    @Override
    public int updateCurrentParticipants(Long id, Integer currentParticipants) {
        return tournamentMapper.updateCurrentParticipants(id, currentParticipants);
    }

    /**
     * 定时任务：按当前时间将「报名中」→「进行中」、「进行中」→「已结束」（仅时间已到的赛事）
     */
    @Override
    public void autoUpdateTournamentStatusByTime() {
        LocalDateTime now = LocalDateTime.now();
        tournamentMapper.batchUpdateEnrollingToOngoing(now);
        tournamentMapper.batchUpdateOngoingToFinished(now);
    }
}
