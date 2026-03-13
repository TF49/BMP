package com.badminton.bmp.modules.equipment.service.impl;

import com.badminton.bmp.modules.equipment.entity.EquipmentImage;
import com.badminton.bmp.modules.equipment.mapper.EquipmentImageMapper;
import com.badminton.bmp.modules.equipment.service.EquipmentImageService;
import com.badminton.bmp.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 器材图片业务实现类
 */
@Service
public class EquipmentImageServiceImpl implements EquipmentImageService {

    @Autowired
    private EquipmentImageMapper equipmentImageMapper;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public List<EquipmentImage> findByEquipmentId(Long equipmentId) {
        return equipmentImageMapper.findByEquipmentId(equipmentId);
    }

    @Override
    public List<EquipmentImage> findByEquipmentIdAndType(Long equipmentId, String imageType) {
        return equipmentImageMapper.findByEquipmentIdAndType(equipmentId, imageType);
    }

    @Override
    public int add(EquipmentImage image) {
        return equipmentImageMapper.insert(image);
    }

    @Override
    public int update(EquipmentImage image) {
        return equipmentImageMapper.update(image);
    }

    @Override
    public boolean deleteById(Long id) {
        EquipmentImage image = equipmentImageMapper.findById(id);
        if (image == null) {
            return false;
        }

        // 删除物理文件
        String imageUrl = image.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 将URL路径转换为文件系统路径
            String filePath = convertUrlToFilePath(imageUrl);
            ImageUtils.deleteImageFile(filePath);
        }

        // 删除数据库记录
        return equipmentImageMapper.deleteById(id) > 0;
    }

    @Override
    public int deleteByEquipmentId(Long equipmentId) {
        List<EquipmentImage> images = equipmentImageMapper.findByEquipmentId(equipmentId);
        
        // 删除所有物理文件
        for (EquipmentImage image : images) {
            String imageUrl = image.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String filePath = convertUrlToFilePath(imageUrl);
                ImageUtils.deleteImageFile(filePath);
            }
        }

        // 删除数据库记录
        return equipmentImageMapper.deleteByEquipmentId(equipmentId);
    }

    @Override
    public int updateSortOrder(Long id, Integer sortOrder) {
        return equipmentImageMapper.updateSortOrder(id, sortOrder);
    }

    @Override
    public int batchAdd(List<EquipmentImage> images) {
        int count = 0;
        for (EquipmentImage image : images) {
            if (add(image) > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将URL路径转换为文件系统路径
     * @param url URL路径（如：/api/uploads/equipments/xxx.jpg）
     * @return 文件系统路径
     */
    private String convertUrlToFilePath(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 移除URL前缀，获取相对路径
        String relativePath = url;
        if (url.startsWith("/api/uploads/")) {
            relativePath = url.substring("/api/uploads/".length());
        } else if (url.startsWith("/uploads/")) {
            relativePath = url.substring("/uploads/".length());
        }
        
        // 拼接完整路径
        return uploadDir + File.separator + relativePath.replace("/", File.separator);
    }
}
