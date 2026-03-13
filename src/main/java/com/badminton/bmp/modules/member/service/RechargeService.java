package com.badminton.bmp.modules.member.service;

import com.badminton.bmp.modules.member.entity.RechargeRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值服务接口
 */
public interface RechargeService {

    /**
     * 用户自己充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @param method 充值方式（CASH-现金，ALIPAY-支付宝，WECHAT-微信，BANK-银行转账）
     * @return 充值记录对象
     */
    RechargeRecord userRecharge(Long userId, BigDecimal amount, String method);

    /**
     * 管理员为用户充值
     * @param memberId 会员ID
     * @param amount 充值金额
     * @param method 充值方式
     * @param operatorId 操作人ID（管理员ID）
     * @param remark 备注
     * @return 充值记录对象
     */
    RechargeRecord adminRecharge(Long memberId, BigDecimal amount, String method, Long operatorId, String remark);

    /**
     * 根据会员ID查询充值记录列表（支持分页）
     * @param memberId 会员ID
     * @param page 页码
     * @param size 每页数量
     * @return 充值记录列表
     */
    List<RechargeRecord> getRechargeRecordsByMemberId(Long memberId, int page, int size);

    /**
     * 根据会员ID统计充值记录数量
     * @param memberId 会员ID
     * @return 充值记录数量
     */
    int countRechargeRecordsByMemberId(Long memberId);

    /**
     * 查询所有充值记录（支持分页，管理员使用）
     * @param page 页码
     * @param size 每页数量
     * @return 充值记录列表
     */
    List<RechargeRecord> getAllRechargeRecords(int page, int size);

    /**
     * 统计所有充值记录数量
     * @return 充值记录数量
     */
    int countAllRechargeRecords();

    /**
     * 根据多条件查询充值记录（支持分页和筛选）
     * @param memberKeyword 会员关键字（姓名或手机号，可为空）
     * @param startTime 开始时间（可为空）
     * @param endTime 结束时间（可为空）
     * @param page 页码
     * @param size 每页数量
     * @return 充值记录列表
     */
    List<RechargeRecord> getRechargeRecordsWithFilters(String memberKeyword, String startTime, String endTime, int page, int size);

    /**
     * 根据多条件统计充值记录数量
     * @param memberKeyword 会员关键字（姓名或手机号，可为空）
     * @param startTime 开始时间（可为空）
     * @param endTime 结束时间（可为空）
     * @return 充值记录数量
     */
    int countRechargeRecordsWithFilters(String memberKeyword, String startTime, String endTime);
}
