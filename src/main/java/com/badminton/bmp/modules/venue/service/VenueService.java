package com.badminton.bmp.modules.venue.service;

import com.badminton.bmp.modules.venue.entity.Venue;

import java.util.List;

/**
 * 场馆业务接口
 * 参照UserService实现方式
 */
public interface VenueService {

    /**
     * 根据ID查找场馆
     * @param id 场馆ID
     * @return 场馆对象
     */
    Venue findById(Long id);

    /**
     * 查找所有场馆
     * @return 场馆列表
     */
    List<Venue> findAll();

    /**
     * 根据场馆名称或地址搜索场馆，支持分页
     * @param venueName 场馆名称
     * @param address 地址
     * @param status 状态（0-关闭，1-营业中，2-暂停）
     * @param page 页码
     * @param size 每页数量
     * @return 场馆列表
     */
    List<Venue> findByVenueNameOrAddress(String venueName, String address, Integer status, int page, int size);

    /**
     * 根据场馆名称或地址统计场馆数量
     * @param venueName 场馆名称
     * @param address 地址
     * @param status 状态（0-关闭，1-营业中，2-暂停）
     * @return 场馆数量
     */
    int countByVenueNameOrAddress(String venueName, String address, Integer status);

    /**
     * 添加场馆
     * @param venue 场馆对象
     * @return 影响的行数
     */
    int add(Venue venue);

    /**
     * 更新场馆信息
     * @param venue 场馆对象
     * @return 影响的行数
     */
    int update(Venue venue);

    /**
     * 删除场馆
     * @param id 场馆ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 统计该场馆下的场地数量
     * @param venueId 场馆ID
     * @return 场地数量
     */
    int countCourtsByVenueId(Long venueId);

    /**
     * 统计该场馆下的赛事数量
     * @param venueId 场馆ID
     * @return 赛事数量
     */
    int countTournamentsByVenueId(Long venueId);

    /**
     * 批量更新场馆下所有场地的状态
     * @param venueId 场馆ID
     * @param status 新状态（0-维护中，1-空闲，2-预约中，3-使用中）
     * @return 影响的行数
     */
    int updateCourtsStatusByVenueId(Long venueId, Integer status);

    /**
     * 获取场馆统计信息
     * @return 统计信息Map，包含总场馆数、营业中数量、暂停数量、关闭数量
     */
    java.util.Map<String, Object> getStatistics();
}
