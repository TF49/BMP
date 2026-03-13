package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.entity.Feedback;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.mapper.FeedbackMapper;
import com.badminton.bmp.modules.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户反馈", description = "帮助与反馈提交，可匿名")
@RestController
@RequestMapping("/api")
public class FeedbackController extends BaseController {

    @Autowired
    private AuthService authService;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Operation(summary = "提交反馈", description = "可匿名，可选填 contact")
    @SecurityRequirements()
    @PostMapping("/feedback")
    public Result<Object> submitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            Long userId = null;
            if (authHeader != null && !authHeader.isEmpty()) {
                String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                User user = authService.getCurrentUser(token);
                if (user != null) userId = user.getId();
            }
            String content = body.get("content");
            if (content == null || content.trim().isEmpty()) return error("请填写反馈内容");
            String contact = body.get("contact");
            Feedback f = new Feedback();
            f.setUserId(userId);
            f.setContent(content.trim());
            f.setContact(contact != null ? contact.trim() : null);
            f.setCreateTime(LocalDateTime.now());
            feedbackMapper.insert(f);
            return success("提交成功");
        } catch (Exception e) {
            return error("提交失败：" + e.getMessage());
        }
    }
}
