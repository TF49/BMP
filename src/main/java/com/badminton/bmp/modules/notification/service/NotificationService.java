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
     * 分页查询当前用户可见的通知列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param userVenueId 当前用户的场馆ID（如果为会长则传null）
     * @return 通知列表
     */
    List<Notification> findByPage(int page, int size, Long userVenueId);

    /**
     * 统计当前用户可见的通知总数
     * @param userVenueId 当前用户的场馆ID（如果为会长则传null）
     * @return 通知总数
     */
    int countAll(Long userVenueId);

    /**
     * 根据ID查询通知详情
     */
    Notification findById(Long id);

    /**
     * 发布通知（仅协会会长、场馆管理员可调用）
     */
    int create(Notification notification);

    /**
     * 记录系统通知（用于定时任务等无登录上下文场景）
     */
    void publishSystemNotification(String title, String content, Long venueId);

    /**
     * 删除通知（仅协会会长、场馆管理员可调用）
     */
    int delete(Long id);

    /**
     * 编辑通知（仅协会会长、场馆管理员可调用）
     */
    int update(Notification notification);
}
