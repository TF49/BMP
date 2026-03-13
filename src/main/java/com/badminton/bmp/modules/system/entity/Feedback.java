package com.badminton.bmp.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;

    @NotNull
    @Positive
    private Long userId;

    @NotBlank
    @Size(max = 2000)
    private String content;

    @Size(max = 100)
    private String contact;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
