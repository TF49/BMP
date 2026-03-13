package com.badminton.bmp.modules.equipment.mapper;

import com.badminton.bmp.modules.equipment.entity.EquipmentImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 器材图片Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface EquipmentImageMapper {

    /**
     * 根据ID查找图片
     * @param id 图片ID
     * @return 图片对象
     */
    @Select("SELECT * FROM sys_equipment_image WHERE id = #{id}")
    EquipmentImage findById(@Param("id") Long id);

    /**
     * 根据器材ID查找所有图片
     * @param equipmentId 器材ID
     * @return 图片列表
     */
    @Select("SELECT * FROM sys_equipment_image WHERE equipment_id = #{equipmentId} ORDER BY sort_order ASC, id ASC")
    List<EquipmentImage> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * 根据器材ID和图片类型查找图片
     * @param equipmentId 器材ID
     * @param imageType 图片类型（MAIN/DETAIL）
     * @return 图片列表
     */
    @Select("SELECT * FROM sys_equipment_image WHERE equipment_id = #{equipmentId} AND image_type = #{imageType} ORDER BY sort_order ASC, id ASC")
    List<EquipmentImage> findByEquipmentIdAndType(@Param("equipmentId") Long equipmentId, @Param("imageType") String imageType);

    /**
     * 插入新图片
     * @param image 图片对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_equipment_image (equipment_id, image_url, image_type, sort_order, create_time) " +
            "VALUES (#{equipmentId}, #{imageUrl}, #{imageType}, #{sortOrder}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EquipmentImage image);

    /**
     * 更新图片信息
     * @param image 图片对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment_image SET image_url = #{imageUrl}, image_type = #{imageType}, " +
            "sort_order = #{sortOrder} WHERE id = #{id}")
    int update(EquipmentImage image);

    /**
     * 删除图片
     * @param id 图片ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_equipment_image WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据器材ID删除所有图片
     * @param equipmentId 器材ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_equipment_image WHERE equipment_id = #{equipmentId}")
    int deleteByEquipmentId(@Param("equipmentId") Long equipmentId);

    /**
     * 更新图片排序
     * @param id 图片ID
     * @param sortOrder 排序顺序
     * @return 影响的行数
     */
    @Update("UPDATE sys_equipment_image SET sort_order = #{sortOrder} WHERE id = #{id}")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
}
