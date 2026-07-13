package com.badminton.bmp.modules.coach.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoachStudentPageVO<T> {
    private List<T> data;
    private int total;
    private int page;
    private int size;
    private int pages;
}
