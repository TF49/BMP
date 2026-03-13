package com.badminton.bmp.modules.equipment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 器材图片实体类
 * 对应数据库表 sys_equipment_image
 */
@Data
public class EquipmentImage {
    /**
     * 图片ID（主键，自增）
     */
    private Long id;

    /**
     * 器材ID（外键关联sys_equipment）
     */
    private Long equipmentId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片类型（MAIN-主图，DETAIL-详情图）
     */
    private String imageType;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
