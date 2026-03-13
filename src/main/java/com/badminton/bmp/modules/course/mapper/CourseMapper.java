package com.badminton.bmp.modules.course.mapper;

import com.badminton.bmp.modules.course.entity.Course;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 课程Mapper接口
 */
@Mapper
public interface CourseMapper {

    @Select("SELECT c.*, co.coach_name, IFNULL(ct.court_name, '') as court_name FROM biz_course c " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE c.id = #{id} AND c.del_flag = 0")
    Course findById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT c.*, co.coach_name, IFNULL(ct.court_name, '') as court_name FROM biz_course c " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "<if test='coachId != null'> AND c.coach_id = #{coachId} </if>" +
            "<if test='courtId != null'> AND c.court_id = #{courtId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND c.course_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='startTime != null and startTime != \"\"'> AND CONCAT(c.course_date,' ',c.start_time) &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != \"\"'> AND CONCAT(c.course_date,' ',c.end_time) &lt;= #{endTime} </if>" +
            "ORDER BY c.course_date DESC, c.start_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Course> findAll(@Param("venueId") Long venueId,
                        @Param("coachId") Long coachId,
                        @Param("courtId") Long courtId,
                        @Param("status") Integer status,
                        @Param("keyword") String keyword,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course c " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "<if test='coachId != null'> AND c.coach_id = #{coachId} </if>" +
            "<if test='courtId != null'> AND c.court_id = #{courtId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND c.course_name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='startTime != null and startTime != \"\"'> AND CONCAT(c.course_date,' ',c.start_time) &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != \"\"'> AND CONCAT(c.course_date,' ',c.end_time) &lt;= #{endTime} </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("coachId") Long coachId,
              @Param("courtId") Long courtId,
              @Param("status") Integer status,
              @Param("keyword") String keyword,
              @Param("startTime") String startTime,
              @Param("endTime") String endTime);

    @Insert("INSERT INTO biz_course (course_name, coach_id, court_id, course_content, course_price, " +
            "course_duration, course_date, start_time, end_time, max_students, current_students, version, status, " +
            "create_time, update_time, del_flag) " +
            "VALUES (#{courseName}, #{coachId}, #{courtId}, #{courseContent}, #{coursePrice}, " +
            "#{courseDuration}, #{courseDate}, #{startTime}, #{endTime}, #{maxStudents}, #{currentStudents}, 0, " +
            "#{status}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("UPDATE biz_course SET course_name = #{courseName}, coach_id = #{coachId}, court_id = #{courtId}, " +
            "course_content = #{courseContent}, course_price = #{coursePrice}, course_duration = #{courseDuration}, " +
            "course_date = #{courseDate}, start_time = #{startTime}, end_time = #{endTime}, " +
            "max_students = #{maxStudents}, status = #{status}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(Course course);

    @Update("UPDATE biz_course SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE biz_course SET status = #{status}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新当前报名人数（带乐观锁）
     * @param id 课程ID
     * @param currentStudents 当前报名人数
     * @param version 当前版本号
     * @return 影响的行数（0表示版本冲突）
     */
    @Update("UPDATE biz_course SET current_students = #{currentStudents}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND version = #{version} AND del_flag = 0")
    int updateCurrentStudentsWithVersion(@Param("id") Long id,
                                         @Param("currentStudents") Integer currentStudents,
                                         @Param("version") Integer version);

    /**
     * 更新当前报名人数（不带乐观锁，兼容旧代码）
     * @param id 课程ID
     * @param currentStudents 当前报名人数
     * @return 影响的行数
     */
    @Update("UPDATE biz_course SET current_students = #{currentStudents}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateCurrentStudents(@Param("id") Long id, @Param("currentStudents") Integer currentStudents);

    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_course WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT COUNT(*) FROM biz_course WHERE del_flag = 0")
    int countAll();

    /** 按场馆过滤的统计（场馆管理员用），无场馆时与 countAll/countByStatus 一致 */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course c " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "</script>")
    int countAllFiltered(@Param("venueId") Long venueId);

    @Select("<script>" +
            "SELECT CAST(c.status AS SIGNED) AS status, COUNT(*) as count FROM biz_course c " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "GROUP BY c.status</script>")
    List<Map<String, Object>> countByStatusFiltered(@Param("venueId") Long venueId);

    /**
     * 定时任务：将「报名中」且已到开始时间的课程改为「进行中」
     * 条件：status=1 且 (course_date &lt; 今日 或 (course_date=今日 且 start_time &lt;= 当前时间))
     */
    @Update("UPDATE biz_course SET status = 2, update_time = NOW() WHERE del_flag = 0 AND status = 1 " +
            "AND (course_date < #{today} OR (course_date = #{today} AND start_time <= #{nowTime}))")
    int batchUpdateEnrollingToOngoing(@Param("today") LocalDate today, @Param("nowTime") LocalTime nowTime);

    /**
     * 定时任务：将「进行中」且已到结束时间的课程改为「已结束」
     * 条件：status=2 且 (course_date &lt; 今日 或 (course_date=今日 且 end_time &lt;= 当前时间))
     */
    @Update("UPDATE biz_course SET status = 3, update_time = NOW() WHERE del_flag = 0 AND status = 2 " +
            "AND (course_date < #{today} OR (course_date = #{today} AND end_time <= #{nowTime}))")
    int batchUpdateOngoingToFinished(@Param("today") LocalDate today, @Param("nowTime") LocalTime nowTime);

    /**
     * 统计指定教练的指定状态的课程数量（用于删除前检查）
     * @param coachId 教练ID
     * @param statusList 状态列表（1-报名中，2-进行中）
     * @return 课程数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course " +
            "WHERE coach_id = #{coachId} AND del_flag = 0 " +
            "AND status IN " +
            "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach>" +
            "</script>")
    int countByCoachIdAndStatusIn(@Param("coachId") Long coachId, 
                                  @Param("statusList") List<Integer> statusList);

    /**
     * 统计指定场地的指定状态的课程数量（用于删除前检查）
     * @param courtId 场地ID
     * @param statusList 状态列表（1-报名中，2-进行中）
     * @return 课程数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course " +
            "WHERE court_id = #{courtId} AND del_flag = 0 " +
            "AND status IN " +
            "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach>" +
            "</script>")
    int countByCourtIdAndStatusIn(@Param("courtId") Long courtId, 
                                  @Param("statusList") List<Integer> statusList);

    /**
     * 按课程名称汇总当前报名人数（与课程管理“人数”一致，用于热度排行榜）
     * 同一课程名称的多条排课其 current_students 相加，如初学入门班两期各12人则合计24人。
     */
    @Select("<script>" +
            "SELECT c.course_name AS courseName, SUM(COALESCE(c.current_students, 0)) AS signupCount " +
            "FROM biz_course c " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "GROUP BY c.course_name " +
            "HAVING signupCount > 0 " +
            "ORDER BY signupCount DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> sumCurrentStudentsByCourseName(@Param("venueId") Long venueId, @Param("limit") int limit);
}
