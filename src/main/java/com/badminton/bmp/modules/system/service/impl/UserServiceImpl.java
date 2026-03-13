package com.badminton.bmp.modules.system.service.impl;

import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.mapper.UserMapper;
import com.badminton.bmp.modules.system.service.UserService;
import com.badminton.bmp.modules.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户业务实现类
 * 参照CMS实现方式
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberService memberService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            return null;
        }

        // 数据范围过滤：根据角色检查访问权限
        if (com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            return user;
        } else if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            Long vId = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(user.getVenueId())) {
                return user;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：只能读取自己的用户信息
            com.badminton.bmp.modules.system.entity.User current = com.badminton.bmp.common.util.SecurityUtils.getCurrentUser();
            if (current != null && current.getId() != null && current.getId().equals(id)) {
                return user;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }
    
    @Override
    public int updateLastLoginTime(Long id) {
        return userMapper.updateLastLoginTime(id, LocalDateTime.now());
    }
    
    /**
     * 验证密码 - 仅使用BCrypt加密验证
     *
     * 安全说明：
     * - 已移除明文密码兼容逻辑，强制使用BCrypt验证
     * - 如有历史明文密码数据，请运行密码迁移脚本进行升级
     */
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        // 仅使用BCrypt验证，不再支持明文密码
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            // BCrypt验证异常，返回false
            return false;
        }
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findByRole(String role) {
        return userMapper.findByRole(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int register(User user) {
        // 设置创建时间
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        // 设置更新时间
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(LocalDateTime.now());
        }
        
        // 设置默认角色为USER
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        // 设置默认状态为启用（1）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 使用BCrypt加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }
        
        // ID 使用数据库自增生成（AUTO_INCREMENT）
        int inserted = userMapper.insert(user);

        // 仅对「用户」角色创建默认会员记录：会员管理只面向普通用户(USER)与会员(MEMBER)，教练/会长/场馆管理者/管理员不进入会员模块
        String role = user.getRole() != null ? user.getRole().trim() : "";
        if ("USER".equalsIgnoreCase(role) || "MEMBER".equalsIgnoreCase(role)) {
            try {
                memberService.createDefaultMemberForUser(user);
            } catch (Exception ignore) {
                // 防御性处理，避免因会员创建失败影响用户注册
            }
        }

        return inserted;
    }

    @Override
    public List<User> findByUsernameOrIdCard(String username, String idCard, String role, Integer status, int page, int size) {
        // 如果搜索条件都为空，直接返回所有用户
        if ((username == null || username.trim().isEmpty()) && 
            (idCard == null || idCard.trim().isEmpty()) && 
            (role == null || role.trim().isEmpty()) &&
            status == null) {
            List<User> allUsers = userMapper.findAll();
            // 手动分页
            int offset = (page - 1) * size;
            int endIndex = Math.min(offset + size, allUsers.size());
            if (offset >= allUsers.size()) {
                return new java.util.ArrayList<>();
            }
            return allUsers.subList(offset, endIndex);
        }
        // 否则按照搜索条件过滤
        int offset = (page - 1) * size;
        return userMapper.findByUsernameOrIdCard(username, idCard, role, status, offset, size);
    }

    @Override
    public int countByUsernameOrIdCard(String username, String idCard, String role, Integer status) {
        // 如果搜索条件都为空，直接返回所有用户数量
        if ((username == null || username.trim().isEmpty()) && 
            (idCard == null || idCard.trim().isEmpty()) && 
            (role == null || role.trim().isEmpty()) &&
            status == null) {
            return userMapper.findAll().size();
        }
        // 否则按照搜索条件统计
        return userMapper.countByUsernameOrIdCard(username, idCard, role, status);
    }

    @Override
    public int update(User user) {
        // 先查询原有用户信息
        User existingUser = userMapper.findById(user.getId());
        if (existingUser != null) {
            // 确保角色字段不为空，使用原有角色
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole(existingUser.getRole());
            }
            // 确保状态不为空，使用原有状态
            if (user.getStatus() == null) {
                user.setStatus(existingUser.getStatus());
            }
            // 处理密码：如果提供了新密码，使用BCrypt加密；否则保持原密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // 如果新密码与旧密码不同，使用BCrypt加密
                if (!user.getPassword().equals(existingUser.getPassword())) {
                    String encodedPassword = passwordEncoder.encode(user.getPassword());
                    user.setPassword(encodedPassword);
                } else {
                    // 如果密码相同，保持原密码（可能是已加密的）
                    user.setPassword(existingUser.getPassword());
                }
            } else {
                // 如果没有提供新密码，保持原密码
                user.setPassword(existingUser.getPassword());
            }
            // 设置更新时间
            user.setUpdateTime(LocalDateTime.now());
        } else {
            // 如果用户不存在，设置默认值
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
            }
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            user.setUpdateTime(LocalDateTime.now());
        }
        return userMapper.update(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    @Override
    public int updateProfile(Long id, String phone, String avatar, String gender, String signature) {
        return userMapper.updateProfile(id, phone, avatar, gender, signature, LocalDateTime.now());
    }

    @Override
    public int updatePassword(Long id, String newEncodedPassword) {
        return userMapper.updatePassword(id, newEncodedPassword, LocalDateTime.now());
    }
    
    @Override
    public boolean checkPresidentExists(Long excludeUserId) {
        User existingPresident = userMapper.findByRole("PRESIDENT");
        if (existingPresident != null) {
            // 如果是更新场景，且当前用户就是协会会长，允许修改
            if (excludeUserId != null && existingPresident.getId().equals(excludeUserId)) {
                return false; // 允许修改
            }
            return true; // 已存在协会会长
        }
        return false; // 不存在协会会长
    }
}
