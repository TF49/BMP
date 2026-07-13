package com.badminton.bmp.modules.coach.dto;

import lombok.Data;

@Data
public class CoachStudentListQuery {
    private int page = 1;
    private int size = 20;
    private String keyword;
    private String memberType;
    private Boolean riskOnly;
    private Boolean todayOnly;
    private String orderBy = "lastLessonTime";
    private String orderDirection = "DESC";
}
