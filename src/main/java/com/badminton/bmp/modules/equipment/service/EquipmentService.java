package com.badminton.bmp.modules.equipment.service;

import com.badminton.bmp.modules.equipment.entity.Equipment;

import java.util.List;
import java.util.Map;

/**
 * 器材业务接口
 */
public interface EquipmentService {

    /**
     * 根据ID查找器材
     * @param id 器材ID
     * @return 器材对象
     */
    Equipment findById(Long id);

    /**
     * 分页查询器材列表
     * @param venueId 场馆ID（可选）
     * @param equipmentType 器材类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 器材列表
     */
    List<Equipment> findAll(Long venueId, String equipmentType, Integer status, String keyword, int page, int size);

    /**
     * 统计器材数量
     * @param venueId 场馆ID（可选）
     * @param equipmentType 器材类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 器材数量
     */
    int count(Long venueId, String equipmentType, Integer status, String keyword);

    /**
     * 添加器材
     * @param equipment 器材对象
     * @return 影响的行数
     */
    int add(Equipment equipment);

    /**
     * 更新器材信息
     * @param equipment 器材对象
     * @return 影响的行数
     */
    int update(Equipment equipment);

    /**
     * 删除器材（逻辑删除）
     * @param id 器材ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 更新器材状态
     * @param id 器材ID
     * @param status 新状态（0-停用，1-正常）
     * @return 影响的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取器材统计信息
     * @return 统计信息Map，包含总器材数、各状态数量
     */
    Map<String, Object> getStatistics();

    /**
     * 更新可用数量
     * @param id 器材ID
     * @param availableQuantity 可用数量
     * @return 影响的行数
     */
    int updateAvailableQuantity(Long id, Integer availableQuantity);

    /**
     * 获取器材类型列表（去重）
     * @return 类型列表
     */
    List<String> getEquipmentTypes();

    /**
     * 库存预警列表：可用数量 <= 阈值的器材（Dashboard 库存预警卡片）
     * @param threshold 安全库存阈值，默认 10
     * @return 器材列表，每项含 equipmentId, equipmentName, category, stock, threshold
     */
    List<Map<String, Object>> getLowStock(Integer threshold);
}
