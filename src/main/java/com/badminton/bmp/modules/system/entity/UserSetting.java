package com.badminton.bmp.modules.system.entity;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserSetting {
    private Long id;

    @NotNull
    @Positive
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String settingKey;

    @Size(max = 500)
    private String settingValue;
}
