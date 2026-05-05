package com.badminton.bmp.modules.member.service.impl;

import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.entity.MemberConsumeRecord;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.booking.mapper.BookingCourtMapper;
import com.badminton.bmp.modules.member.mapper.MemberConsumeRecordMapper;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员消费记录服务实现类
 */
@Service
public class MemberConsumeRecordServiceImpl implements MemberConsumeRecordService {

    @Autowired
    private MemberConsumeRecordMapper memberConsumeRecordMapper;

    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private com.badminton.bmp.modules.booking.mapper.BookingMapper bookingMapper;

    @Autowired
    private BookingCourtMapper bookingCourtMapper;
    
    @Autowired
    private com.badminton.bmp.modules.court.mapper.CourtMapper courtMapper;
    
    @Autowired
    private com.badminton.bmp.modules.course.mapper.CourseMapper courseMapper;

    @Autowired
    private com.badminton.bmp.modules.equipment.mapper.EquipmentMapper equipmentMapper;

    @Autowired
    private com.badminton.bmp.modules.equipment.mapper.EquipmentRentalMapper equipmentRentalMapper;

    @Autowired
    private com.badminton.bmp.modules.tournament.mapper.TournamentMapper tournamentMapper;

    @Autowired
    private com.badminton.bmp.modules.stringing.mapper.StringingServiceMapper stringingServiceMapper;

    /**
     * 创建消费记录（支付时调用）
     * @param memberId 会员ID
     * @param amount 金额（正数）
     * @param businessType 业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT/STRINGING/OTHER）
     * @param businessId 业务ID
     * @param paymentMethod 支付方式
     * @param remark 备注
     * @return 消费记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConsumeRecord(Long memberId, BigDecimal amount, String businessType, Long businessId,
                                   String paymentMethod, String remark) {
        // 1. 查询会员信息
        Member member = memberMapper.findById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 2. 如果使用余额支付，检查余额是否充足并扣减
        BigDecimal beforeBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        BigDecimal afterBalance = beforeBalance;

        if ("BALANCE".equals(paymentMethod)) {
            if (beforeBalance.compareTo(amount) < 0) {
                throw new RuntimeException("余额不足");
            }
            // 扣减余额（使用乐观锁）
            afterBalance = beforeBalance.subtract(amount);
            Integer version = member.getVersion() != null ? member.getVersion() : 0;
            int updateResult = memberMapper.updateBalanceWithVersion(memberId, afterBalance, version);
            if (updateResult == 0) {
                throw new RuntimeException("余额扣减失败，数据已被其他操作修改，请重试");
            }

            // 更新累计消费金额
            BigDecimal totalConsumption = member.getTotalConsumption() != null ? member.getTotalConsumption() : BigDecimal.ZERO;
            member.setTotalConsumption(totalConsumption.add(amount));
            member.setBalance(afterBalance);
            member.setUpdateTime(LocalDateTime.now());
            memberMapper.update(member);
        }

        // 3. 生成消费描述
        String description = generateDescription(businessType, businessId);

        // 4. 写入消费记录
        MemberConsumeRecord record = new MemberConsumeRecord();
        record.setMemberId(memberId);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setDescription(description);
        record.setAmount(amount); // 正数
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(1); // 已支付
        record.setBeforeBalance(beforeBalance);
        record.setAfterBalance(afterBalance);
        record.setRemark(remark);
        // 尝试推导所属场馆ID（便于场馆级查询）
        try {
            Long venueId = null;
            if ("BOOKING".equalsIgnoreCase(businessType) && businessId != null) {
                List<BookingCourt> details = bookingCourtMapper.findByBookingId(businessId);
                if (details != null && !details.isEmpty()) {
                    venueId = details.get(0).getVenueId();
                    if (venueId == null) {
                        com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(details.get(0).getCourtId());
                        if (court != null) venueId = court.getVenueId();
                    }
                }
            } else if ("COURSE".equalsIgnoreCase(businessType) && businessId != null) {
                com.badminton.bmp.modules.course.entity.Course course = courseMapper.findById(businessId);
                if (course != null && course.getCourtId() != null) {
                    com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(course.getCourtId());
                    if (court != null) venueId = court.getVenueId();
                }
            } else if ("EQUIPMENT".equalsIgnoreCase(businessType) && businessId != null) {
                com.badminton.bmp.modules.equipment.entity.Equipment equipment = equipmentMapper.findById(businessId);
                if (equipment != null) venueId = equipment.getVenueId();
            } else if ("TOURNAMENT".equalsIgnoreCase(businessType) && businessId != null) {
                com.badminton.bmp.modules.tournament.entity.Tournament tournament = tournamentMapper.findById(businessId);
                if (tournament != null) venueId = tournament.getVenueId();
            } else if ("STRINGING".equalsIgnoreCase(businessType) && businessId != null) {
                com.badminton.bmp.modules.stringing.entity.StringingService stringingService = stringingServiceMapper.findById(businessId);
                if (stringingService != null) venueId = stringingService.getVenueId();
            }
            record.setVenueId(venueId);
        } catch (Exception ignored) {
            // 不影响核心支付流程
        }
        record.setCreateTime(LocalDateTime.now());

        int result = memberConsumeRecordMapper.insert(record);
        if (result > 0) {
            return record.getId();
        }
        return null;
    }

    /**
     * 生成消费描述
     */
    private String generateDescription(String businessType, Long businessId) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return businessId == null ? "消费" : ("消费 - 订单号: " + businessId);
        }
        if (businessId == null) {
            return getBusinessTypeName(businessType);
        }

        try {
            switch (businessType.toUpperCase()) {
                case "BOOKING":
                    Booking booking = bookingMapper.findById(businessId);
                    if (booking != null) {
                        String prefix = Booking.MODE_PACKAGE.equalsIgnoreCase(booking.getBookingMode()) ? "包场预约 - " : "拼场预约 - ";
                        return prefix + (booking.getBookingNo() != null ? booking.getBookingNo() : "订单号: " + businessId);
                    }
                    break;
                case "COURSE":
                    com.badminton.bmp.modules.course.entity.Course course = courseMapper.findById(businessId);
                    if (course != null) {
                        return "课程预约 - " + (course.getCourseName() != null ? course.getCourseName() : "课程ID: " + businessId);
                    }
                    break;
                case "EQUIPMENT":
                    // businessId 语义为“租借单ID”，优先通过租借单获取器材信息
                    com.badminton.bmp.modules.equipment.entity.EquipmentRental rental = equipmentRentalMapper.findById(businessId);
                    if (rental != null && rental.getEquipmentId() != null) {
                        com.badminton.bmp.modules.equipment.entity.Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
                        String equipmentName = equipment != null ? equipment.getEquipmentName() : null;
                        String rentalNo = rental.getRentalNo();
                        if (equipmentName != null && !equipmentName.trim().isEmpty()) {
                            return rentalNo != null && !rentalNo.trim().isEmpty()
                                    ? ("器材租借 - " + equipmentName + "（" + rentalNo + "）")
                                    : ("器材租借 - " + equipmentName);
                        }
                        if (rentalNo != null && !rentalNo.trim().isEmpty()) {
                            return "器材租借 - " + rentalNo;
                        }
                    }
                    // 兼容旧数据：若 businessId 实际传的是器材ID
                    com.badminton.bmp.modules.equipment.entity.Equipment equipmentFallback = equipmentMapper.findById(businessId);
                    if (equipmentFallback != null) {
                        return "器材租借 - " + (equipmentFallback.getEquipmentName() != null ? equipmentFallback.getEquipmentName() : "器材ID: " + businessId);
                    }
                    break;
                case "TOURNAMENT":
                    com.badminton.bmp.modules.tournament.entity.Tournament tournament = tournamentMapper.findById(businessId);
                    if (tournament != null) {
                        return "赛事报名 - " + (tournament.getTournamentName() != null ? tournament.getTournamentName() : "赛事ID: " + businessId);
                    }
                    break;
                case "STRINGING":
                    com.badminton.bmp.modules.stringing.entity.StringingService stringingService = stringingServiceMapper.findById(businessId);
                    if (stringingService != null) {
                        return "穿线服务 - 订单号: " + businessId;
                    }
                    break;
            }
        } catch (Exception e) {
            // 如果查询失败，返回默认描述
        }

        return getBusinessTypeName(businessType) + " - 订单号: " + businessId;
    }

    /**
     * 获取业务类型名称
     */
    private String getBusinessTypeName(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return "消费";
        }
        switch (businessType.toUpperCase()) {
            case "BOOKING": return "场地预约";
            case "COURSE": return "课程预约";
            case "EQUIPMENT": return "器材租借";
            case "TOURNAMENT": return "赛事报名";
            case "STRINGING": return "穿线服务";
            case "OTHER": return "其他消费";
            default: return "消费";
        }
    }

    /**
     * 创建退款记录（退款时调用）
     * @param memberId 会员ID
     * @param refundAmount 退款金额（正数）
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param paymentMethod 支付方式
     * @param remark 备注
     * @return 消费记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRefundRecord(Long memberId, BigDecimal refundAmount, String businessType, Long businessId, 
                                  String paymentMethod, String remark) {
        // 1. 查询会员信息
        Member member = memberMapper.findById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 2. 如果使用余额支付，回滚余额
        BigDecimal beforeBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        BigDecimal afterBalance = beforeBalance;

        if ("BALANCE".equals(paymentMethod)) {
            // 回滚余额（使用乐观锁）
            afterBalance = beforeBalance.add(refundAmount);
            Integer version = member.getVersion() != null ? member.getVersion() : 0;
            int updateResult = memberMapper.updateBalanceWithVersion(memberId, afterBalance, version);
            if (updateResult == 0) {
                throw new RuntimeException("退款失败，数据已被其他操作修改，请重试");
            }
        }

        // 3. 写入退款记录（负数冲正）
        MemberConsumeRecord record = new MemberConsumeRecord();
        record.setMemberId(memberId);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setDescription("退款 - " + generateDescription(businessType, businessId));
        record.setAmount(refundAmount.negate()); // 负数
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(2); // 已退款
        record.setBeforeBalance(beforeBalance);
        record.setAfterBalance(afterBalance);
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());

        int result = memberConsumeRecordMapper.insert(record);
        if (result > 0) {
            return record.getId();
        }
        return null;
    }
}
