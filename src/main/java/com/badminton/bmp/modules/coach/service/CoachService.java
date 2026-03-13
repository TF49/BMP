package com.badminton.bmp.modules.coach.service;

import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.system.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 教练业务接口
 */
public interface CoachService {

    /**
     * 根据ID查找教练
     * @param id 教练ID
     * @return 教练对象
     */
    Coach findById(Long id);

    /**
     * 根据用户ID查找教练（COACH 角色与教练档案绑定）
     * @param userId 用户ID
     * @return 教练对象，未绑定时为 null
     */
    Coach findByUserId(Long userId);

    /**
     * 获取当前登录用户的教练ID（仅 COACH 角色且已绑定时返回，否则 null）
     */
    Long getCurrentCoachIdOrNull();

    /**
     * 教练本人更新可编辑档案（姓名、电话、专长、经验、头像）
     * @param coachId 教练ID（必须为当前登录教练）
     * @param coachName 姓名
     * @param phone 电话
     * @param specialty 专长
     * @param experience 经验
     * @param avatar 头像URL
     * @return 影响行数
     */
    int updateSelfProfile(Long coachId, String coachName, String phone, String specialty, String experience, String avatar);

    /**
     * 获取 role=COACH 且尚未绑定教练档案的用户列表（供管理端“关联账号”下拉）
     */
    List<User> getUnboundCoachUsers();

    /**
     * 分页查询教练列表
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 教练列表
     */
    List<Coach> findAll(Long venueId, Integer status, String keyword, Integer gender, int page, int size);

    /**
     * 统计教练数量
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param gender 性别（可选，0-女 1-男）
     * @return 教练数量
     */
    int count(Long venueId, Integer status, String keyword, Integer gender);

    /**
     * 添加教练
     * @param coach 教练对象
     * @return 影响的行数
     */
    int add(Coach coach);

    /**
     * 更新教练信息
     * @param coach 教练对象
     * @return 影响的行数
     */
    int update(Coach coach);

    /**
     * 删除教练（逻辑删除）
     * @param id 教练ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 更新教练状态
     * @param id 教练ID
     * @param status 新状态（0-停用，1-正常）
     * @return 影响的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取教练统计信息
     * @return 统计信息Map，包含总教练数、各状态数量
     */
    Map<String, Object> getStatistics();
}
