package com.badminton.bmp.modules.tournament.service.impl;

import com.badminton.bmp.config.PaymentAutoCancelProperties;
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
import com.badminton.bmp.modules.tournament.support.TournamentTypeHelper;
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
    private static final java.math.BigDecimal DOUBLES_FEE_MULTIPLIER = java.math.BigDecimal.valueOf(2);

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
    @Autowired
    private PaymentAutoCancelProperties paymentAutoCancelProperties;

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
        Member partner = null;
        if (registration.getPartnerId() != null) {
            partner = memberMapper.findById(registration.getPartnerId());
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
        String eventType = TournamentTypeHelper.normalizeEventType(
                tournament.getEventType(),
                tournament.getTournamentType(),
                tournament.getTournamentName(),
                tournament.getDescription()
        );
        // 双打/混双必须填写搭档，单打禁止带搭档
        if (TournamentTypeHelper.isDoublesEvent(eventType)) {
            if (registration.getPartnerId() == null) {
                throw new RuntimeException("双打/混双赛事必须填写搭档");
            }
        } else if (registration.getPartnerId() != null) {
            throw new RuntimeException("单打赛事不允许填写搭档");
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
        registration.setEntryFee(resolveEntryFee(tournament, eventType, registration.getEntryFee()));
        if (registration.getPaymentMethod() == null || registration.getPaymentMethod().trim().isEmpty()) {
            registration.setPaymentMethod("BALANCE");
        }

        if (registration.getRegistrantName() == null || registration.getRegistrantName().trim().isEmpty()) {
            registration.setRegistrantName(member.getMemberName());
        }
        if (registration.getRegistrantPhone() == null || registration.getRegistrantPhone().trim().isEmpty()) {
            registration.setRegistrantPhone(member.getPhone());
        }
        if (registration.getRegistrantIdCard() == null || registration.getRegistrantIdCard().trim().isEmpty()) {
            registration.setRegistrantIdCard(member.getIdCard());
        }
        registration.setEventTypeSnapshot(eventType);
        registration.setEventTypeNameSnapshot(TournamentTypeHelper.eventTypeLabel(eventType));
        if (partner != null) {
            if (registration.getPartnerNameSnapshot() == null || registration.getPartnerNameSnapshot().trim().isEmpty()) {
                registration.setPartnerNameSnapshot(partner.getMemberName());
            }
            if (registration.getPartnerPhoneSnapshot() == null || registration.getPartnerPhoneSnapshot().trim().isEmpty()) {
                registration.setPartnerPhoneSnapshot(partner.getPhone());
            }
        } else {
            registration.setPartnerNameSnapshot(null);
            registration.setPartnerPhoneSnapshot(null);
        }
        
        int occupancyUnits = resolveOccupancyUnits(eventType);
        ensureTournamentCapacity(tournament, occupancyUnits, "赛事报名人数已满");
        
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
        
        // 如果插入成功，按有效报名重算赛事当前占位人数
        if (result > 0) {
            syncTournamentCurrentParticipants(registration.getTournamentId());
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
        boolean tournamentChanged = registration.getTournamentId() != null && !registration.getTournamentId().equals(existing.getTournamentId());
        if (effectivePartnerId != null && effectivePartnerId.equals(effectiveMemberId)) {
            throw new RuntimeException("主报名人与搭档不能为同一人");
        }
        Tournament tournamentForValidation = tournamentMapper.findById(tournamentId);
        if (tournamentForValidation != null) {
            if (tournamentChanged) {
                ensureTournamentSelectableForRegistration(tournamentForValidation);
            }
            String eventType = TournamentTypeHelper.normalizeEventType(
                    tournamentForValidation.getEventType(),
                    tournamentForValidation.getTournamentType(),
                    tournamentForValidation.getTournamentName(),
                    tournamentForValidation.getDescription()
            );
            if (TournamentTypeHelper.isDoublesEvent(eventType)) {
                if (effectivePartnerId == null) {
                    throw new RuntimeException("双打/混双赛事必须填写搭档");
                }
            } else if (effectivePartnerId != null) {
                throw new RuntimeException("单打赛事不允许填写搭档");
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
        boolean oldStatusValid = isActiveStatus(oldStatus);
        boolean newStatusValid = isActiveStatus(newStatus);
        int occupancyUnits = 0;
        if (tournamentForValidation != null) {
            String effectiveEventType = TournamentTypeHelper.normalizeEventType(
                    tournamentForValidation.getEventType(),
                    tournamentForValidation.getTournamentType(),
                    tournamentForValidation.getTournamentName(),
                    tournamentForValidation.getDescription()
            );
            occupancyUnits = resolveOccupancyUnits(effectiveEventType);
        }
        if (newStatusValid) {
            if (tournamentChanged) {
                Tournament newTournament = tournamentMapper.findById(tournamentId);
                if (newTournament == null) {
                    throw new RuntimeException("新赛事不存在");
                }
                ensureTournamentCapacity(newTournament, occupancyUnits, "新赛事报名人数已满");
            } else if (!oldStatusValid) {
                Tournament tournament = tournamentMapper.findById(tournamentId);
                if (tournament != null) {
                    ensureTournamentCapacity(tournament, occupancyUnits, "赛事报名人数已满");
                }
            }
        }
        
        // 部分更新时避免用 null 覆盖已有字段
        if (registration.getTournamentId() == null) registration.setTournamentId(existing.getTournamentId());
        if (registration.getMemberId() == null) registration.setMemberId(existing.getMemberId());
        if (registration.getPartnerId() == null) registration.setPartnerId(existing.getPartnerId());
        if (registration.getRegistrantName() == null) registration.setRegistrantName(existing.getRegistrantName());
        if (registration.getRegistrantPhone() == null) registration.setRegistrantPhone(existing.getRegistrantPhone());
        if (registration.getRegistrantIdCard() == null) registration.setRegistrantIdCard(existing.getRegistrantIdCard());
        if (registration.getEventTypeSnapshot() == null) registration.setEventTypeSnapshot(existing.getEventTypeSnapshot());
        if (registration.getEventTypeNameSnapshot() == null) registration.setEventTypeNameSnapshot(existing.getEventTypeNameSnapshot());
        if (registration.getPartnerNameSnapshot() == null) registration.setPartnerNameSnapshot(existing.getPartnerNameSnapshot());
        if (registration.getPartnerPhoneSnapshot() == null) registration.setPartnerPhoneSnapshot(existing.getPartnerPhoneSnapshot());
        if (registration.getEntryFee() == null) registration.setEntryFee(existing.getEntryFee());
        if (registration.getStatus() == null) registration.setStatus(existing.getStatus());
        if (registration.getPaymentMethod() == null) registration.setPaymentMethod(existing.getPaymentMethod());
        if (registration.getPaymentStatus() == null) registration.setPaymentStatus(existing.getPaymentStatus());
        Member effectiveMember = memberMapper.findById(registration.getMemberId());
        if (effectiveMember != null) {
            if (registration.getRegistrantName() == null || registration.getRegistrantName().trim().isEmpty()) {
                registration.setRegistrantName(effectiveMember.getMemberName());
            }
            if (registration.getRegistrantPhone() == null || registration.getRegistrantPhone().trim().isEmpty()) {
                registration.setRegistrantPhone(effectiveMember.getPhone());
            }
            if (registration.getRegistrantIdCard() == null || registration.getRegistrantIdCard().trim().isEmpty()) {
                registration.setRegistrantIdCard(effectiveMember.getIdCard());
            }
        }
        if (tournamentForValidation != null) {
            String effectiveEventType = TournamentTypeHelper.normalizeEventType(
                    tournamentForValidation.getEventType(),
                    tournamentForValidation.getTournamentType(),
                    tournamentForValidation.getTournamentName(),
                    tournamentForValidation.getDescription()
            );
            registration.setEventTypeSnapshot(effectiveEventType);
            registration.setEventTypeNameSnapshot(TournamentTypeHelper.eventTypeLabel(effectiveEventType));
            registration.setEntryFee(resolveEntryFee(tournamentForValidation, effectiveEventType, registration.getEntryFee()));
        }
        if (registration.getPartnerId() != null) {
            Member effectivePartner = memberMapper.findById(registration.getPartnerId());
            if (effectivePartner != null) {
                registration.setPartnerNameSnapshot(effectivePartner.getMemberName());
                registration.setPartnerPhoneSnapshot(effectivePartner.getPhone());
            }
        } else {
            registration.setPartnerNameSnapshot(null);
            registration.setPartnerPhoneSnapshot(null);
        }
        // 设置更新时间
        registration.setUpdateTime(LocalDateTime.now());
        int result = tournamentRegistrationMapper.update(registration);
        if (result > 0) {
            syncTournamentCurrentParticipants(tournamentId);
            if (tournamentChanged) {
                syncTournamentCurrentParticipants(existing.getTournamentId());
            }
        }
        return result;
    }

    private java.math.BigDecimal resolveEntryFee(Tournament tournament, String eventType, java.math.BigDecimal requestedFee) {
        java.math.BigDecimal baseFee = tournament.getEntryFee() != null ? tournament.getEntryFee() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal expectedFee = TournamentTypeHelper.isDoublesEvent(eventType)
                ? baseFee.multiply(DOUBLES_FEE_MULTIPLIER)
                : baseFee;
        if (requestedFee == null || requestedFee.signum() <= 0) {
            return expectedFee;
        }
        return TournamentTypeHelper.isDoublesEvent(eventType) ? expectedFee : requestedFee;
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
        
        int result = tournamentRegistrationMapper.deleteById(id);
        if (result > 0 && isActiveStatus(registration.getStatus())) {
            syncTournamentCurrentParticipants(registration.getTournamentId());
        }
        return result;
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

        if (status == 0 && registration.getPaymentStatus() != null && registration.getPaymentStatus() == 1) {
            registration.setStatus(0);
            registration.setPaymentStatus(3);
            registration.setUpdateTime(LocalDateTime.now());
            int updated = tournamentRegistrationMapper.update(registration);
            if (updated > 0) {
                syncTournamentCurrentParticipants(registration.getTournamentId());
                try {
                    Long userId = null;
                    Member m = memberMapper.findById(registration.getMemberId());
                    if (m != null && m.getUserId() != null) userId = m.getUserId();
                    webSocketPushService.pushOrderStatusToUser(userId, "tournamentRegistration", id, 0, "已取消", "赛事报名");
                    webSocketPushService.pushDashboardRefresh();
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
                }
            }
            return updated;
        }
        
        // 如果状态从无效变有效，需要校验席位是否足够
        Integer oldStatus = registration.getStatus();
        if (!isActiveStatus(oldStatus) && isActiveStatus(status)) {
            Tournament tournament = tournamentMapper.findById(registration.getTournamentId());
            if (tournament != null) {
                String eventType = TournamentTypeHelper.normalizeEventType(
                        tournament.getEventType(),
                        tournament.getTournamentType(),
                        tournament.getTournamentName(),
                        tournament.getDescription()
                );
                ensureTournamentCapacity(tournament, resolveOccupancyUnits(eventType), "赛事报名人数已满");
            }
        }
        
        int result = tournamentRegistrationMapper.updateStatus(id, status);
        if (result > 0) {
            syncTournamentCurrentParticipants(registration.getTournamentId());
        }
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

    private boolean isActiveStatus(Integer status) {
        return status != null && status >= 1 && status <= 3;
    }

    private int resolveOccupancyUnits(String eventType) {
        return TournamentTypeHelper.isDoublesEvent(eventType) ? 2 : 1;
    }

    private void ensureTournamentSelectableForRegistration(Tournament tournament) {
        if (tournament == null) {
            throw new RuntimeException("目标赛事不存在");
        }
        Integer status = tournament.getStatus();
        if (status != null && status == 1) {
            return;
        }
        if (status == null) {
            throw new RuntimeException("目标赛事状态异常，仅可选择报名中的赛事");
        }
        switch (status) {
            case 0:
                throw new RuntimeException("目标赛事已取消，仅可选择报名中的赛事");
            case 2:
                throw new RuntimeException("目标赛事已进行中，仅可选择报名中的赛事");
            case 3:
                throw new RuntimeException("目标赛事已结束，仅可选择报名中的赛事");
            default:
                throw new RuntimeException("目标赛事未开放报名，仅可选择报名中的赛事");
        }
    }

    private void ensureTournamentCapacity(Tournament tournament, int additionalUnits, String fullMessage) {
        if (tournament == null) {
            return;
        }
        int occupied = tournamentRegistrationMapper.countOccupiedParticipants(tournament.getId());
        int maxParticipants = tournament.getMaxParticipants() == null ? 0 : tournament.getMaxParticipants();
        if (occupied + additionalUnits > maxParticipants) {
            throw new RuntimeException(fullMessage);
        }
    }

    private void syncTournamentCurrentParticipants(Long tournamentId) {
        if (tournamentId == null) {
            return;
        }
        int occupied = tournamentRegistrationMapper.countOccupiedParticipants(tournamentId);
        tournamentMapper.updateCurrentParticipants(tournamentId, Math.max(0, occupied));
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
        return processPaymentInternal(registrationId, paymentMethod, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processMemberPayment(Long registrationId, String paymentMethod, Long userId) {
        return processPaymentInternal(registrationId, paymentMethod, userId, true);
    }

    private int processPaymentInternal(Long registrationId, String paymentMethod, Long userId, boolean memberPayment) {
        if (!"BALANCE".equals(paymentMethod)) {
            throw new RuntimeException("业务订单仅支持余额支付");
        }

        // 查询报名记录
        TournamentRegistration registration = tournamentRegistrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        Tournament tournamentForPermission = tournamentMapper.findById(registration.getTournamentId());

        if (memberPayment) {
            if (userId == null) {
                throw new RuntimeException("未登录或Token无效");
            }
            Member currentMember = memberMapper.findByUserId(userId);
            if (currentMember == null) {
                throw new RuntimeException("当前用户未关联会员，无法支付赛事报名");
            }
            if (!currentMember.getId().equals(registration.getMemberId())) {
                throw new RuntimeException("权限不足，只能支付自己的赛事报名");
            }
        } else if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || tournamentForPermission == null || !currentVenueId.equals(tournamentForPermission.getVenueId())) {
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

        LocalDateTime now = LocalDateTime.now();
        if (isPendingPaymentExpired(registration.getCreateTime(), now)) {
            throw new RuntimeException("该赛事报名已超出支付时限，系统已自动取消或正在取消，无法支付");
        }
        LocalDateTime expireBefore = resolvePendingPaymentExpireBefore(now);
        int updated = tournamentRegistrationMapper.markPaidIfPending(registrationId, paymentMethod, now, expireBefore);
        if (updated <= 0) {
            throw new RuntimeException(resolvePendingPaymentConflictMessage(tournamentRegistrationMapper.findById(registrationId), "赛事报名"));
        }

        memberConsumeRecordService.createConsumeRecord(
            registration.getMemberId(),
            registration.getEntryFee(),
            "TOURNAMENT",
            registrationId,
            paymentMethod,
            "赛事报名支付：" + registration.getRegistrationNo()
        );

        // 获取场馆ID用于财务记录
        Long venueIdForFinance = tournamentForPermission != null ? tournamentForPermission.getVenueId() : null;

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
        TournamentRegistration refreshedRegistration = tournamentRegistrationMapper.findById(registrationId);
        Integer refreshedPaymentStatus = refreshedRegistration != null ? refreshedRegistration.getPaymentStatus() : null;
        Integer refreshedStatus = refreshedRegistration != null ? refreshedRegistration.getStatus() : null;
        if (refreshedPaymentStatus == null || refreshedPaymentStatus != 1 || refreshedStatus == null || refreshedStatus == 1) {
            org.slf4j.LoggerFactory.getLogger(TournamentRegistrationServiceImpl.class).error(
                    "支付后赛事报名状态异常, registrationId={}, actualPaymentStatus={}, actualStatus={}",
                    registrationId, refreshedPaymentStatus, refreshedStatus
            );
            throw new RuntimeException("支付成功后赛事报名状态异常，请稍后重试");
        }
        if (updated > 0) {
            try {
                Long notifyUserId = null;
                Member m = memberMapper.findById(registration.getMemberId());
                if (m != null && m.getUserId() != null) notifyUserId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(notifyUserId, "tournamentRegistration", registrationId, 2, "已支付", "赛事报名");
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
    private LocalDateTime resolvePendingPaymentExpireBefore(LocalDateTime now) {
        if (paymentAutoCancelProperties == null || !paymentAutoCancelProperties.isEnabled()) {
            return null;
        }
        return now.minus(paymentAutoCancelProperties.getTimeoutDuration());
    }

    private boolean isPendingPaymentExpired(LocalDateTime createTime, LocalDateTime now) {
        LocalDateTime expireBefore = resolvePendingPaymentExpireBefore(now);
        return expireBefore != null && createTime != null && !createTime.isAfter(expireBefore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long registrationId) {
        // 查询报名记录
        TournamentRegistration registration = tournamentRegistrationMapper.findById(registrationId);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        return refundRegistration(registration, true);
    }

    private int refundRegistration(TournamentRegistration registration, boolean enforceAdminPermission) {
        Long registrationId = registration.getId();
        if (registrationId == null) {
            throw new RuntimeException("报名记录不存在");
        }

        if (enforceAdminPermission && !SecurityUtils.isPresident()) {
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

        Integer payStatus = registration.getPaymentStatus();
        if (payStatus == null || (payStatus != 1 && payStatus != 3)) {
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

        Tournament tournamentForRefund = tournamentMapper.findById(registration.getTournamentId());
        Long venueIdForRefund = tournamentForRefund != null ? tournamentForRefund.getVenueId() : null;

        financeService.createFromBusiness(
            Finance.TYPE_TOURNAMENT,
            registrationId,
            registration.getEntryFee(),
            Finance.EXPENSE,
            registration.getPaymentMethod(),
            venueIdForRefund,
            "赛事报名退款：" + registration.getRegistrationNo()
        );

        registration.setPaymentStatus(2);
        registration.setStatus(0);
        registration.setUpdateTime(LocalDateTime.now());
        int result = tournamentRegistrationMapper.update(registration);

        if (result > 0) {
            syncTournamentCurrentParticipants(registration.getTournamentId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCancelExpiredUnpaidOrders(LocalDateTime cutoff) {
        if (cutoff == null) {
            return 0;
        }
        List<Long> registrationIds = tournamentRegistrationMapper.findExpiredUnpaidRegistrationIds(cutoff);
        if (registrationIds == null || registrationIds.isEmpty()) {
            return 0;
        }
        int cancelled = 0;
        for (Long registrationId : registrationIds) {
            TournamentRegistration registration = tournamentRegistrationMapper.findById(registrationId);
            if (registration == null) {
                continue;
            }
            int updated = tournamentRegistrationMapper.cancelExpiredUnpaidRegistration(registrationId, LocalDateTime.now());
            if (updated <= 0) {
                continue;
            }
            cancelled += updated;
            syncTournamentCurrentParticipants(registration.getTournamentId());
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
        return cancelled;
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

    private String resolvePendingPaymentConflictMessage(TournamentRegistration latestRegistration, String orderLabel) {
        if (latestRegistration == null) {
            return orderLabel + "记录不存在";
        }
        Integer payStatus = latestRegistration.getPaymentStatus();
        if (payStatus != null) {
            if (payStatus == 1) {
                return "该" + orderLabel + "已支付，无需重复支付";
            }
            if (payStatus == 2) {
                return "该" + orderLabel + "已退款，无法再次支付";
            }
            if (payStatus == 3) {
                return "该" + orderLabel + "退款申请处理中，无法再次支付";
            }
        }
        if (isPendingPaymentExpired(latestRegistration.getCreateTime(), LocalDateTime.now())) {
            return "该" + orderLabel + "已超出支付时限，系统已自动取消或正在取消，无法支付";
        }
        Integer status = latestRegistration.getStatus();
        if (status != null && status == 0) {
            return "该" + orderLabel + "已超时取消，无法支付";
        }
        return orderLabel + "状态已变化，请刷新后重试";
    }
}
