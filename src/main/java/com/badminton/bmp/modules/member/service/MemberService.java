package com.badminton.bmp.modules.member.service;

import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.system.entity.User;

/**
 * 会员服务接口
 */
public interface MemberService {

    /**
     * 根据会员ID查询
     */
    Member findById(Long id);

    /**
     * 根据用户ID查询会员
     */
    Member findByUserId(Long userId);

    /**
     * 为系统用户创建默认的会员记录（NORMAL 类型，余额0）
     * 如果已存在则直接返回，不重复创建。
     */
    Member createDefaultMemberForUser(User user);

    /**
     * 条件查询（分页）
     */
    java.util.List<Member> findByConditions(String memberName, String phone, Long memberId,
                                            String memberType, Integer status, int page, int size);

    /**
     * 条件统计
     */
    int countByConditions(String memberName, String phone, Long memberId, String memberType, Integer status);

    /**
     * 新增会员
     */
    Member add(Member member);

    /**
     * 更新会员
     */
    int update(Member member);

    /**
     * 删除会员（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 统计信息（总数、状态分布、类型+等级分布）
     */
    java.util.Map<String, Object> getStatistics();

    /**
     * 会员分布（用于 Dashboard 饼图）
     * 返回：[{ name, value }]
     */
    java.util.List<java.util.Map<String, Object>> getDistribution();

    /**
     * 会员漏斗（注册→活跃→高频→VIP，用于 Dashboard 漏斗图）
     * 返回：[{ name, value, rate }]，rate 为相对上一级的转化率百分比
     */
    java.util.List<java.util.Map<String, Object>> getFunnel();

    /**
     * 消费记录占位（当前返回空列表）
     */
    java.util.Map<String, Object> getConsumeRecords(Long memberId, int page, int size);

    /**
     * 即将到期会员列表（Dashboard 预警卡片）
     * @param days 未来多少天内到期，默认 30
     * @return [{ memberId, memberName, memberLevel, daysLeft, expireDate }]
     */
    java.util.List<java.util.Map<String, Object>> getExpiringMembers(int days);

    /**
     * 会员来源分布（Dashboard 饼图，按会员类型聚合：会员/普通用户）
     * @return [{ source, count }]
     */
    java.util.List<java.util.Map<String, Object>> getSource();
}
