package com.badminton.bmp.modules.member.mapper;

import com.badminton.bmp.modules.member.entity.RechargeRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 充值记录Mapper接口
 * 用于数据库操作
 * 参照UserMapper实现方式，使用MyBatis注解
 */
@Mapper
public interface RechargeRecordMapper {

    /**
     * 根据ID查找充值记录
     * @param id 充值记录ID
     * @return 充值记录对象
     */
    @Select("SELECT * FROM biz_recharge_record WHERE id = #{id}")
    RechargeRecord findById(@Param("id") Long id);

    /**
     * 根据会员ID查找充值记录列表
     * @param memberId 会员ID
     * @return 充值记录列表
     */
    @Select("SELECT * FROM biz_recharge_record WHERE member_id = #{memberId} " +
            "ORDER BY recharge_time DESC")
    List<RechargeRecord> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据会员ID查找充值记录列表（支持分页）
     * @param memberId 会员ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 充值记录列表
     */
    @Select("SELECT * FROM biz_recharge_record WHERE member_id = #{memberId} " +
            "ORDER BY recharge_time DESC LIMIT #{offset}, #{limit}")
    List<RechargeRecord> findByMemberIdWithPage(@Param("memberId") Long memberId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    /**
     * 根据会员ID统计充值记录数量
     * @param memberId 会员ID
     * @return 充值记录数量
     */
    @Select("SELECT COUNT(*) FROM biz_recharge_record WHERE member_id = #{memberId}")
    int countByMemberId(@Param("memberId") Long memberId);

    /**
     * 查找所有充值记录（支持分页）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 充值记录列表
     */
    @Select("<script>" +
            "SELECT * FROM biz_recharge_record " +
            "WHERE 1=1 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "ORDER BY recharge_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<RechargeRecord> findAllWithPage(@Param("venueId") Long venueId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计所有充值记录数量
     * @return 充值记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_recharge_record " +
            "WHERE 1=1 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "</script>")
    int countAll(@Param("venueId") Long venueId);

    /**
     * 查询指定日期的充值记录数量（用于生成充值单号）
     * @param datePrefix 日期前缀（格式：yyyyMMdd）
     * @return 当天充值记录数量
     */
    @Select("SELECT COUNT(*) FROM biz_recharge_record WHERE recharge_no LIKE CONCAT('RC', #{datePrefix}, '%')")
    int countByDatePrefix(@Param("datePrefix") String datePrefix);

    /**
     * 根据多条件查询充值记录（支持分页和筛选）
     * @param venueId 场馆ID（可为空）
     * @param memberKeyword 会员关键字（姓名或手机号，可为空）
     * @param startTime 开始时间（可为空）
     * @param endTime 结束时间（可为空）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 充值记录列表
     */
    @Select("<script>" +
            "SELECT r.* FROM biz_recharge_record r " +
            "LEFT JOIN sys_member m ON r.member_id = m.id " +
            "WHERE 1=1 " +
            "<if test='venueId != null'> AND r.venue_id = #{venueId} </if>" +
            "<if test='memberKeyword != null and memberKeyword != \"\"'> " +
            "  AND (m.member_name LIKE CONCAT('%', #{memberKeyword}, '%') " +
            "       OR m.phone LIKE CONCAT('%', #{memberKeyword}, '%')) " +
            "</if>" +
            "<if test='startTime != null'> AND r.recharge_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND r.recharge_time &lt;= #{endTime} </if>" +
            "ORDER BY r.recharge_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<RechargeRecord> findWithFilters(@Param("venueId") Long venueId,
                                         @Param("memberKeyword") String memberKeyword,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 根据多条件统计充值记录数量
     * @param venueId 场馆ID（可为空）
     * @param memberKeyword 会员关键字（姓名或手机号，可为空）
     * @param startTime 开始时间（可为空）
     * @param endTime 结束时间（可为空）
     * @return 充值记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_recharge_record r " +
            "LEFT JOIN sys_member m ON r.member_id = m.id " +
            "WHERE 1=1 " +
            "<if test='venueId != null'> AND r.venue_id = #{venueId} </if>" +
            "<if test='memberKeyword != null and memberKeyword != \"\"'> " +
            "  AND (m.member_name LIKE CONCAT('%', #{memberKeyword}, '%') " +
            "       OR m.phone LIKE CONCAT('%', #{memberKeyword}, '%')) " +
            "</if>" +
            "<if test='startTime != null'> AND r.recharge_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND r.recharge_time &lt;= #{endTime} </if>" +
            "</script>")
    int countWithFilters(@Param("venueId") Long venueId,
                        @Param("memberKeyword") String memberKeyword,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime);

    /**
     * 插入新充值记录
     * @param record 充值记录对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO biz_recharge_record (recharge_no, member_id, user_id, recharge_amount, " +
            "recharge_method, recharge_time, operator_type, operator_id, remark, is_upgraded, is_level_upgraded, venue_id, create_time) " +
            "VALUES (#{rechargeNo}, #{memberId}, #{userId}, #{rechargeAmount}, #{rechargeMethod}, " +
            "#{rechargeTime}, #{operatorType}, #{operatorId}, #{remark}, #{isUpgraded}, #{isLevelUpgraded}, #{venueId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargeRecord record);

}
