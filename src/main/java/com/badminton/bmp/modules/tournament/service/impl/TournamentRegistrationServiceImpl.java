package com.badminton.bmp.modules.tournament.service.impl;

import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.tournament.entity.Tournament;
import com.badminton.bmp.modules.tournament.entity.TournamentRegistration;
import com.badminton.bmp.modules.tournament.mapper.TournamentMapper;
import com.badminton.bmp.modules.tournament.mapper.TournamentRegistrationMapper;
import com.badminton.bmp.modules.tournament.service.TournamentRegistrationService;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badminton.bmp.common.util.SecurityUtils;

/**
 * 赛事报名业务实现类
 * 处理赛事报名相关的业务逻辑
 */
@Service
public class TournamentRegistrationServiceImpl implements TournamentRegistrationService {
    @Autowired
    private TournamentRegistrationMapper tournamentRegistrationMapper;
    @Autowired
    private TournamentMapper tournamentMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    /**
     * 根据ID查找赛事报名记录
     * @param id 报名记录ID
     * @return 报名记录对象
     */
    @Override
    public TournamentRegistration findById(Long id) {
        TournamentRegistration registration = tournamentRegistrationMapper.findById(id);
        if (registration == null) {
            return null;
        }
        
        // 数据范围过滤：根据角色检查访问权限
        if (SecurityUtils.isPresident()) {
            // PRESIDENT: 可以访问所有数据
            return registration;
        } else if (SecurityUtils.isVenueManager()) {
            // VENUE_MANAGER: 只能访问自己场馆的数据
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId != null) {
                // 需要通过赛事查询场馆ID
                Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
                if (tournament != null && currentVenueId.equals(tournament.getVenueId())) {
                    return registration;
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // USER: 可访问自己作为主报名人或搭档的报名
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null && (m.getId().equals(registration.getMemberId())
                        || (registration.getPartnerId() != null && m.getId().equals(registration.getPartnerId())))) {
                    return registration;
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    /**
     * 查找所有赛事报名记录（支持条件筛选和分页）
     * @param tournamentId 赛事ID（可选）
     * @param memberId 会员ID（可选）
     * @param status 状态（可选）
     * @param registrationNo 报名单号（可选）
     * @param memberKeyword 会员关键词（可选，匹配会员姓名）
     * @param startTime 开始时间（按报名创建时间筛选，可选）
     * @param endTime 结束时间（按报名创建时间筛选，可选）
     * @param page 页码
     * @param size 每页数量
     * @return 报名记录列表
     */
    @Override
    public List<TournamentRegistration> findAll(Long tournamentId, Long memberId, Integer status,
                                                String registrationNo, String memberKeyword,
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
        Long venueFilter = null;
        Long memberFilter = memberId;
        Long participantMemberId = null;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：查「我参与的报名」（主报名人或搭档）
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    participantMemberId = m.getId();
                    memberFilter = null;
                }
            }
        }
        return tournamentRegistrationMapper.findAll(venueFilter, tournamentId, memberFilter, participantMemberId, status,
                registrationNo, memberKeyword, startTime, endTime, offset, size);
    }

    /**
     * 统计赛事报名记录数量（支持条件筛选）
     * @param tournamentId 赛事ID（可选）
     * @param memberId 会员ID（可选）
     * @param status 状态（可选）
     * @param registrationNo 报名单号（可选）
     * @param memberKeyword 会员关键词（可选，匹配会员姓名）
     * @param startTime 开始时间（按报名创建时间筛选，可选）
     * @param endTime 结束时间（按报名创建时间筛选，可选）
     * @return 报名记录数量
     */
    @Override
    public int count(Long tournamentId, Long memberId, Integer status,
                     String registrationNo, String memberKeyword,
                     LocalDateTime startTime, LocalDateTime endTime) {
        Long venueFilter = null;
        Long memberFilter = memberId;
        Long participantMemberId = null;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    participantMemberId = m.getId();
                    memberFilter = null;
                }
            }
        }
        return tournamentRegistrationMapper.count(venueFilter, tournamentId, memberFilter, participantMemberId, status,
                registrationNo, memberKeyword, startTime, endTime);
    }

    /**
     * 添加赛事报名记录
     * @param registration 报名记录对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int add(TournamentRegistration registration) {
        // 权限校验：USER 只能为自己创建报名，且可自动补全 memberId
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null) {
                throw new RuntimeException("权限不足");
            }
            Member m = memberMapper.findByUserId(current.getId());
            if (m == null) {
                throw new RuntimeException("当前用户未关联会员，无法报名赛事");
            }
            if (registration.getMemberId() == null) {
                registration.setMemberId(m.getId());
            } else if (!m.getId().equals(registration.getMemberId())) {
                throw new RuntimeException("权限不足，只能为自己创建报名");
            }
        }
        
        // 验证会员是否存在
        Member member = memberMapper.findById(registration.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 如果提供了搭档ID，验证搭档会员是否存在
        if (registration.getPartnerId() != null) {
            Member partner = memberMapper.findById(registration.getPartnerId());
            if (partner == null) {
                throw new RuntimeException("搭档会员不存在");
            }
            if (registration.getPartnerId().equals(registration.getMemberId())) {
                throw new RuntimeException("主报名人与搭档不能为同一人");
            }
        }

        // 验证赛事是否存在
        Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
        if (tournament == null) {
            throw new RuntimeException("赛事不存在");
        }
        // 双打/混双必须填写搭档
        String type = tournament.getTournamentType();
        if ("DOUBLE".equals(type) || "MIXED".equals(type)) {
            if (registration.getPartnerId() == null) {
                throw new RuntimeException("双打/混双赛事必须填写搭档");
            }
        }
        // 同一赛事下同一会员只能有一条有效报名（作为主报名人或搭档）
        if (tournamentRegistrationMapper.countValidRegistrationInTournament(
                registration.getTournamentId(), registration.getMemberId(), null) > 0) {
            throw new RuntimeException("该会员已在本赛事有有效报名，不可重复报名");
        }
        if (registration.getPartnerId() != null && tournamentRegistrationMapper.countValidRegistrationInTournament(
                registration.getTournamentId(), registration.getPartnerId(), null) > 0) {
            throw new RuntimeException("该搭档已在本赛事有有效报名，不可重复报名");
        }
        // 仅允许对「报名中」的赛事新增报名，按状态返回明确提示
        Integer tournamentStatus = tournament.getStatus();
        if (tournamentStatus == null || tournamentStatus != 1) {
            String msg;
            if (tournamentStatus == null) {
                msg = "该赛事状态异常，无法新增报名";
            } else if (tournamentStatus == 0) {
                msg = "该赛事已取消，无法新增报名";
            } else if (tournamentStatus == 2) {
                msg = "该赛事已进行中，无法新增报名";
            } else if (tournamentStatus == 3) {
                msg = "该赛事已结束，无法新增报名";
            } else {
                msg = "该赛事未开放报名，仅可对报名中的赛事新增报名";
            }
            throw new RuntimeException(msg);
        }
        // 未传或传 0 时使用赛事报名费；双打/混双为单人报名费×2
        if (registration.getEntryFee() == null || registration.getEntryFee().signum() <= 0) {
            java.math.BigDecimal fee = tournament.getEntryFee() != null ? tournament.getEntryFee() : java.math.BigDecimal.ZERO;
            if ("DOUBLE".equals(type) || "MIXED".equals(type)) {
                fee = fee.multiply(java.math.BigDecimal.valueOf(2));
            }
            registration.setEntryFee(fee);
        }
        
        // 验证赛事报名人数是否已满
        if (tournament.getCurrentParticipants() >= tournament.getMaxParticipants()) {
            throw new RuntimeException("赛事报名人数已满");
        }
        
        // 生成报名单号（如果未提供）
        if (registration.getRegistrationNo() == null || registration.getRegistrationNo().trim().isEmpty()) {
            registration.setRegistrationNo(generateRegistrationNo());
        }
        
        // 验证报名单号是否已存在
        if (tournamentRegistrationMapper.existsByRegistrationNo(registration.getRegistrationNo(), null)) {
            throw new RuntimeException("报名单号已存在");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        registration.setCreateTime(now);
        registration.setUpdateTime(now);
        
        // 设置默认值；新增报名强制为未支付，仅点击支付按钮后才扣款
        if (registration.getStatus() == null) {
            registration.setStatus(1);
        }
        registration.setPaymentStatus(0);
        
        // 插入报名记录
        int result = tournamentRegistrationMapper.insert(registration);
        
        // 如果插入成功，更新赛事的当前参赛人数
        if (result > 0) {
            tournamentMapper.updateCurrentParticipants(registration.getTournamentId(), 
                tournament.getCurrentParticipants() + 1);
        }
        
        return result;
    }

    /**
     * 更新赛事报名记录
     * @param registration 报名记录对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int update(TournamentRegistration registration) {
        // 验证报名记录是否存在
        TournamentRegistration existing = tournamentRegistrationMapper.findById(registration.getId());
        if (existing == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的报名
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Tournament tournament = tournamentMapper.findById(existing.getTournamentId());
                    if (tournament == null || !currentVenueId.equals(tournament.getVenueId())) {
                        throw new RuntimeException("权限不足，只能修改自己场馆的报名");
                    }
                }
            } else {
                // USER: 只能修改自己的报名
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(existing.getMemberId())) {
                        throw new RuntimeException("权限不足，只能修改自己的报名");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        Long tournamentId = registration.getTournamentId() != null ? registration.getTournamentId() : existing.getTournamentId();
        Long effectiveMemberId = registration.getMemberId() != null ? registration.getMemberId() : existing.getMemberId();
        Long effectivePartnerId = registration.getPartnerId() != null ? registration.getPartnerId() : existing.getPartnerId();
        if (effectivePartnerId != null && effectivePartnerId.equals(effectiveMemberId)) {
            throw new RuntimeException("主报名人与搭档不能为同一人");
        }
        Tournament tournamentForValidation = tournamentMapper.findById(tournamentId);
        if (tournamentForValidation != null) {
            String type = tournamentForValidation.getTournamentType();
            if ("DOUBLE".equals(type) || "MIXED".equals(type)) {
                if (effectivePartnerId == null) {
                    throw new RuntimeException("双打/混双赛事必须填写搭档");
                }
            }
            if (tournamentRegistrationMapper.countValidRegistrationInTournament(tournamentId, effectiveMemberId, existing.getId()) > 0) {
                throw new RuntimeException("该会员已在本赛事有有效报名，不可重复报名");
            }
            if (effectivePartnerId != null && tournamentRegistrationMapper.countValidRegistrationInTournament(tournamentId, effectivePartnerId, existing.getId()) > 0) {
                throw new RuntimeException("该搭档已在本赛事有有效报名，不可重复报名");
            }
        }
        Integer oldStatus = existing.getStatus();
        Integer newStatus = registration.getStatus() != null ? registration.getStatus() : existing.getStatus();
        
        // 如果修改了赛事ID，需要处理参赛人数的变化
        if (registration.getTournamentId() != null && !registration.getTournamentId().equals(existing.getTournamentId())) {
            // 从旧赛事减少参赛人数（如果旧状态是有效状态）
            if (oldStatus != null && oldStatus >= 1 && oldStatus <= 3) {
                Tournament oldTournament = tournamentMapper.findById(existing.getTournamentId());
                if (oldTournament != null) {
                    tournamentMapper.updateCurrentParticipants(existing.getTournamentId(), 
                        Math.max(0, oldTournament.getCurrentParticipants() - 1));
                }
            }
            
            // 向新赛事增加参赛人数（需要验证新赛事是否还有名额）
            Tournament newTournament = tournamentMapper.findById(registration.getTournamentId());
            if (newTournament == null) {
                throw new RuntimeException("新赛事不存在");
            }
            if (newStatus != null && newStatus >= 1 && newStatus <= 3) {
                if (newTournament.getCurrentParticipants() >= newTournament.getMaxParticipants()) {
                    throw new RuntimeException("新赛事报名人数已满");
                }
                tournamentMapper.updateCurrentParticipants(registration.getTournamentId(), 
                    newTournament.getCurrentParticipants() + 1);
            }
        } else if (newStatus != null && !newStatus.equals(oldStatus)) {
            // 如果只修改了状态（未修改赛事），需要处理参赛人数的变化
            Tournament tournament = tournamentMapper.findById(tournamentId);
            if (tournament != null) {
                boolean oldStatusValid = oldStatus != null && oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = newStatus >= 1 && newStatus <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少参赛人数
                    tournamentMapper.updateCurrentParticipants(tournamentId, 
                        Math.max(0, tournament.getCurrentParticipants() - 1));
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加参赛人数
                    if (tournament.getCurrentParticipants() >= tournament.getMaxParticipants()) {
                        throw new RuntimeException("赛事报名人数已满");
                    }
                    tournamentMapper.updateCurrentParticipants(tournamentId, 
                        tournament.getCurrentParticipants() + 1);
                }
            }
        }
        
        // 部分更新时避免用 null 覆盖已有字段
        if (registration.getTournamentId() == null) registration.setTournamentId(existing.getTournamentId());
        if (registration.getMemberId() == null) registration.setMemberId(existing.getMemberId());
        if (registration.getPartnerId() == null) registration.setPartnerId(existing.getPartnerId());
        if (registration.getEntryFee() == null) registration.setEntryFee(existing.getEntryFee());
        if (registration.getStatus() == null) registration.setStatus(existing.getStatus());
        if (registration.getPaymentMethod() == null) registration.setPaymentMethod(existing.getPaymentMethod());
        if (registration.getPaymentStatus() == null) registration.setPaymentStatus(existing.getPaymentStatus());
        // 设置更新时间
        registration.setUpdateTime(LocalDateTime.now());
        return tournamentRegistrationMapper.update(registration);
    }

    /**
     * 删除赛事报名记录（逻辑删除）
     * @param id 报名记录ID
     * @return 影响行数
     */
    @Override
    @Transactional
    public int deleteById(Long id) {
        // 验证报名记录是否存在
        TournamentRegistration registration = tournamentRegistrationMapper.findById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能删除自己场馆的报名
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
                    if (tournament == null || !currentVenueId.equals(tournament.getVenueId())) {
                        throw new RuntimeException("权限不足，只能删除自己场馆的报名");
                    }
                }
            } else {
                // USER: 只能删除自己的报名
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(registration.getMemberId())) {
                        throw new RuntimeException("权限不足，只能删除自己的报名");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        // 如果状态是有效状态（1-待支付，2-已支付，3-已参赛），都需要减少赛事的当前参赛人数
        if (registration.getStatus() != null && registration.getStatus() >= 1 && registration.getStatus() <= 3) {
            Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
            if (tournament != null) {
                tournamentMapper.updateCurrentParticipants(registration.getTournamentId(), 
                    Math.max(0, tournament.getCurrentParticipants() - 1));
            }
        }
        
        return tournamentRegistrationMapper.deleteById(id);
    }

    /**
     * 更新赛事报名状态
     * @param id 报名记录ID
     * @param status 状态值（0-已取消，1-待支付，2-已支付，3-已参赛，4-已退赛）
     * @return 影响行数
     */
    @Override
    @Transactional
    public int updateStatus(Long id, Integer status) {
        // 验证报名记录是否存在
        TournamentRegistration registration = tournamentRegistrationMapper.findById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的报名状态
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
                    if (tournament == null || !currentVenueId.equals(tournament.getVenueId())) {
                        throw new RuntimeException("权限不足，只能修改自己场馆的报名状态");
                    }
                }
            } else {
                // USER: 主报名人或搭档均可修改报名状态（通常仅允许取消）
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null) {
                        throw new RuntimeException("权限不足，只能修改自己的报名状态");
                    }
                    boolean isMemberOrPartner = m.getId().equals(registration.getMemberId())
                            || (registration.getPartnerId() != null && m.getId().equals(registration.getPartnerId()));
                    if (!isMemberOrPartner) {
                        throw new RuntimeException("权限不足，只能修改自己参与的报名状态");
                    }
                    // USER只能取消报名（status=0），不能设置为其他状态
                    if (status != null && status != 0) {
                        throw new RuntimeException("普通用户只能取消报名");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        // 验证状态值是否有效
        if (status == null || status < 0 || status > 4) {
            throw new RuntimeException("无效的状态值，必须是0（已取消）、1（待支付）、2（已支付）、3（已参赛）或4（已退赛）");
        }
        
        // 如果状态变更，需要处理参赛人数的变化
        Integer oldStatus = registration.getStatus();
        if (oldStatus != null && !oldStatus.equals(status)) {
            Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
            if (tournament != null) {
                boolean oldStatusValid = oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = status >= 1 && status <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少参赛人数
                    tournamentMapper.updateCurrentParticipants(registration.getTournamentId(), 
                        Math.max(0, tournament.getCurrentParticipants() - 1));
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加参赛人数
                    if (tournament.getCurrentParticipants() >= tournament.getMaxParticipants()) {
                        throw new RuntimeException("赛事报名人数已满");
                    }
                    tournamentMapper.updateCurrentParticipants(registration.getTournamentId(), 
                        tournament.getCurrentParticipants() + 1);
                }
            }
        }
        
        int result = tournamentRegistrationMapper.updateStatus(id, status);
        try {
            Long userId = null;
            Member m = memberMapper.findById(registration.getMemberId());
            if (m != null && m.getUserId() != null) userId = m.getUserId();
            String statusText = tournamentRegistrationStatusText(status);
            webSocketPushService.pushOrderStatusToUser(userId, "tournamentRegistration", id, status, statusText, "赛事报名");
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    private static String tournamentRegistrationStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "已参赛";
            case 4: return "已退赛";
            default: return "未知";
        }
    }

    /**
     * 获取赛事报名统计信息
     * @return 统计信息（总报名数、各状态数量）
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总报名数
        stats.put("total", tournamentRegistrationMapper.countAll());
        
        // 获取各状态的报名数量
        List<Map<String, Object>> statusCounts = tournamentRegistrationMapper.countByStatus();
        
        // 初始化各状态数量
        int cancelled = 0;     // 已取消（0）
        int pending = 0;        // 待支付（1）
        int paid = 0;          // 已支付（2）
        int participated = 0;  // 已参赛（3）
        int withdrawn = 0;     // 已退赛（4）
        
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
                    pending = count;
                    break;
                case 2:
                    paid = count;
                    break;
                case 3:
                    participated = count;
                    break;
                case 4:
                    withdrawn = count;
                    break;
            }
        }
        
        stats.put("cancelled", cancelled);
        stats.put("pending", pending);
        stats.put("paid", paid);
        stats.put("participated", participated);
        stats.put("withdrawn", withdrawn);

        // Dashboard 赛事报名漏斗图：views/signups/paid/attended
        Map<String, Object> funnel = new HashMap<>();
        int totalReg = tournamentRegistrationMapper.countAll();
        funnel.put("views", totalReg);
        funnel.put("totalViews", totalReg);
        funnel.put("signups", totalReg);
        funnel.put("totalSignups", totalReg);
        funnel.put("paid", paid);
        funnel.put("totalPaid", paid);
        funnel.put("attended", participated);
        funnel.put("totalAttended", participated);
        stats.put("funnel", funnel);

        return stats;
    }

    /**
     * 生成报名单号（线程安全）
     * 格式：TR + yyyyMMdd + 三位序号（如：TR20240101001）
     * @return 报名单号
     */
    @Override
    public synchronized String generateRegistrationNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TR" + dateStr;
        List<TournamentRegistration> todayRegistrations = tournamentRegistrationMapper.findAll(null, null, null, null, null, prefix, null, null, null, 0, 1000);
        int maxSequence = 0;
        for (TournamentRegistration reg : todayRegistrations) {
            if (reg.getRegistrationNo() != null && reg.getRegistrationNo().startsWith(prefix)) {
                try {
                    String seq = reg.getRegistrationNo().substring(prefix.length());
                    int sequence = Integer.parseInt(seq);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略格式错误的单号
                }
            }
        }
        return prefix + String.format("%03d", maxSequence + 1);
    }

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param registrationId 报名ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long registrationId, String paymentMethod) {
        // 查询报名记录
        TournamentRegistration registration = tournamentRegistrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
                if (currentVenueId == null || tournament == null || !currentVenueId.equals(tournament.getVenueId())) {
                    throw new RuntimeException("权限不足，只能处理自己场馆的赛事报名支付");
                }
            } else {
                throw new RuntimeException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证报名状态
        if (registration.getStatus() != 1) {
            Integer regStatus = registration.getStatus();
            String msg;
            if (regStatus == null) {
                msg = "报名状态异常，无法支付";
            } else if (regStatus == 0) {
                msg = "该报名已取消，无法支付";
            } else if (regStatus == 2) {
                msg = "该报名已支付，无需重复支付";
            } else if (regStatus == 3) {
                msg = "该报名已参赛，无法重复支付";
            } else if (regStatus == 4) {
                msg = "该报名已退赛，无法支付";
            } else {
                msg = "报名状态不正确，只能对待支付状态的报名进行支付";
            }
            throw new RuntimeException(msg);
        }

        // 如果使用余额支付，调用消费记录服务
        if ("BALANCE".equals(paymentMethod)) {
            memberConsumeRecordService.createConsumeRecord(
                registration.getMemberId(),
                registration.getEntryFee(),
                "TOURNAMENT",
                registrationId,
                paymentMethod,
                "赛事报名支付：" + registration.getRegistrationNo()
            );
        }

        // 获取场馆ID用于财务记录
        Tournament tournamentForFinance = tournamentMapper.findById(registration.getTournamentId());
        Long venueIdForFinance = tournamentForFinance != null ? tournamentForFinance.getVenueId() : null;

        // 创建财务记录（收入）
        financeService.createFromBusiness(
            Finance.TYPE_TOURNAMENT,
            registrationId,
            registration.getEntryFee(),
            Finance.INCOME,
            paymentMethod,
            venueIdForFinance,
            "赛事报名支付：" + registration.getRegistrationNo()
        );

        // 更新报名支付状态和状态
        registration.setPaymentMethod(paymentMethod);
        registration.setPaymentStatus(1); // 已支付
        registration.setStatus(2); // 已支付
        registration.setUpdateTime(LocalDateTime.now());
        int updated = tournamentRegistrationMapper.update(registration);
        if (updated > 0) {
            try {
                Long userId = null;
                Member m = memberMapper.findById(registration.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(userId, "tournamentRegistration", registrationId, 2, "已支付", "赛事报名");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return updated;
    }

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param registrationId 报名ID
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long registrationId) {
        // 查询报名记录
        TournamentRegistration registration = tournamentRegistrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
                if (currentVenueId == null || tournament == null || !currentVenueId.equals(tournament.getVenueId())) {
                    throw new RuntimeException("权限不足，只能处理自己场馆的赛事报名退款");
                }
            } else {
                throw new RuntimeException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证报名支付状态，按状态返回明确提示（null 视为未支付，与场地预约一致）
        Integer payStatus = registration.getPaymentStatus();
        if (payStatus == null || payStatus != 1) {
            String msg;
            if (payStatus == null || payStatus == 0) {
                msg = "该报名尚未支付，无法退款";
            } else if (payStatus == 2) {
                msg = "该报名已退款，无需重复退款";
            } else {
                msg = "只能对已支付的报名进行退款";
            }
            throw new RuntimeException(msg);
        }

        // 调用退款记录服务（只有余额支付才需要）
        if ("BALANCE".equals(registration.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                registration.getMemberId(),
                registration.getEntryFee(),
                "TOURNAMENT",
                registrationId,
                registration.getPaymentMethod(),
                "赛事报名退款：" + registration.getRegistrationNo()
            );
        }

        // 获取场馆ID用于财务记录
        Tournament tournamentForRefund = tournamentMapper.findById(registration.getTournamentId());
        Long venueIdForRefund = tournamentForRefund != null ? tournamentForRefund.getVenueId() : null;

        // 创建财务记录（支出/退款）
        financeService.createFromBusiness(
            Finance.TYPE_TOURNAMENT,
            registrationId,
            registration.getEntryFee(),
            Finance.EXPENSE,
            registration.getPaymentMethod(),
            venueIdForRefund,
            "赛事报名退款：" + registration.getRegistrationNo()
        );

        // 更新报名状态
        registration.setPaymentStatus(2); // 已退款
        registration.setStatus(0); // 已取消
        registration.setUpdateTime(LocalDateTime.now());
        int result = tournamentRegistrationMapper.update(registration);

        // 减少赛事的当前参赛人数
        Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
        if (tournament != null) {
            tournamentMapper.updateCurrentParticipants(registration.getTournamentId(),
                Math.max(0, tournament.getCurrentParticipants() - 1));
        }
        if (result > 0) {
            try {
                Long userId = null;
                Member m = memberMapper.findById(registration.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(userId, "tournamentRegistration", registrationId, 0, "已取消", "赛事报名");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * 定时任务：将「已支付」且关联赛事已到开始时间的报名改为「已参赛」
     * 与课程预约定时任务（已支付→进行中、进行中→已完成）保持一致思路
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateTournamentRegistrationStatusByTime() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> toParticipate = tournamentRegistrationMapper.findRegistrationIdsToParticipate(now);
        if (toParticipate != null) {
            for (Long id : toParticipate) {
                tournamentRegistrationMapper.updateStatus(id, 3); // 已参赛
            }
            try {
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
    }
}
