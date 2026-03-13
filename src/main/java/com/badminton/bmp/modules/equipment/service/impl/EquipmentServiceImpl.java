package com.badminton.bmp.modules.equipment.service.impl;

import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.mapper.EquipmentMapper;
import com.badminton.bmp.modules.equipment.mapper.EquipmentRentalMapper;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;

/**
 * 器材业务实现类
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private EquipmentRentalMapper equipmentRentalMapper;

    @Override
    public Equipment findById(Long id) {
        Equipment equipment = equipmentMapper.findById(id);
        if (equipment == null) {
            return null;
        }

        // 数据范围过滤：根据角色检查访问权限
        if (SecurityUtils.isPresident()) {
            return equipment;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(equipment.getVenueId())) {
                return equipment;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：仅查看可用器材（status == 1）
            if (equipment.getStatus() != null && equipment.getStatus() == 1) {
                return equipment;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public List<Equipment> findAll(Long venueId, String equipmentType, Integer status, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        // 数据范围过滤：根据当前用户角色调整查询参数
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 协会会长：使用传入的 venueId（可能为 null）
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能查询自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：不按场馆过滤，但只查询可用器材
            venueFilter = null;
        }
        return equipmentMapper.findAll(venueFilter, equipmentType, status, keyword, offset, size);
    }

    @Override
    public int count(Long venueId, String equipmentType, Integer status, String keyword) {
        // 数据范围过滤：根据当前用户角色调整查询参数
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 协会会长：使用传入的 venueId（可能为 null）
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能查询自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：不按场馆过滤，但只查询可用器材
            venueFilter = null;
        }
        return equipmentMapper.count(venueFilter, equipmentType, status, keyword);
    }

    @Override
    @Transactional
    public int add(Equipment equipment) {
        // 验证场馆ID必填（会长必须指定场馆，场馆管理者自动使用自己的场馆）
        if (SecurityUtils.isPresident()) {
            if (equipment.getVenueId() == null) {
                throw new BusinessException("请选择所属场馆");
            }
            // 验证场馆是否存在
            Venue venue = venueMapper.findById(equipment.getVenueId());
            if (venue == null) {
                throw new ResourceNotFoundException("所选场馆不存在");
            }
        } else if (SecurityUtils.isVenueManager()) {
            // 数据范围：场馆管理者只能在自己场馆下新增
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法新增器材");
            }
            equipment.setVenueId(vId);
        } else {
            throw new BusinessException("权限不足，只有管理员可以添加器材");
        }

        // 验证器材编号是否已存在
        if (equipmentMapper.existsByEquipmentCode(equipment.getEquipmentCode(), null)) {
            throw new BusinessException("器材编号已存在");
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        equipment.setCreateTime(now);
        equipment.setUpdateTime(now);

        // 设置默认状态为正常（1）
        if (equipment.getStatus() == null) {
            equipment.setStatus(1);
        }

        // 设置默认总数量和可用数量
        if (equipment.getTotalQuantity() == null) {
            equipment.setTotalQuantity(0);
        }
        if (equipment.getAvailableQuantity() == null) {
            equipment.setAvailableQuantity(equipment.getTotalQuantity());
        }

        // 确保可用数量不超过总数量
        if (equipment.getAvailableQuantity() > equipment.getTotalQuantity()) {
            equipment.setAvailableQuantity(equipment.getTotalQuantity());
        }

        return equipmentMapper.insert(equipment);
    }

    @Override
    @Transactional
    public int update(Equipment equipment) {
        // 查询原有器材信息
        Equipment existingEquipment = equipmentMapper.findById(equipment.getId());
        if (existingEquipment == null) {
            throw new ResourceNotFoundException("器材不存在");
        }

        // 数据范围：场馆管理者只能更新自己场馆的器材
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新器材");
            }
            if (existingEquipment.getVenueId() == null || !vId.equals(existingEquipment.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的器材");
            }
            // 强制固定为自己的场馆，避免前端传入其他venueId越权
            equipment.setVenueId(vId);
        } else if (SecurityUtils.isPresident()) {
            // 会长允许更新venueId（如果未传则保留原值）
            if (equipment.getVenueId() == null) {
                equipment.setVenueId(existingEquipment.getVenueId());
            } else if (!equipment.getVenueId().equals(existingEquipment.getVenueId())) {
                // 如果修改了场馆，验证新场馆是否存在
                Venue venue = venueMapper.findById(equipment.getVenueId());
                if (venue == null) {
                    throw new ResourceNotFoundException("所选场馆不存在");
                }
            }
        } else {
            throw new BusinessException("权限不足");
        }

        // 验证器材编号是否已存在（排除自身）
        String equipmentCode = equipment.getEquipmentCode() != null ? 
                equipment.getEquipmentCode() : existingEquipment.getEquipmentCode();
        if (equipmentMapper.existsByEquipmentCode(equipmentCode, equipment.getId())) {
            throw new BusinessException("器材编号已存在");
        }

        // 保留原有值（如果未传入）
        if (equipment.getEquipmentCode() == null) {
            equipment.setEquipmentCode(existingEquipment.getEquipmentCode());
        }
        if (equipment.getEquipmentName() == null) {
            equipment.setEquipmentName(existingEquipment.getEquipmentName());
        }
        if (equipment.getEquipmentType() == null) {
            equipment.setEquipmentType(existingEquipment.getEquipmentType());
        }
        if (equipment.getBrand() == null) {
            equipment.setBrand(existingEquipment.getBrand());
        }
        if (equipment.getPrice() == null) {
            equipment.setPrice(existingEquipment.getPrice());
        }
        if (equipment.getRentalPrice() == null) {
            equipment.setRentalPrice(existingEquipment.getRentalPrice());
        }
        if (equipment.getRentalDeposit() == null) {
            equipment.setRentalDeposit(existingEquipment.getRentalDeposit());
        }
        if (equipment.getTotalQuantity() == null) {
            equipment.setTotalQuantity(existingEquipment.getTotalQuantity());
        }
        if (equipment.getAvailableQuantity() == null) {
            equipment.setAvailableQuantity(existingEquipment.getAvailableQuantity());
        }
        if (equipment.getStatus() == null) {
            equipment.setStatus(existingEquipment.getStatus());
        }
        if (equipment.getDescription() == null) {
            equipment.setDescription(existingEquipment.getDescription());
        }
        if (equipment.getVenueId() == null) {
            equipment.setVenueId(existingEquipment.getVenueId());
        }

        // 确保可用数量不超过总数量
        if (equipment.getAvailableQuantity() > equipment.getTotalQuantity()) {
            equipment.setAvailableQuantity(equipment.getTotalQuantity());
        }

        // 设置更新时间
        equipment.setUpdateTime(LocalDateTime.now());

        return equipmentMapper.update(equipment);
    }

    @Override
    public int deleteById(Long id) {
        // 验证器材是否存在
        Equipment equipment = equipmentMapper.findById(id);
        if (equipment == null) {
            throw new ResourceNotFoundException("器材不存在");
        }

        // 数据范围：场馆管理者只能删除自己场馆的器材
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法删除器材");
            }
            if (equipment.getVenueId() == null || !vId.equals(equipment.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的器材");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足");
        }

        // 检查是否有未归还的租借记录
        int rentingCount = equipmentRentalMapper.countByEquipmentIdAndStatus(id, 1);
        if (rentingCount > 0) {
            throw new BusinessException(
                String.format("该器材存在%d条未归还的租借记录，无法删除。请先处理这些租借记录后再试。", rentingCount)
            );
        }

        return equipmentMapper.deleteById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // 验证器材是否存在
        Equipment equipment = equipmentMapper.findById(id);
        if (equipment == null) {
            throw new ResourceNotFoundException("器材不存在");
        }

        // 数据范围：场馆管理者只能更新自己场馆的器材状态
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新器材状态");
            }
            if (equipment.getVenueId() == null || !vId.equals(equipment.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的器材");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足");
        }

        // 验证状态值是否有效（0-停用，1-正常，2-维护中）
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BusinessException("请选择有效的状态：正常、停用或维护中");
        }

        return equipmentMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取总器材数
        int total = equipmentMapper.countAll();
        statistics.put("total", total);

        // 获取各状态的器材数量
        List<Map<String, Object>> statusCounts = equipmentMapper.countByStatus();

        // 初始化各状态数量
        int disabled = 0;     // 停用（0）
        int normal = 0;       // 正常（1）
        int maintenance = 0;  // 维护中（2）

        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");

            if (statusObj != null && countObj != null) {
                int statusValue = ((Number) statusObj).intValue();
                int countValue = ((Number) countObj).intValue();

                switch (statusValue) {
                    case 0:
                        disabled = countValue;
                        break;
                    case 1:
                        normal = countValue;
                        break;
                    case 2:
                        maintenance = countValue;
                        break;
                }
            }
        }

        statistics.put("disabled", disabled);
        statistics.put("normal", normal);
        statistics.put("maintenance", maintenance);

        return statistics;
    }

    @Override
    @Transactional
    public int updateAvailableQuantity(Long id, Integer availableQuantity) {
        // 验证器材是否存在
        Equipment equipment = equipmentMapper.findById(id);
        if (equipment == null) {
            throw new ResourceNotFoundException("器材不存在");
        }

        // 确保可用数量不超过总数量
        if (availableQuantity > equipment.getTotalQuantity()) {
            throw new BusinessException("可用数量不能超过总数量");
        }

        // 确保可用数量不为负数
        if (availableQuantity < 0) {
            throw new BusinessException("可用数量不能为负数");
        }

        // 使用乐观锁更新
        Integer version = equipment.getVersion() != null ? equipment.getVersion() : 0;
        int result = equipmentMapper.updateAvailableQuantity(id, availableQuantity, version);
        if (result == 0) {
            throw new BusinessException("库存更新失败，数据已被其他操作修改，请重试");
        }
        return result;
    }

    @Override
    public List<String> getEquipmentTypes() {
        return equipmentMapper.findAllTypes();
    }

    @Override
    public List<Map<String, Object>> getLowStock(Integer threshold) {
        int th = threshold != null && threshold >= 0 ? threshold : 10;
        Long venueId = null;
        if (SecurityUtils.isVenueManager()) {
            venueId = SecurityUtils.getCurrentUserVenueId();
        }
        List<Equipment> list = equipmentMapper.findLowStock(th, venueId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Equipment e : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("equipmentId", e.getId());
            item.put("id", e.getId());
            item.put("equipmentName", e.getEquipmentName());
            item.put("name", e.getEquipmentName());
            item.put("category", e.getEquipmentType() != null ? e.getEquipmentType() : "其他");
            item.put("type", e.getEquipmentType());
            item.put("stock", e.getAvailableQuantity() != null ? e.getAvailableQuantity() : 0);
            item.put("quantity", e.getAvailableQuantity());
            item.put("threshold", th);
            item.put("safetyStock", th);
            result.add(item);
        }
        return result;
    }
}
