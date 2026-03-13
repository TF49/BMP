package com.badminton.bmp.modules.equipment.service;

import com.badminton.bmp.modules.equipment.entity.EquipmentImage;

import java.util.List;

/**
 * 器材图片业务接口
 */
public interface EquipmentImageService {

    /**
     * 根据器材ID查找所有图片
     * @param equipmentId 器材ID
     * @return 图片列表
     */
    List<EquipmentImage> findByEquipmentId(Long equipmentId);

    /**
     * 根据器材ID和图片类型查找图片
     * @param equipmentId 器材ID
     * @param imageType 图片类型（MAIN/DETAIL）
     * @return 图片列表
     */
    List<EquipmentImage> findByEquipmentIdAndType(Long equipmentId, String imageType);

    /**
     * 添加图片
     * @param image 图片对象
     * @return 影响的行数
     */
    int add(EquipmentImage image);

    /**
     * 更新图片信息
     * @param image 图片对象
     * @return 影响的行数
     */
    int update(EquipmentImage image);

    /**
     * 删除图片（包括物理文件）
     * @param id 图片ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据器材ID删除所有图片（包括物理文件）
     * @param equipmentId 器材ID
     * @return 删除的图片数量
     */
    int deleteByEquipmentId(Long equipmentId);

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
    int batchAdd(List<EquipmentImage> images);
}
