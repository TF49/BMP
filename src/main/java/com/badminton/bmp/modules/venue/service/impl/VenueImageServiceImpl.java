package com.badminton.bmp.modules.venue.service.impl;

import com.badminton.bmp.modules.venue.entity.VenueImage;
import com.badminton.bmp.modules.venue.mapper.VenueImageMapper;
import com.badminton.bmp.modules.venue.service.VenueImageService;
import com.badminton.bmp.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 场馆图片业务实现类
 */
@Service
public class VenueImageServiceImpl implements VenueImageService {

    @Autowired
    private VenueImageMapper venueImageMapper;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public List<VenueImage> findByVenueId(Long venueId) {
        return venueImageMapper.findByVenueId(venueId);
    }

    @Override
    public List<VenueImage> findByVenueIdAndType(Long venueId, String imageType) {
        return venueImageMapper.findByVenueIdAndType(venueId, imageType);
    }

    @Override
    public int add(VenueImage image) {
        return venueImageMapper.insert(image);
    }

    @Override
    public int update(VenueImage image) {
        return venueImageMapper.update(image);
    }

    @Override
    public boolean deleteById(Long id) {
        VenueImage image = venueImageMapper.findById(id);
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
        return venueImageMapper.deleteById(id) > 0;
    }

    @Override
    public int deleteByVenueId(Long venueId) {
        List<VenueImage> images = venueImageMapper.findByVenueId(venueId);
        
        // 删除所有物理文件
        for (VenueImage image : images) {
            String imageUrl = image.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String filePath = convertUrlToFilePath(imageUrl);
                ImageUtils.deleteImageFile(filePath);
            }
        }

        // 删除数据库记录
        return venueImageMapper.deleteByVenueId(venueId);
    }

    @Override
    public int updateSortOrder(Long id, Integer sortOrder) {
        return venueImageMapper.updateSortOrder(id, sortOrder);
    }

    @Override
    public VenueImage findById(Long id) {
        return venueImageMapper.findById(id);
    }

    @Override
    public int batchAdd(List<VenueImage> images) {
        int count = 0;
        for (VenueImage image : images) {
            if (add(image) > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将URL路径转换为文件系统路径
     * @param url URL路径（如：/api/uploads/venues/xxx.jpg）
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
