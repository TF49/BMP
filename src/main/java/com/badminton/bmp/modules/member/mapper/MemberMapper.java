package com.badminton.bmp.modules.member.mapper;

import com.badminton.bmp.modules.member.entity.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会员Mapper接口
 * 用于数据库操作
 * 参照UserMapper实现方式，使用MyBatis注解
 */
@Mapper
public interface MemberMapper {

    /**
     * 根据ID查找会员
     * @param id 会员ID
     * @return 会员对象
     */
    @Select("SELECT * FROM sys_member WHERE id = #{id} AND del_flag = 0")
    Member findById(@Param("id") Long id);

    /**
     * 根据用户ID查找会员
     * @param userId 用户ID
     * @return 会员对象
     */
    @Select("SELECT * FROM sys_member WHERE user_id = #{userId} AND del_flag = 0")
    Member findByUserId(@Param("userId") Long userId);

    /**
     * 查找所有会员（仅用户 USER/MEMBER，不含教练/会长等）
     * @return 会员列表
     */
    @Select("SELECT m.* FROM sys_member m " + USER_ROLE_FILTER + "WHERE m.del_flag = 0 ORDER BY m.id")
    List<Member> findAll();

    /**
     * 插入新会员
     * @param member 会员对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_member (user_id, member_name, gender, phone, id_card, " +
            "member_type, member_level, register_time, expire_time, status, total_consumption, " +
            "balance, total_recharge, version, create_time, update_time, del_flag) " +
            "VALUES (#{userId}, #{memberName}, #{gender}, #{phone}, #{idCard}, " +
            "#{memberType}, #{memberLevel}, #{registerTime}, #{expireTime}, #{status}, " +
            "#{totalConsumption}, #{balance}, #{totalRecharge}, #{version}, #{createTime}, #{updateTime}, #{delFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);

    /**
     * 更新会员信息
     * @param member 会员对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET user_id = #{userId}, member_name = #{memberName}, " +
            "gender = #{gender}, phone = #{phone}, id_card = #{idCard}, " +
            "member_type = #{memberType}, member_level = #{memberLevel}, " +
            "register_time = #{registerTime}, expire_time = #{expireTime}, status = #{status}, " +
            "total_consumption = #{totalConsumption}, balance = #{balance}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    int update(Member member);

    /**
     * 更新会员余额（带乐观锁）
     * @param id 会员ID
     * @param balance 新余额
     * @param version 当前版本号（null 会被视为 0）
     * @return 影响的行数（0表示版本冲突）
     */
    @Update("UPDATE sys_member SET balance = #{balance}, version = COALESCE(version, 0) + 1, update_time = NOW() " +
            "WHERE id = #{id} AND COALESCE(version, 0) = #{version}")
    int updateBalanceWithVersion(@Param("id") Long id,
                                  @Param("balance") java.math.BigDecimal balance,
                                  @Param("version") Integer version);

    /**
     * 更新会员余额（不带乐观锁，兼容旧代码）
     * @param id 会员ID
     * @param balance 新余额
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET balance = #{balance}, update_time = NOW() WHERE id = #{id}")
    int updateBalance(@Param("id") Long id, @Param("balance") java.math.BigDecimal balance);

    /**
     * 初始化 version 为 0（用于处理 NULL 值）
     * @param id 会员ID
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET version = 0 WHERE id = #{id} AND (version IS NULL OR version < 0)")
    int updateVersionToZero(@Param("id") Long id);

    /**
     * 更新会员类型和等级（用于升级）
     * @param id 会员ID
     * @param memberType 会员类型
     * @param memberLevel 会员等级
     * @param expireTime 到期时间
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET member_type = #{memberType}, member_level = #{memberLevel}, " +
            "expire_time = #{expireTime}, update_time = NOW() WHERE id = #{id}")
    int updateMemberTypeAndLevel(@Param("id") Long id,
                                 @Param("memberType") String memberType,
                                 @Param("memberLevel") Integer memberLevel,
                                 @Param("expireTime") java.time.LocalDateTime expireTime);

    /**
     * 累加会员累计充值金额
     * @param id 会员ID
     * @param amount 本次充值金额
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET total_recharge = COALESCE(total_recharge, 0) + #{amount}, update_time = NOW() WHERE id = #{id}")
    int updateTotalRecharge(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    /**
     * 更新会员等级（用于累计充值升级）
     * @param id 会员ID
     * @param memberLevel 新等级
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET member_level = #{memberLevel}, update_time = NOW() WHERE id = #{id}")
    int updateMemberLevel(@Param("id") Long id, @Param("memberLevel") Integer memberLevel);

    /**
     * 逻辑删除会员
     * @param id 会员ID
     * @return 影响的行数
     */
    @Update("UPDATE sys_member SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 仅统计/列表「用户」角色：会员管理只面向 sys_user.role IN ('USER','MEMBER')，排除教练/会长/场馆管理者/管理员 */
    static final String USER_ROLE_FILTER = " INNER JOIN sys_user u ON m.user_id = u.id AND u.role IN ('USER','MEMBER') ";

    /**
     * 条件查询（支持分页）- 仅包含关联用户角色为 USER/MEMBER 的会员
     */
    @Select("<script>" +
            "SELECT m.* FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 " +
            "<if test='memberName != null and memberName.trim() != \"\"'>AND m.member_name LIKE CONCAT('%', #{memberName}, '%')</if> " +
            "<if test='phone != null and phone.trim() != \"\"'>AND m.phone LIKE CONCAT('%', #{phone}, '%')</if> " +
            "<if test='memberId != null'>AND m.id = #{memberId}</if> " +
            "<if test='memberType != null and memberType.trim() != \"\"'>AND m.member_type = #{memberType}</if> " +
            "<if test='status != null'>AND m.status = #{status}</if> " +
            "ORDER BY m.id " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Member> findByConditions(@Param("memberName") String memberName,
                                  @Param("phone") String phone,
                                  @Param("memberId") Long memberId,
                                  @Param("memberType") String memberType,
                                  @Param("status") Integer status,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * 条件统计 - 仅包含关联用户角色为 USER/MEMBER 的会员
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 " +
            "<if test='memberName != null and memberName.trim() != \"\"'>AND m.member_name LIKE CONCAT('%', #{memberName}, '%')</if> " +
            "<if test='phone != null and phone.trim() != \"\"'>AND m.phone LIKE CONCAT('%', #{phone}, '%')</if> " +
            "<if test='memberId != null'>AND m.id = #{memberId}</if> " +
            "<if test='memberType != null and memberType.trim() != \"\"'>AND m.member_type = #{memberType}</if> " +
            "<if test='status != null'>AND m.status = #{status}</if> " +
            "</script>")
    int countByConditions(@Param("memberName") String memberName,
                          @Param("phone") String phone,
                          @Param("memberId") Long memberId,
                          @Param("memberType") String memberType,
                          @Param("status") Integer status);

    /**
     * 统计总数 - 仅用户(USER/MEMBER)，不含教练/会长等
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER + "WHERE m.del_flag = 0")
    int countAll();

    /**
     * 按状态统计 - 仅用户(USER/MEMBER)
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER + "WHERE m.del_flag = 0 AND m.status = #{status}")
    int countByStatus(@Param("status") Integer status);

    /**
     * 按类型和等级分组统计 - 仅用户(USER/MEMBER)
     */
    @Select("SELECT m.member_type AS memberType, COALESCE(m.member_level, 0) AS memberLevel, COUNT(*) AS cnt " +
            "FROM sys_member m " + USER_ROLE_FILTER + "WHERE m.del_flag = 0 GROUP BY m.member_type, COALESCE(m.member_level, 0)")
    List<java.util.Map<String, Object>> countByTypeAndLevel();

    /**
     * 统计指定日期注册的会员数量（按 register_time）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 AND DATE(m.register_time) = #{date}")
    int countRegisteredOnDate(@Param("date") java.time.LocalDate date);

    /**
     * 统计指定区间注册的 NORMAL 会员数量（用于“新会员”分组）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 AND m.member_type = 'NORMAL' " +
            "AND DATE(m.register_time) >= #{startDate} AND DATE(m.register_time) <= #{endDate}")
    int countNormalRegisteredBetween(@Param("startDate") java.time.LocalDate startDate,
                                     @Param("endDate") java.time.LocalDate endDate);

    /**
     * 统计 NORMAL 会员总数 - 仅用户(USER/MEMBER)
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER + "WHERE m.del_flag = 0 AND m.member_type = 'NORMAL'")
    int countNormalTotal();

    /**
     * 最近注册会员（用于最近活动）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT m.id, m.member_name AS member_name, m.register_time AS register_time, m.create_time AS create_time FROM sys_member m " +
            USER_ROLE_FILTER + "WHERE m.del_flag = 0 " +
            "ORDER BY COALESCE(m.register_time, m.create_time) DESC LIMIT #{limit}")
    List<java.util.Map<String, Object>> findRecentMembers(@Param("limit") int limit);

    /**
     * 按日期汇总注册人数（用于趋势图）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT DATE(m.register_time) AS date, COUNT(*) AS cnt FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 AND m.register_time IS NOT NULL " +
            "AND DATE(m.register_time) >= #{startDate} AND DATE(m.register_time) <= #{endDate} " +
            "GROUP BY DATE(m.register_time) ORDER BY date")
    List<java.util.Map<String, Object>> countRegisteredByDate(@Param("startDate") java.time.LocalDate startDate,
                                                              @Param("endDate") java.time.LocalDate endDate);

    /**
     * 统计某日期之前累计注册会员数（用于累计趋势）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT COUNT(*) FROM sys_member m " + USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 AND m.register_time IS NOT NULL AND DATE(m.register_time) < #{date}")
    int countRegisteredBeforeDate(@Param("date") java.time.LocalDate date);

    /**
     * 即将到期会员（expire_time 在未来 days 天内）- 仅用户(USER/MEMBER)
     */
    @Select("SELECT m.id, m.member_name AS member_name, m.member_type AS member_type, m.member_level AS member_level, m.expire_time AS expire_time FROM sys_member m " +
            USER_ROLE_FILTER +
            "WHERE m.del_flag = 0 AND m.status = 1 AND m.expire_time IS NOT NULL " +
            "AND m.expire_time > NOW() AND m.expire_time <= DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY m.expire_time ASC")
    List<java.util.Map<String, Object>> findExpiringWithinDays(@Param("days") int days);
}
