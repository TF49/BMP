package com.badminton.bmp.modules.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VenueImage {
    private Long id;

    @NotNull
    @Positive
    private Long venueId;

    @NotBlank
    @Size(max = 500)
    private String imageUrl;

    @NotBlank
    @Pattern(regexp = "MAIN|DETAIL")
    private String imageType;

    @Min(0)
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
