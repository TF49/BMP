package com.badminton.bmp.modules.member.service;

import java.math.BigDecimal;

/**
 * 会员消费记录服务接口
 */
public interface MemberConsumeRecordService {

    /**
     * 创建消费记录（支付时调用）
     * @param memberId 会员ID
     * @param amount 金额（正数）
     * @param businessType 业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT/OTHER）
     * @param businessId 业务ID
     * @param paymentMethod 支付方式
     * @param remark 备注
     * @return 消费记录ID
     */
    Long createConsumeRecord(Long memberId, BigDecimal amount, String businessType, Long businessId, 
                            String paymentMethod, String remark);

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
    Long createRefundRecord(Long memberId, BigDecimal refundAmount, String businessType, Long businessId, 
                           String paymentMethod, String remark);
}
