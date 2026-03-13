package com.badminton.bmp.modules.venue.mapper;

import com.badminton.bmp.modules.venue.entity.Venue;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 场馆Mapper接口
 * 用于数据库操作
 * 参照UserMapper实现方式，使用MyBatis注解
 */
@Mapper
public interface VenueMapper {

    /**
     * 根据ID查找场馆
     * @param id 场馆ID
     * @return 场馆对象
     */
    @Select("SELECT * FROM sys_venue WHERE id = #{id}")
    Venue findById(@Param("id") Long id);

    /**
     * 查找所有场馆
     * @return 场馆列表
     */
    @Select("SELECT * FROM sys_venue ORDER BY id")
    List<Venue> findAll();

    /**
     * 根据场馆名称或地址搜索场馆，支持分页
     * @param venueName 场馆名称
     * @param address 地址
     * @param status 状态（0-关闭，1-营业中，2-暂停）
     * @param venueId 场馆ID（可选，非空时仅返回该场馆，用于场馆管理者数据范围过滤）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 场馆列表
     */
    @Select("SELECT * FROM sys_venue WHERE 1=1 " +
            "AND (#{venueName} IS NULL OR #{venueName} = '' OR venue_name LIKE CONCAT('%', #{venueName}, '%')) " +
            "AND (#{address} IS NULL OR #{address} = '' OR address LIKE CONCAT('%', #{address}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "AND (#{venueId} IS NULL OR id = #{venueId}) " +
            "ORDER BY id LIMIT #{offset}, #{limit}")
    List<Venue> findByVenueNameOrAddress(@Param("venueName") String venueName,
                                        @Param("address") String address,
                                        @Param("status") Integer status,
                                        @Param("venueId") Long venueId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * 根据场馆名称或地址统计场馆数量
     * @param venueName 场馆名称
     * @param address 地址
     * @param status 状态（0-关闭，1-营业中，2-暂停）
     * @param venueId 场馆ID（可选，非空时仅统计该场馆，用于场馆管理者数据范围过滤）
     * @return 场馆数量
     */
    @Select("SELECT COUNT(*) FROM sys_venue WHERE 1=1 " +
            "AND (#{venueName} IS NULL OR #{venueName} = '' OR venue_name LIKE CONCAT('%', #{venueName}, '%')) " +
            "AND (#{address} IS NULL OR #{address} = '' OR address LIKE CONCAT('%', #{address}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "AND (#{venueId} IS NULL OR id = #{venueId})")
    int countByVenueNameOrAddress(@Param("venueName") String venueName,
                                  @Param("address") String address,
                                  @Param("status") Integer status,
                                  @Param("venueId") Long venueId);

    /**
     * 插入新场馆
     * @param venue 场馆对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_venue (venue_name, address, contact_phone, contact_person, " +
            "business_hours, description, venue_image, status, create_time, update_time) " +
            "VALUES (#{venueName}, #{address}, #{contactPhone}, #{contactPerson}, " +
            "#{businessHours}, #{description}, #{venueImage}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Venue venue);

    /**
     * 更新场馆信息
     * @param venue 场馆对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_venue SET venue_name = #{venueName}, address = #{address}, " +
            "contact_phone = #{contactPhone}, contact_person = #{contactPerson}, " +
            "business_hours = #{businessHours}, description = #{description}, venue_image = #{venueImage}, " +
            "status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Venue venue);

    /**
     * 删除场馆
     * @param id 场馆ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 统计该场馆下的场地数量
     * @param venueId 场馆ID
     * @return 场地数量
     */
    @Select("SELECT COUNT(*) FROM sys_court WHERE venue_id = #{venueId} AND del_flag = 0")
    int countCourtsByVenueId(@Param("venueId") Long venueId);

    /**
     * 统计该场馆下的赛事数量
     * @param venueId 场馆ID
     * @return 赛事数量
     */
    @Select("SELECT COUNT(*) FROM biz_tournament WHERE venue_id = #{venueId} AND del_flag = 0")
    int countTournamentsByVenueId(@Param("venueId") Long venueId);

    /**
     * 批量更新场馆下所有场地的状态
     * @param venueId 场馆ID
     * @param status 新状态（0-维护中，1-空闲，2-预约中，3-使用中）
     * @return 影响的行数
     */
    @Update("UPDATE sys_court SET status = #{status}, update_time = NOW() WHERE venue_id = #{venueId} AND del_flag = 0")
    int updateCourtsStatusByVenueId(@Param("venueId") Long venueId, @Param("status") Integer status);
}
