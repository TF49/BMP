package com.badminton.bmp.modules.system.mapper;

import com.badminton.bmp.modules.system.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper接口
 * 用于数据库操作
 * 参照CMS实现方式，使用MyBatis注解
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 更新最后登录时间
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 影响的行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime} WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 查找所有用户
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user")
    List<User> findAll();

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_user (username, password, phone, id_card, avatar, gender, signature, role, venue_id, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{phone}, #{idCard}, #{avatar}, #{gender}, #{signature}, #{role}, #{venueId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 获取用户表中的最大ID
     * @return 最大ID
     */
    @Select("SELECT COALESCE(MAX(id), 0) FROM sys_user")
    Long getMaxId();

    /**
     * 根据用户名或身份证号搜索用户，支持分页
     * @param username 用户名
     * @param idCard 身份证号
     * @param role 角色（PRESIDENT/VENUE_MANAGER/USER）
     * @param status 状态（0-禁用，1-启用）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE 1=1 " +
            "AND (#{username} IS NULL OR #{username} = '' OR username LIKE CONCAT('%', #{username}, '%')) " +
            "AND (#{idCard} IS NULL OR #{idCard} = '' OR id_card LIKE CONCAT('%', #{idCard}, '%')) " +
            "AND (#{role} IS NULL OR role = #{role}) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY id LIMIT #{offset}, #{limit}")
    List<User> findByUsernameOrIdCard(@Param("username") String username,
                                     @Param("idCard") String idCard,
                                     @Param("role") String role,
                                     @Param("status") Integer status,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 根据用户名或身份证号统计用户数量
     * @param username 用户名
     * @param idCard 身份证号
     * @param role 角色（PRESIDENT/VENUE_MANAGER/USER）
     * @param status 状态（0-禁用，1-启用）
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE 1=1 " +
            "AND (#{username} IS NULL OR #{username} = '' OR username LIKE CONCAT('%', #{username}, '%')) " +
            "AND (#{idCard} IS NULL OR #{idCard} = '' OR id_card LIKE CONCAT('%', #{idCard}, '%')) " +
            "AND (#{role} IS NULL OR role = #{role}) " +
            "AND (#{status} IS NULL OR status = #{status})")
    int countByUsernameOrIdCard(@Param("username") String username,
                               @Param("idCard") String idCard,
                               @Param("role") String role,
                               @Param("status") Integer status);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_user SET username = #{username}, password = #{password}, " +
            "phone = #{phone}, id_card = #{idCard}, avatar = #{avatar}, gender = #{gender}, signature = #{signature}, " +
            "role = #{role}, venue_id = #{venueId}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(User user);

    /**
     * 仅更新当前用户可编辑的资料字段（不更新 username、password、role、status、venueId）
     */
    @Update("UPDATE sys_user SET phone = #{phone}, avatar = #{avatar}, gender = #{gender}, signature = #{signature}, update_time = #{updateTime} WHERE id = #{id}")
    int updateProfile(@Param("id") Long id, @Param("phone") String phone, @Param("avatar") String avatar,
                      @Param("gender") String gender, @Param("signature") String signature, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 仅更新密码与更新时间（用于修改密码）
     */
    @Update("UPDATE sys_user SET password = #{password}, update_time = #{updateTime} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据角色查找用户（用于检查 PRESIDENT 唯一性）
     * @param role 角色
     * @return 用户对象或 null
     */
    @Select("SELECT * FROM sys_user WHERE role = #{role} LIMIT 1")
    User findByRole(@Param("role") String role);

    /**
     * 根据角色查找所有用户（用于教练端“未绑定教练档案”的 COACH 用户列表）
     * @param role 角色
     * @return 用户列表
     */
    @Select("SELECT id, username, role, status FROM sys_user WHERE role = #{role} ORDER BY id")
    List<User> findAllByRole(@Param("role") String role);

    /**
     * 仅更新用户角色（用于充值达标后 USER 升级为 MEMBER）
     * @param id 用户ID
     * @param role 角色
     * @return 影响的行数
     */
    @Update("UPDATE sys_user SET role = #{role}, update_time = NOW() WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
