package com.badminton.bmp.modules.notification.service;

import com.badminton.bmp.modules.notification.entity.Notification;

import java.util.List;

/**
 * 系统通知服务接口
 */
public interface NotificationService {

    /**
     * 分页查询通知列表
     */
    List<Notification> findByPage(int page, int size);

    /**
     * 统计通知总数
     */
    int countAll();

    /**
     * 根据ID查询通知详情
     */
    Notification findById(Long id);

    /**
     * 发布通知（仅协会会长、场馆管理员可调用）
     */
    int create(Notification notification);

    /**
     * 删除通知（仅协会会长、场馆管理员可调用）
     */
    int delete(Long id);

    /**
     * 编辑通知（仅协会会长、场馆管理员可调用）
     */
    int update(Notification notification);
}
