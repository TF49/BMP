package com.badminton.bmp.modules.course.service;

import com.badminton.bmp.modules.course.entity.Course;
import java.util.List;
import java.util.Map;

public interface CourseService {
    Course findById(Long id);
    List<Course> findAll(Long coachId, Long courtId, Integer status, String keyword, String startTime, String endTime, int page, int size);
    int count(Long coachId, Long courtId, Integer status, String keyword, String startTime, String endTime);
    int add(Course course);
    int update(Course course);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Map<String, Object> getStatistics();
    int updateCurrentStudents(Long id, Integer currentStudents);

    /**
     * 定时任务：按当前时间将「报名中」→「进行中」、「进行中」→「已结束」（仅时间已到的课程）
     */
    void autoUpdateCourseStatusByTime();
}
