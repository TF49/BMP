package com.badminton.bmp.modules.equipment.mapper;

import com.badminton.bmp.modules.equipment.entity.EquipmentRental;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 器材租借Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface EquipmentRentalMapper {

    /**
     * 根据ID查找器材租借记录
     * @param id 租借ID
     * @return 租借对象
     */
    @Select("SELECT er.*, m.member_name, e.equipment_name, e.equipment_code " +
            "FROM biz_equipment_rental er " +
            "LEFT JOIN sys_member m ON er.member_id = m.id " +
            "LEFT JOIN sys_equipment e ON er.equipment_id = e.id " +
            "WHERE er.id = #{id} AND er.del_flag = 0")
    EquipmentRental findById(@Param("id") Long id);

    /**
     * 查找所有器材租借记录（支持条件筛选和分页）
     * @param memberId 会员ID（可选）
     * @param equipmentId 器材ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配租借单号、会员姓名、器材名称）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 租借记录列表
     */
    @Select("<script>" +
            "SELECT er.*, m.member_name, e.equipment_name, e.equipment_code " +
            "FROM biz_equipment_rental er " +
            "LEFT JOIN sys_member m ON er.member_id = m.id " +
            "LEFT JOIN sys_equipment e ON er.equipment_id = e.id " +
            "WHERE er.del_flag = 0 " +
            "<if test='venueId != null'> AND e.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND er.member_id = #{memberId} </if>" +
            "<if test='equipmentId != null'> AND er.equipment_id = #{equipmentId} </if>" +
            "<if test='status != null'> AND er.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (er.rental_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.equipment_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY er.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<EquipmentRental> findAll(@Param("venueId") Long venueId,
                                  @Param("memberId") Long memberId,
                                  @Param("equipmentId") Long equipmentId,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * 统计器材租借记录数量（支持条件筛选）
     * @param memberId 会员ID（可选）
     * @param equipmentId 器材ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配租借单号、会员姓名、器材名称）
     * @return 租借记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_equipment_rental er " +
            "LEFT JOIN sys_member m ON er.member_id = m.id " +
            "LEFT JOIN sys_equipment e ON er.equipment_id = e.id " +
            "WHERE er.del_flag = 0 " +
            "<if test='venueId != null'> AND e.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND er.member_id = #{memberId} </if>" +
            "<if test='equipmentId != null'> AND er.equipment_id = #{equipmentId} </if>" +
            "<if test='status != null'> AND er.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (er.rental_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.equipment_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("memberId") Long memberId,
              @Param("equipmentId") Long equipmentId,
              @Param("status") Integer status,
              @Param("keyword") String keyword);

    /**
     * 插入新器材租借记录
     * @param rental 租借对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO biz_equipment_rental (rental_no, member_id, equipment_id, quantity, " +
            "rental_date, expected_return_date, return_date, rental_amount, unit_price, deposit_amount, duration_hours, payment_method, payment_status, status, " +
            "remark, create_time, update_time, del_flag) " +
            "VALUES (#{rentalNo}, #{memberId}, #{equipmentId}, #{quantity}, " +
            "#{rentalDate}, #{expectedReturnDate}, #{returnDate}, #{rentalAmount}, #{unitPrice}, #{depositAmount}, #{durationHours}, #{paymentMethod}, #{paymentStatus}, " +
            "#{status}, #{remark}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EquipmentRental rental);

    /**
     * 更新器材租借记录
     * @param rental 租借对象
     * @return 影响的行数
     */
    @Update("UPDATE biz_equipment_rental SET member_id = #{memberId}, equipment_id = #{equipmentId}, " +
            "quantity = #{quantity}, rental_date = #{rentalDate}, expected_return_date = #{expectedReturnDate}, return_date = #{returnDate}, " +
            "rental_amount = #{rentalAmount}, unit_price = #{unitPrice}, deposit_amount = #{depositAmount}, duration_hours = #{durationHours}, payment_method = #{paymentMethod}, " +
            "payment_status = #{paymentStatus}, status = #{status}, remark = #{remark}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(EquipmentRental rental);

    /**
     * 逻辑删除器材租借记录
     * @param id 租借ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_equipment_rental SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新器材租借状态
     * @param id 租借ID
     * @param status 新状态
     * @return 影响的行数
     */
    @Update("UPDATE biz_equipment_rental SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查租借单号是否已存在
     * @param rentalNo 租借单号
     * @param excludeId 排除的租借ID（用于更新时排除自身）
     * @return 存在返回true，否则返回false
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_equipment_rental " +
            "WHERE rental_no = #{rentalNo} " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByRentalNo(@Param("rentalNo") String rentalNo,
                            @Param("excludeId") Long excludeId);

    /**
     * 根据前缀获取当前数据库中最大的租借单号
     * 说明：
     * - 不区分是否逻辑删除，确保整个系统生命周期内租借单号全局唯一；
     * - 利用前缀 + 递增序号的字符串排序特性，直接按 rental_no 倒序取第一条即可。
     *
     * @param prefix 单号前缀（例如 ER20260213）
     * @return 最大的租借单号，如不存在则返回 null
     */
    @Select("SELECT rental_no FROM biz_equipment_rental " +
            "WHERE rental_no LIKE CONCAT(#{prefix}, '%') " +
            "ORDER BY rental_no DESC LIMIT 1")
    String findMaxRentalNoByPrefix(@Param("prefix") String prefix);

    /**
     * 根据日期范围查询租借记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 租借记录列表
     */
    @Select("SELECT er.*, m.member_name, e.equipment_name, e.equipment_code " +
            "FROM biz_equipment_rental er " +
            "LEFT JOIN sys_member m ON er.member_id = m.id " +
            "LEFT JOIN sys_equipment e ON er.equipment_id = e.id " +
            "WHERE er.rental_date BETWEEN #{startDate} AND #{endDate} " +
            "AND er.del_flag = 0 " +
            "ORDER BY er.rental_date DESC, er.create_time DESC")
    List<EquipmentRental> findByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 统计各状态的租借记录数量
     * @return 各状态数量
     */
    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_equipment_rental " +
            "WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计所有租借记录数量
     * @return 租借记录总数
     */
    @Select("SELECT COUNT(*) FROM biz_equipment_rental WHERE del_flag = 0")
    int countAll();

    /**
     * 定时任务：查询「租借中」且已过应还时间的租借ID（用于自动改为逾期）
     * 条件：status=1 且 expected_return_date 不为空且 expected_return_date < 当前日期
     */
    @Select("SELECT id FROM biz_equipment_rental WHERE del_flag = 0 AND status = 1 " +
            "AND expected_return_date IS NOT NULL AND expected_return_date < CURDATE()")
    List<Long> findOverdueRentalIds();

    /**
     * 按器材统计租借次数（按 quantity 汇总，用于 Dashboard 饼图）
     */
    @Select("<script>" +
            "SELECT e.equipment_name AS name, SUM(er.quantity) AS cnt " +
            "FROM biz_equipment_rental er " +
            "LEFT JOIN sys_equipment e ON er.equipment_id = e.id " +
            "WHERE er.del_flag = 0 " +
            "<if test='venueId != null'> AND e.venue_id = #{venueId} </if>" +
            "GROUP BY er.equipment_id, e.equipment_name " +
            "ORDER BY cnt DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> sumQuantityByEquipment(@Param("venueId") Long venueId,
                                                     @Param("limit") int limit);

    /**
     * 统计指定器材的指定状态的租借记录数量（用于删除前检查）
     * @param equipmentId 器材ID
     * @param status 状态（1-租借中）
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM biz_equipment_rental " +
            "WHERE equipment_id = #{equipmentId} AND status = #{status} AND del_flag = 0")
    int countByEquipmentIdAndStatus(@Param("equipmentId") Long equipmentId, 
                                     @Param("status") Integer status);
}
