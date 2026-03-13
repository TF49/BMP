package com.badminton.bmp.modules.system.service;

import com.badminton.bmp.modules.system.entity.User;

import java.util.List;

/**
 * 用户业务接口
 * 参照CMS实现方式
 */
public interface UserService {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);
    
    /**
     * 根据用户ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Long id);
    
    /**
     * 更新最后登录时间
     * @param id 用户ID
     * @return 影响的行数
     */
    int updateLastLoginTime(Long id);
    
    /**
     * 验证密码（仅使用BCrypt加密验证）
     * 安全说明：已移除明文密码兼容，强制使用BCrypt
     * @param rawPassword 原始密码
     * @param encodedPassword 存储的BCrypt加密密码
     * @return 是否匹配
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);

    /**
     * 查找所有用户
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 注册新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    int register(User user);

    /**
     * 根据用户名或身份证号搜索用户，支持分页
     * @param username 用户名
     * @param idCard 身份证号
     * @param role 角色（PRESIDENT/VENUE_MANAGER/USER）
     * @param status 状态（0-禁用，1-启用）
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> findByUsernameOrIdCard(String username, String idCard, String role, Integer status, int page, int size);

    /**
     * 根据用户名或身份证号统计用户数量
     * @param username 用户名
     * @param idCard 身份证号
     * @param role 角色（PRESIDENT/VENUE_MANAGER/USER）
     * @param status 状态（0-禁用，1-启用）
     * @return 用户数量
     */
    int countByUsernameOrIdCard(String username, String idCard, String role, Integer status);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    int update(User user);

    /**
     * 仅更新当前用户可编辑的资料字段（phone、avatar、gender、signature）
     * @param id 用户ID
     * @param phone 手机号
     * @param avatar 头像URL
     * @param gender 性别
     * @param signature 个性签名
     * @return 影响的行数
     */
    int updateProfile(Long id, String phone, String avatar, String gender, String signature);

    /**
     * 仅更新用户密码（用于修改密码）
     * @param id 用户ID
     * @param newEncodedPassword 已加密的新密码
     * @return 影响的行数
     */
    int updatePassword(Long id, String newEncodedPassword);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 根据角色查找用户（用于检查 PRESIDENT 唯一性）
     * @param role 角色
     * @return 用户对象或 null
     */
    User findByRole(String role);
    
    /**
     * 检查是否存在协会会长（PRESIDENT角色）
     * @param excludeUserId 排除的用户ID（用于更新场景，如果当前用户就是协会会长，允许修改）
     * @return 如果已存在协会会长（且不是排除的用户），返回true；否则返回false
     */
    boolean checkPresidentExists(Long excludeUserId);
}
