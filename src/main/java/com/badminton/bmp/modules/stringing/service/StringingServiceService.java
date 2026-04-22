package com.badminton.bmp.modules.stringing.service;

import com.badminton.bmp.modules.stringing.entity.StringingService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 穿线服务业务接口
 */
public interface StringingServiceService {

    /**
     * 根据ID查找穿线服务记录
     * @param id 服务ID
     * @return 服务对象
     */
    StringingService findById(Long id);

    /**
     * 根据服务单号查找穿线服务记录
     * @param serviceNo 服务单号
     * @return 服务对象
     */
    StringingService findByServiceNo(String serviceNo);

    /**
     * 分页查询穿线服务记录列表（支持权限过滤）
     * @param memberId 会员ID（可选）
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param venueId 场馆ID（可选，会长筛选用）
     * @param createTimeStart 创建时间起（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param createTimeEnd 创建时间止（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 服务记录列表
     */
    List<StringingService> findAll(Long memberId, Long userId, Integer status, String keyword,
                                   Long venueId, String createTimeStart, String createTimeEnd,
                                   int page, int size);

    /**
     * 统计穿线服务记录数量（支持权限过滤）
     * @param memberId 会员ID（可选）
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param venueId 场馆ID（可选）
     * @param createTimeStart 创建时间起（可选）
     * @param createTimeEnd 创建时间止（可选）
     * @return 服务记录数量
     */
    int count(Long memberId, Long userId, Integer status, String keyword,
              Long venueId, String createTimeStart, String createTimeEnd);

    /**
     * 添加穿线服务记录
     * @param service 服务对象
     * @return 影响的行数
     */
    int add(StringingService service);

    /**
     * 更新穿线服务记录
     * @param service 服务对象
     * @return 影响的行数
     */
    int update(StringingService service);

    /**
     * 删除穿线服务记录（逻辑删除）
     * @param id 服务ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 更新穿线服务状态
     * @param id 服务ID
     * @param status 新状态（0-已取消，1-等待穿线，2-正在穿线，3-已完成）
     * @return 影响的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 用户取消自己的穿线服务
     * @param serviceId 服务ID
     * @return 影响的行数
     */
    int cancelByUser(Long serviceId);

    /**
     * 获取穿线服务统计信息（支持权限过滤）
     * @return 统计信息Map，包含总服务数、各状态数量
     */
    Map<String, Object> getStatistics();

    /**
     * 生成服务单号
     * @return 服务单号（格式：SR+日期+序号，如SR20250125001）
     */
    String generateServiceNo();

    /**
     * 计算服务价格
     * @param stringId 线材ID（可选，如果从系统选择）
     * @param isOwnString 是否自带线材（0-否，1-是）
     * @return 服务价格（线材价格 + 手工费20元，如果自带线材则只收手工费）
     */
    BigDecimal calculatePrice(Long stringId, Integer isOwnString);

    /**
     * 处理支付（含余额扣减、消费记录、财务流水）
     * @param serviceId 穿线服务ID
     * @param paymentMethod 支付方式（仅支持 BALANCE）
     * @return 影响的行数
     */
    int processPayment(Long serviceId, String paymentMethod);

    /**
     * 处理退款（含余额回滚、消费记录冲正、财务流水）
     * @param serviceId 穿线服务ID
     * @return 影响的行数
     */
    int processRefund(Long serviceId);
}
