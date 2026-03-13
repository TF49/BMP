package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.util.LoginRateLimiter;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.entity.UserSetting;
import com.badminton.bmp.modules.system.mapper.UserSettingMapper;
import com.badminton.bmp.modules.system.service.AuthResult;
import com.badminton.bmp.modules.system.service.AuthService;
import com.badminton.bmp.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户认证控制器
 * 处理用户登录等认证相关请求
 * 包含登录限流保护，防止暴力破解攻击
 */
@Tag(name = "认证模块", description = "登录、注册、Token 刷新、当前用户信息与设置")
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Operation(summary = "用户登录", description = "带限流保护，成功返回 AccessToken 与 RefreshToken")
    @SecurityRequirements()
    @PostMapping("/login")
    public Result<Object> login(@RequestBody LoginBody loginBody, HttpServletRequest request) {
        try {
            // 获取客户端IP
            String clientIp = getClientIp(request);
            String username = loginBody.getUsername();

            // 限流检查
            LoginRateLimiter.RateLimitResult rateLimitResult = loginRateLimiter.checkRateLimit(clientIp, username);
            if (!rateLimitResult.isAllowed()) {
                return error(rateLimitResult.getMessage());
            }

            // 调用认证服务进行登录验证
            AuthResult result = authService.login(username, loginBody.getPassword());

            if (result.isSuccess()) {
                // 登录成功，清除失败记录
                loginRateLimiter.recordLoginSuccess(clientIp, username);

                return success(new LoginResult(
                        result.getToken(),
                        result.getRefreshToken(),
                        result.getExpiresIn(),
                        result.getMessage(),
                        result.getUser()
                ));
            } else {
                // 登录失败，记录失败次数
                loginRateLimiter.recordLoginFailure(username);
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("登录失败，请稍后重试");
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Operation(summary = "获取当前用户信息", description = "需携带 Authorization: Bearer {token}")
    @GetMapping("/current")
    public Result<Object> getCurrentUser(@Parameter(description = "Bearer Token", required = true) @RequestHeader("Authorization") String authHeader) {
        try {
            // 提取Token（去除"Bearer "前缀）
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            User user = authService.getCurrentUser(token);
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                return success(user);
            } else {
                return error("Token无效或已过期");
            }
        } catch (Exception e) {
            return error("获取用户信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新当前用户信息", description = "仅可编辑 phone、gender、avatar、signature")
    @PostMapping("/update")
    public Result<Object> updateCurrentUser(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> body) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");
            Long id = user.getId();
            String phone = (String) body.get("phone");
            String avatar = (String) body.get("avatar");
            String gender = (String) body.get("gender");
            String signature = (String) body.get("signature");
            int n = userService.updateProfile(id, phone, avatar, gender, signature);
            if (n > 0) {
                User updated = userService.findById(id);
                if (updated != null) updated.setPassword(null);
                return success(updated != null ? updated : "更新成功");
            }
            return error("更新失败");
        } catch (Exception e) {
            return error("更新失败：" + e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Object> changePassword(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> body) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            if (oldPassword == null || oldPassword.isEmpty()) return error("请输入原密码");
            if (newPassword == null || newPassword.isEmpty()) return error("请输入新密码");
            if (newPassword.length() < 6) return error("新密码长度至少6位");
            if (!userService.verifyPassword(oldPassword, user.getPassword())) return error("原密码错误");
            String encoded = passwordEncoder.encode(newPassword);
            int n = userService.updatePassword(user.getId(), encoded);
            if (n > 0) return success("密码修改成功");
            return error("修改失败");
        } catch (Exception e) {
            return error("修改失败：" + e.getMessage());
        }
    }

    @Operation(summary = "上传头像", description = "上传文件并返回 URL，前端再调用 /api/auth/update 写回 avatar")
    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Object> uploadAvatar(@RequestHeader("Authorization") String authHeader, @Parameter(required = true) @RequestParam("file") MultipartFile file) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");
            if (file == null || file.isEmpty()) return error("请选择要上传的图片文件");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) return error("文件类型不支持，请上传图片文件");
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot >= 0 && dot < originalFilename.length() - 1) ext = originalFilename.substring(dot + 1).toLowerCase();
            }
            Set<String> allowedExt = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
            if (ext.isEmpty() || !allowedExt.contains(ext)) return error("图片格式不支持，请上传 jpg/jpeg/png/webp 格式");
            if (file.getSize() > 2 * 1024 * 1024) return error("头像大小不能超过 2MB");
            Path avatarDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = avatarDir.resolve(filename).normalize();
            if (!target.startsWith(avatarDir)) return error("非法文件路径");
            file.transferTo(target.toFile());
            String url = "/api/uploads/avatars/" + filename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            return success(result);
        } catch (Exception e) {
            return error("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户设置", description = "安全/通知等 key-value 设置")
    @GetMapping("/settings")
    public Result<Object> getSettings(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");
            List<UserSetting> list = userSettingMapper.findByUserId(user.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("loginAlert", true);
            map.put("strangeDevice", true);
            map.put("siteMessage", true);
            map.put("emailNotify", false);
            map.put("smsNotify", false);
            for (UserSetting s : list) {
                String key = s.getSettingKey();
                String val = s.getSettingValue();
                if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val)) {
                    map.put(key, Boolean.parseBoolean(val));
                } else {
                    map.put(key, val);
                }
            }
            return success(map);
        } catch (Exception e) {
            return error("获取设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "保存当前用户设置")
    @PutMapping("/settings")
    public Result<Object> putSettings(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> body) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");
            Long userId = user.getId();
            userSettingMapper.deleteByUserId(userId);
            List<String> keys = Arrays.asList("loginAlert", "strangeDevice", "siteMessage", "emailNotify", "smsNotify");
            for (String key : keys) {
                if (!body.containsKey(key)) continue;
                Object v = body.get(key);
                String val = v == null ? "" : (v instanceof Boolean ? ((Boolean) v).toString() : v.toString());
                UserSetting s = new UserSetting();
                s.setUserId(userId);
                s.setSettingKey(key);
                s.setSettingValue(val);
                userSettingMapper.insert(s);
            }
            return success("保存成功");
        } catch (Exception e) {
            return error("保存失败：" + e.getMessage());
        }
    }

    private User getCurrentUserFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) return null;
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return authService.getCurrentUser(token);
    }

    @Operation(summary = "用户注册")
    @SecurityRequirements()
    @PostMapping("/register")
    public Result<Object> register(@RequestBody RegisterBody registerBody) {
        try {
            // 检查密码确认
            if (!registerBody.getPassword().equals(registerBody.getConfirmPassword())) {
                return error("两次输入的密码不一致，请重新输入");
            }

            // 创建用户对象
            User user = new User();
            user.setUsername(registerBody.getUsername());
            user.setPassword(registerBody.getPassword());
            user.setIdCard(registerBody.getIdCard());
            user.setRole(registerBody.getRole());
            user.setStatus(1); // 设置为启用状态

            // 先检查用户名是否已存在
            User existingUser = userService.findByUsername(registerBody.getUsername());
            if (existingUser != null) {
                return error("该用户名已被注册，请选择其他用户名");
            }

            // 检查协会会长（PRESIDENT）唯一性
            if ("PRESIDENT".equals(registerBody.getRole())) {
                User existingPresident = userService.findByRole("PRESIDENT");
                if (existingPresident != null) {
                    return error("系统中已存在协会会长账户，无法创建新的协会会长");
                }
            }

            // 调用认证服务进行注册
            boolean success = authService.register(user);

            if (success) {
                return success("注册成功");
            } else {
                return error("注册失败，请稍后重试");
            }
        } catch (Exception e) {
            return error("注册失败，请稍后重试");
        }
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Object> logout() {
        try {
            // 退出登录逻辑（可选实现）
            return success("退出登录成功");
        } catch (Exception e) {
            return error("退出登录失败");
        }
    }

    @Operation(summary = "刷新 AccessToken", description = "使用 RefreshToken 获取新的 AccessToken")
    @SecurityRequirements()
    @PostMapping("/refresh")
    public Result<Object> refreshToken(@RequestBody RefreshTokenBody refreshBody) {
        try {
            if (refreshBody.getRefreshToken() == null || refreshBody.getRefreshToken().isEmpty()) {
                return error("RefreshToken不能为空");
            }

            AuthResult result = authService.refreshToken(refreshBody.getRefreshToken());

            if (result.isSuccess()) {
                return success(new LoginResult(
                        result.getToken(),
                        result.getRefreshToken(),
                        result.getExpiresIn(),
                        result.getMessage(),
                        result.getUser()
                ));
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            return error("Token刷新失败，请重新登录");
        }
    }

    @Operation(summary = "检查账户锁定状态", description = "用于测试和管理，无需认证")
    @SecurityRequirements()
    @GetMapping("/check-lock/{username}")
    public Result<Object> checkAccountLock(@Parameter(description = "用户名", required = true) @PathVariable String username) {
        try {
            boolean locked = loginRateLimiter.isAccountLocked(username);
            long remainingTime = loginRateLimiter.getRemainingLockTime(username);

            return success(new AccountLockStatus(locked, remainingTime));
        } catch (Exception e) {
            return error("检查账户锁定状态失败");
        }
    }

    @Operation(summary = "解锁账户", description = "管理员使用")
    @PostMapping("/unlock/{username}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> unlockAccount(@Parameter(description = "用户名", required = true) @PathVariable String username) {
        try {
            loginRateLimiter.unlockAccount(username);
            return success("账户已解锁");
        } catch (Exception e) {
            return error("解锁账户失败");
        }
    }

    @Operation(summary = "忘记密码", description = "通过用户名+身份证验证身份后重置密码，无需登录")
    @SecurityRequirements()
    @PostMapping("/forgot-password")
    public Result<Object> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String idCard = body.get("idCard");
            String newPassword = body.get("newPassword");
            String confirmPassword = body.get("confirmPassword");

            if (username == null || username.trim().isEmpty()) return error("请输入用户名");
            if (idCard == null || idCard.trim().isEmpty()) return error("请输入身份证号");
            if (newPassword == null || newPassword.isEmpty()) return error("请输入新密码");
            if (newPassword.length() < 6) return error("新密码长度至少6位");
            if (!newPassword.equals(confirmPassword)) return error("两次输入的密码不一致");

            User user = userService.findByUsername(username.trim());
            if (user == null) return error("用户名不存在");
            if (user.getIdCard() == null || !user.getIdCard().trim().equalsIgnoreCase(idCard.trim())) {
                return error("身份证号与账号不匹配，请核对后重试");
            }
            if (user.getStatus() != null && user.getStatus() == 0) return error("该账户已被禁用，请联系管理员");

            String encoded = passwordEncoder.encode(newPassword);
            int n = userService.updatePassword(user.getId(), encoded);
            if (n > 0) return success("密码重置成功，请使用新密码登录");
            return error("重置失败，请稍后重试");
        } catch (Exception e) {
            return error("重置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "账号注销", description = "需验证密码后删除账户")
    @PostMapping("/delete-account")
    public Result<Object> deleteAccount(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> body) {
        try {
            User user = getCurrentUserFromHeader(authHeader);
            if (user == null) return error("请先登录");

            String password = body.get("password");
            if (password == null || password.isEmpty()) return error("请输入密码以确认注销");

            // 验证密码
            if (!userService.verifyPassword(password, user.getPassword())) {
                return error("密码错误，无法注销账户");
            }

            // 删除用户设置
            userSettingMapper.deleteByUserId(user.getId());

            // 删除用户账户
            int n = userService.deleteById(user.getId());
            if (n > 0) {
                return success("账户已注销");
            }
            return error("注销失败");
        } catch (Exception e) {
            return error("注销失败：" + e.getMessage());
        }
    }
}

/**
 * 登录请求体
 */
class LoginBody {
    private String username;
    private String password;
    private String code;
    private String uuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

/**
 * 注册请求体
 */
class RegisterBody {
    private String username;
    private String password;
    private String confirmPassword;
    private String idCard;
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

/**
 * 登录结果返回体
 * 支持双Token认证机制
 */
class LoginResult {
    private String token;           // AccessToken
    private String refreshToken;    // RefreshToken
    private Long expiresIn;         // AccessToken有效期（秒）
    private String message;
    private User user;

    public LoginResult(String token, String refreshToken, Long expiresIn, String message, User user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.message = message;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

/**
 * 刷新Token请求体
 */
class RefreshTokenBody {
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

/**
 * 账户锁定状态
 */
class AccountLockStatus {
    private boolean locked;
    private long remainingSeconds;

    public AccountLockStatus(boolean locked, long remainingSeconds) {
        this.locked = locked;
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
