package com.badminton.bmp.modules.venue.service;

import com.badminton.bmp.modules.venue.entity.VenueImage;

import java.util.List;

/**
 * 场馆图片业务接口
 */
public interface VenueImageService {

    /**
     * 根据场馆ID查找所有图片
     * @param venueId 场馆ID
     * @return 图片列表
     */
    List<VenueImage> findByVenueId(Long venueId);

    /**
     * 根据场馆ID和图片类型查找图片
     * @param venueId 场馆ID
     * @param imageType 图片类型（MAIN/DETAIL）
     * @return 图片列表
     */
    List<VenueImage> findByVenueIdAndType(Long venueId, String imageType);

    /**
     * 添加图片
     * @param image 图片对象
     * @return 影响的行数
     */
    int add(VenueImage image);

    /**
     * 更新图片信息
     * @param image 图片对象
     * @return 影响的行数
     */
    int update(VenueImage image);

    /**
     * 删除图片（包括物理文件）
     * @param id 图片ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据场馆ID删除所有图片（包括物理文件）
     * @param venueId 场馆ID
     * @return 删除的图片数量
     */
    int deleteByVenueId(Long venueId);

    /**
     * 更新图片排序
     * @param id 图片ID
     * @param sortOrder 排序顺序
     * @return 影响的行数
     */
    int updateSortOrder(Long id, Integer sortOrder);

    /**
     * 批量添加图片
     * @param images 图片列表
     * @return 添加成功的数量
     */
    int batchAdd(List<VenueImage> images);
    
    /**
     * 根据图片ID查询图片信息
     * @param id 图片ID
     * @return 图片对象或 null
     */
    VenueImage findById(Long id);
}
