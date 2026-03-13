package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.entity.Feedback;
import com.badminton.bmp.modules.system.mapper.FeedbackMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "联系留言", description = "前台悬浮联系表单提交")
@RestController
@RequestMapping("/api/contact")
public class ContactController extends BaseController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Data
    public static class ContactForm {
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名不能超过50个字符")
        private String name;

        @NotBlank(message = "联系方式不能为空")
        @Size(max = 100, message = "联系方式不能超过100个字符")
        private String contact;

        @NotBlank(message = "留言内容不能为空")
        @Size(max = 500, message = "留言内容不能超过500个字符")
        private String message;
    }

    @Operation(summary = "提交留言", description = "匿名提交，name+message合并为content")
    @SecurityRequirements()
    @PostMapping("/submit")
    public Result<Object> submit(@Valid @RequestBody ContactForm form) {
        Feedback f = new Feedback();
        f.setContent("[" + form.getName() + "] " + form.getMessage());
        f.setContact(form.getContact());
        f.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(f);
        return success("留言已提交，我们会尽快联系您！");
    }
}
