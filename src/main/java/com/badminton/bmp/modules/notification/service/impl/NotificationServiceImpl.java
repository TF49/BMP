package com.badminton.bmp.modules.notification.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.notification.entity.Notification;
import com.badminton.bmp.modules.notification.mapper.NotificationMapper;
import com.badminton.bmp.modules.notification.service.NotificationService;
import com.badminton.bmp.modules.system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知服务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> findByPage(int page, int size) {
        int offset = (page - 1) * size;
        return notificationMapper.findByPage(offset, size);
    }

    @Override
    public int countAll() {
        return notificationMapper.countAll();
    }

    @Override
    public Notification findById(Long id) {
        return notificationMapper.findById(id);
    }

    @Override
    public int create(Notification notification) {
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new AccessDeniedException("仅协会会长和场馆管理员可发布通知");
        }

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("用户未登录");
        }

        notification.setPublisherId(currentUser.getId());
        notification.setPublisherName(currentUser.getUsername());
        if (SecurityUtils.isVenueManager()) {
            notification.setVenueId(SecurityUtils.getCurrentUserVenueId());
        } else {
            notification.setVenueId(null);
        }
        notification.setCreateTime(LocalDateTime.now());

        return notificationMapper.insert(notification);
    }

    @Override
    public int delete(Long id) {
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new AccessDeniedException("仅协会会长和场馆管理员可删除通知");
        }

        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            throw new IllegalArgumentException("通知不存在");
        }

        // 场馆管理员只能删除自己场馆的通知
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (notification.getVenueId() == null || !notification.getVenueId().equals(currentVenueId)) {
                throw new AccessDeniedException("无权删除其他场馆的通知");
            }
        }

        return notificationMapper.deleteById(id);
    }

    @Override
    public int update(Notification notification) {
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new AccessDeniedException("仅协会会长和场馆管理员可编辑通知");
        }

        if (notification.getId() == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }

        Notification existingNotification = notificationMapper.findById(notification.getId());
        if (existingNotification == null) {
            throw new IllegalArgumentException("通知不存在");
        }

        // 场馆管理员只能编辑自己场馆的通知
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (existingNotification.getVenueId() == null || !existingNotification.getVenueId().equals(currentVenueId)) {
                throw new AccessDeniedException("无权编辑其他场馆的通知");
            }
        }

        return notificationMapper.update(notification);
    }
}
