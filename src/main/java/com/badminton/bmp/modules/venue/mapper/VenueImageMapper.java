package com.badminton.bmp.modules.venue.mapper;

import com.badminton.bmp.modules.venue.entity.VenueImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 场馆图片Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface VenueImageMapper {

    /**
     * 根据ID查找图片
     * @param id 图片ID
     * @return 图片对象
     */
    @Select("SELECT * FROM sys_venue_image WHERE id = #{id}")
    VenueImage findById(@Param("id") Long id);

    /**
     * 根据场馆ID查找所有图片
     * @param venueId 场馆ID
     * @return 图片列表
     */
    @Select("SELECT * FROM sys_venue_image WHERE venue_id = #{venueId} ORDER BY sort_order ASC, id ASC")
    List<VenueImage> findByVenueId(@Param("venueId") Long venueId);

    /**
     * 根据场馆ID和图片类型查找图片
     * @param venueId 场馆ID
     * @param imageType 图片类型（MAIN/DETAIL）
     * @return 图片列表
     */
    @Select("SELECT * FROM sys_venue_image WHERE venue_id = #{venueId} AND image_type = #{imageType} ORDER BY sort_order ASC, id ASC")
    List<VenueImage> findByVenueIdAndType(@Param("venueId") Long venueId, @Param("imageType") String imageType);

    /**
     * 插入新图片
     * @param image 图片对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_venue_image (venue_id, image_url, image_type, sort_order, create_time) " +
            "VALUES (#{venueId}, #{imageUrl}, #{imageType}, #{sortOrder}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VenueImage image);

    /**
     * 更新图片信息
     * @param image 图片对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_venue_image SET image_url = #{imageUrl}, image_type = #{imageType}, " +
            "sort_order = #{sortOrder} WHERE id = #{id}")
    int update(VenueImage image);

    /**
     * 删除图片
     * @param id 图片ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue_image WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据场馆ID删除所有图片
     * @param venueId 场馆ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue_image WHERE venue_id = #{venueId}")
    int deleteByVenueId(@Param("venueId") Long venueId);

    /**
     * 更新图片排序
     * @param id 图片ID
     * @param sortOrder 排序顺序
     * @return 影响的行数
     */
    @Update("UPDATE sys_venue_image SET sort_order = #{sortOrder} WHERE id = #{id}")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
}
