package com.badminton.bmp.modules.stringing.mapper;

import com.badminton.bmp.modules.stringing.entity.StringingService;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 穿线服务Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface StringingServiceMapper {

    /**
     * 根据ID查找穿线服务记录
     * @param id 服务ID
     * @return 服务对象
     */
    @Select("SELECT ss.*, m.member_name, m.phone as member_phone, u.username as user_name, v.venue_name, e.equipment_name as string_equipment_name " +
            "FROM biz_stringing_service ss " +
            "LEFT JOIN sys_member m ON ss.member_id = m.id " +
            "LEFT JOIN sys_user u ON ss.user_id = u.id " +
            "LEFT JOIN sys_venue v ON ss.venue_id = v.id " +
            "LEFT JOIN sys_equipment e ON ss.string_id = e.id " +
            "WHERE ss.id = #{id} AND ss.del_flag = 0")
    StringingService findById(@Param("id") Long id);

    /**
     * 根据服务单号查找穿线服务记录
     * @param serviceNo 服务单号
     * @return 服务对象
     */
    @Select("SELECT ss.*, m.member_name, m.phone as member_phone, u.username as user_name, v.venue_name, e.equipment_name as string_equipment_name " +
            "FROM biz_stringing_service ss " +
            "LEFT JOIN sys_member m ON ss.member_id = m.id " +
            "LEFT JOIN sys_user u ON ss.user_id = u.id " +
            "LEFT JOIN sys_venue v ON ss.venue_id = v.id " +
            "LEFT JOIN sys_equipment e ON ss.string_id = e.id " +
            "WHERE ss.service_no = #{serviceNo} AND ss.del_flag = 0")
    StringingService findByServiceNo(@Param("serviceNo") String serviceNo);

    /**
     * 查找所有穿线服务记录（支持条件筛选和分页）
     * @param venueId 场馆ID（可选，用于权限过滤）
     * @param memberId 会员ID（可选）
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配服务单号、会员姓名、会员手机号、场馆名称）
     * @param createTimeStart 创建时间起（可选）
     * @param createTimeEnd 创建时间止（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 服务记录列表
     */
    @Select("<script>" +
            "SELECT ss.*, m.member_name, u.username as user_name, v.venue_name, e.equipment_name as string_equipment_name " +
            "FROM biz_stringing_service ss " +
            "LEFT JOIN sys_member m ON ss.member_id = m.id " +
            "LEFT JOIN sys_user u ON ss.user_id = u.id " +
            "LEFT JOIN sys_venue v ON ss.venue_id = v.id " +
            "LEFT JOIN sys_equipment e ON ss.string_id = e.id " +
            "WHERE ss.del_flag = 0 " +
            "<if test='venueId != null'> AND ss.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND ss.member_id = #{memberId} </if>" +
            "<if test='userId != null'> AND ss.user_id = #{userId} </if>" +
            "<if test='status != null'> AND ss.status = #{status} </if>" +
            "<if test='createTimeStart != null and createTimeStart != \"\"'> AND ss.create_time &gt;= #{createTimeStart} </if>" +
            "<if test='createTimeEnd != null and createTimeEnd != \"\"'> AND ss.create_time &lt;= #{createTimeEnd} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (ss.service_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY ss.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<StringingService> findAll(@Param("venueId") Long venueId,
                                  @Param("memberId") Long memberId,
                                  @Param("userId") Long userId,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword,
                                  @Param("createTimeStart") String createTimeStart,
                                  @Param("createTimeEnd") String createTimeEnd,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * 统计穿线服务记录数量（支持条件筛选）
     * @param venueId 场馆ID（可选，用于权限过滤）
     * @param memberId 会员ID（可选）
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配服务单号、会员姓名、会员手机号、场馆名称）
     * @param createTimeStart 创建时间起（可选）
     * @param createTimeEnd 创建时间止（可选）
     * @return 服务记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_stringing_service ss " +
            "LEFT JOIN sys_member m ON ss.member_id = m.id " +
            "LEFT JOIN sys_venue v ON ss.venue_id = v.id " +
            "WHERE ss.del_flag = 0 " +
            "<if test='venueId != null'> AND ss.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND ss.member_id = #{memberId} </if>" +
            "<if test='userId != null'> AND ss.user_id = #{userId} </if>" +
            "<if test='status != null'> AND ss.status = #{status} </if>" +
            "<if test='createTimeStart != null and createTimeStart != \"\"'> AND ss.create_time &gt;= #{createTimeStart} </if>" +
            "<if test='createTimeEnd != null and createTimeEnd != \"\"'> AND ss.create_time &lt;= #{createTimeEnd} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (ss.service_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("memberId") Long memberId,
              @Param("userId") Long userId,
              @Param("status") Integer status,
              @Param("keyword") String keyword,
              @Param("createTimeStart") String createTimeStart,
              @Param("createTimeEnd") String createTimeEnd);

    /**
     * 插入新穿线服务记录
     * @param service 服务对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO biz_stringing_service (service_no, member_id, user_id, venue_id, " +
            "racket_brand, racket_model, racket_description, string_id, string_name, is_own_string, " +
            "pound, stringing_method, has_breakage, has_collapse, status, service_price, " +
            "payment_method, payment_status, remark, create_time, update_time, del_flag) " +
            "VALUES (#{serviceNo}, #{memberId}, #{userId}, #{venueId}, " +
            "#{racketBrand}, #{racketModel}, #{racketDescription}, #{stringId}, #{stringName}, #{isOwnString}, " +
            "#{pound}, #{stringingMethod}, #{hasBreakage}, #{hasCollapse}, #{status}, #{servicePrice}, " +
            "#{paymentMethod}, #{paymentStatus}, #{remark}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StringingService service);

    /**
     * 更新穿线服务记录
     * @param service 服务对象
     * @return 影响的行数
     */
    @Update("UPDATE biz_stringing_service SET member_id = #{memberId}, venue_id = #{venueId}, " +
            "racket_brand = #{racketBrand}, racket_model = #{racketModel}, racket_description = #{racketDescription}, " +
            "string_id = #{stringId}, string_name = #{stringName}, is_own_string = #{isOwnString}, " +
            "pound = #{pound}, stringing_method = #{stringingMethod}, has_breakage = #{hasBreakage}, " +
            "has_collapse = #{hasCollapse}, status = #{status}, service_price = #{servicePrice}, " +
            "payment_method = #{paymentMethod}, payment_status = #{paymentStatus}, remark = #{remark}, " +
            "start_time = #{startTime}, update_time = #{updateTime} " +
            "WHERE id = #{id} AND del_flag = 0")
    int update(StringingService service);

    /**
     * 逻辑删除穿线服务记录
     * @param id 服务ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_stringing_service SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新穿线服务状态
     * @param id 服务ID
     * @param status 新状态
     * @param startTime 开始时间（可选，仅在状态为"正在穿线"时设置）
     * @return 影响的行数
     */
    @Update("<script>" +
            "UPDATE biz_stringing_service SET status = #{status}, update_time = NOW() " +
            "<if test='startTime != null'>, start_time = #{startTime} </if>" +
            "WHERE id = #{id} AND del_flag = 0" +
            "</script>")
    int updateStatus(@Param("id") Long id, 
                     @Param("status") Integer status,
                     @Param("startTime") java.time.LocalDateTime startTime);

    /**
     * 检查服务单号是否已存在
     * @param serviceNo 服务单号
     * @param excludeId 排除的服务ID（用于更新时排除自身）
     * @return 存在返回true，否则返回false
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_stringing_service " +
            "WHERE service_no = #{serviceNo} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByServiceNo(@Param("serviceNo") String serviceNo,
                              @Param("excludeId") Long excludeId);

    /**
     * 统计各状态的服务记录数量
     * @param venueId 场馆ID（可选，用于权限过滤）
     * @return 各状态数量
     */
    @Select("<script>" +
            "SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_stringing_service " +
            "WHERE del_flag = 0 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "GROUP BY status" +
            "</script>")
    List<Map<String, Object>> countByStatus(@Param("venueId") Long venueId);

    /**
     * 统计所有服务记录数量
     * @param venueId 场馆ID（可选，用于权限过滤）
     * @return 服务记录总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_stringing_service WHERE del_flag = 0 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "</script>")
    int countAll(@Param("venueId") Long venueId);

    /**
     * 查询当天所有服务单号（用于单号生成，不进行权限过滤）。
     * 包含已软删除记录，避免重复生成已存在的单号导致唯一键冲突。
     * @param prefix 服务单号前缀（如：SR20250125）
     * @return 服务记录列表（仅含 service_no）
     */
    @Select("SELECT service_no FROM biz_stringing_service " +
            "WHERE service_no LIKE CONCAT(#{prefix}, '%') " +
            "ORDER BY service_no DESC")
    List<StringingService> findByServiceNoPrefix(@Param("prefix") String prefix);
}
