package com.badminton.bmp.modules.finance.service;

import java.util.Map;

/**
 * 财务对账服务接口
 */
public interface FinanceReconciliationService {

    /**
     * 执行全面对账检查
     * 检查所有业务模块的支付记录是否都有对应的财务记录
     * @return 对账结果报告
     */
    Map<String, Object> performFullReconciliation();

    /**
     * 检查场地预约模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileBookingFinance();

    /**
     * 检查课程预约模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileCourseFinance();

    /**
     * 检查器材租借模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileEquipmentFinance();

    /**
     * 检查赛事报名模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileTournamentFinance();

    /**
     * 检查穿线服务模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileStringingFinance();

    /**
     * 检查会员充值模块的财务对账
     * @return 对账结果
     */
    Map<String, Object> reconcileRechargeFinance();
}
