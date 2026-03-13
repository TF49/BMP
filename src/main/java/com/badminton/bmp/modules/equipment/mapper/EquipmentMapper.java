package com.badminton.bmp.modules.equipment.mapper;

import com.badminton.bmp.modules.equipment.entity.Equipment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 器材Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface EquipmentMapper {

    /**
     * 根据ID查找器材
     * @param id 器材ID
     * @return 器材对象
     */
    @Select("SELECT e.*, v.venue_name FROM sys_equipment e " +
            "LEFT JOIN sys_venue v ON e.venue_id = v.id " +
            "WHERE e.id = #{id} AND e.del_flag = 0")
    Equipment findById(@Param("id") Long id);

    /**
     * 查找所有器材（支持条件筛选和分页）
     * @param equipmentType 器材类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配器材编号或名称）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 器材列表
     */
    @Select("<script>" +
            "SELECT e.*, v.venue_name FROM sys_equipment e " +
            "LEFT JOIN sys_venue v ON e.venue_id = v.id " +
            "WHERE e.del_flag = 0 " +
            "<if test='venueId != null'> AND e.venue_id = #{venueId} </if>" +
            "<if test='equipmentType != null and equipmentType != \"\"'> AND e.equipment_type = #{equipmentType} </if>" +
            "<if test='status != null'> AND e.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (e.equipment_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.equipment_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY e.venue_id, e.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Equipment> findAll(@Param("venueId") Long venueId,
                            @Param("equipmentType") String equipmentType,
                            @Param("status") Integer status,
                            @Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    /**
     * 统计器材数量（支持条件筛选）
     * @param equipmentType 器材类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配器材编号或名称）
     * @return 器材数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_equipment e " +
            "WHERE e.del_flag = 0 " +
            "<if test='venueId != null'> AND e.venue_id = #{venueId} </if>" +
            "<if test='equipmentType != null and equipmentType != \"\"'> AND e.equipment_type = #{equipmentType} </if>" +
            "<if test='status != null'> AND e.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (e.equipment_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR e.equipment_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("equipmentType") String equipmentType,
              @Param("status") Integer status,
              @Param("keyword") String keyword);

    /**
     * 插入新器材
     * @param equipment 器材对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_equipment (equipment_code, equipment_image, equipment_name, equipment_type, " +
            "brand, price, rental_price, rental_deposit, total_quantity, available_quantity, version, status, description, venue_id, " +
            "create_time, update_time, del_flag) " +
            "VALUES (#{equipmentCode}, #{equipmentImage}, #{equipmentName}, #{equipmentType}, " +
            "#{brand}, #{price}, #{rentalPrice}, #{rentalDeposit}, #{totalQuantity}, #{availableQuantity}, 0, #{status}, " +
            "#{description}, #{venueId}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Equipment equipment);

    /**
     * 更新器材信息
     * @param equipment 器材对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment SET equipment_code = #{equipmentCode}, equipment_image = #{equipmentImage}, " +
            "equipment_name = #{equipmentName}, equipment_type = #{equipmentType}, brand = #{brand}, " +
            "price = #{price}, rental_price = #{rentalPrice}, rental_deposit = #{rentalDeposit}, total_quantity = #{totalQuantity}, " +
            "available_quantity = #{availableQuantity}, version = #{version}, status = #{status}, description = #{description}, venue_id = #{venueId}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(Equipment equipment);

    /**
     * 逻辑删除器材
     * @param id 器材ID
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新器材状态
     * @param id 器材ID
     * @param status 新状态
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查器材编号是否已存在（含逻辑删除记录，因数据库唯一键不区分 del_flag）
     * @param equipmentCode 器材编号
     * @param excludeId 排除的器材ID（用于更新时排除自身）
     * @return 存在返回true，否则返回false
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM sys_equipment " +
            "WHERE equipment_code = #{equipmentCode} " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByEquipmentCode(@Param("equipmentCode") String equipmentCode,
                                  @Param("excludeId") Long excludeId);

    /**
     * 更新可用数量（乐观锁）
     * @param id 器材ID
     * @param availableQuantity 可用数量
     * @param version 当前版本号
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment SET available_quantity = #{availableQuantity}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND version = #{version} AND del_flag = 0")
    int updateAvailableQuantity(@Param("id") Long id, @Param("availableQuantity") Integer availableQuantity, @Param("version") Integer version);

    /**
     * 统计各状态的器材数量
     * @return 各状态数量
     */
    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM sys_equipment " +
            "WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 统计所有器材数量
     * @return 器材总数
     */
    @Select("SELECT COUNT(*) FROM sys_equipment WHERE del_flag = 0")
    int countAll();

    /**
     * 获取器材类型列表（去重）
     * @return 类型列表
     */
    @Select("SELECT DISTINCT equipment_type FROM sys_equipment WHERE del_flag = 0 AND equipment_type IS NOT NULL AND equipment_type != '' ORDER BY equipment_type")
    List<String> findAllTypes();

    /**
     * 库存预警：可用数量 <= 阈值的器材（用于 Dashboard 库存预警）
     * @param threshold 安全库存阈值，默认 10
     * @param venueId 场馆过滤（可选）
     */
    @Select("<script>" +
            "SELECT id, equipment_name, equipment_type, available_quantity, total_quantity FROM sys_equipment " +
            "WHERE del_flag = 0 AND status = 1 AND COALESCE(available_quantity,0) &lt;= #{threshold} " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "ORDER BY available_quantity ASC" +
            "</script>")
    List<Equipment> findLowStock(@Param("threshold") int threshold, @Param("venueId") Long venueId);
}
