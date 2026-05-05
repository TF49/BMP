package com.badminton.bmp.modules.booking.mapper;

import com.badminton.bmp.modules.booking.entity.BookingCourt;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BookingCourtMapper {

    @Select("<script>" +
            "SELECT bc.*, b.status AS booking_status, b.payment_status AS booking_payment_status, " +
            "b.booking_mode, b.member_id, c.court_name, c.court_code, c.venue_id, v.venue_name " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.booking_id IN " +
            "<foreach collection='bookingIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            " ORDER BY bc.booking_id, bc.court_id" +
            "</script>")
    List<BookingCourt> findByBookingIds(@Param("bookingIds") List<Long> bookingIds);

    @Select("SELECT bc.*, b.status AS booking_status, b.payment_status AS booking_payment_status, " +
            "b.booking_mode, b.member_id, c.court_name, c.court_code, c.venue_id, v.venue_name " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.booking_id = #{bookingId} ORDER BY bc.court_id")
    List<BookingCourt> findByBookingId(@Param("bookingId") Long bookingId);

    @Insert("<script>" +
            "INSERT INTO biz_booking_court (booking_id, court_id, booking_date, start_time, end_time, pricing_mode, unit_price, duration, line_amount, create_time) VALUES " +
            "<foreach collection='details' item='item' separator=','>" +
            "(#{item.bookingId}, #{item.courtId}, #{item.bookingDate}, #{item.startTime}, #{item.endTime}, #{item.pricingMode}, #{item.unitPrice}, #{item.duration}, #{item.lineAmount}, #{item.createTime})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("details") List<BookingCourt> details);

    @Delete("DELETE FROM biz_booking_court WHERE booking_id = #{bookingId}")
    int deleteByBookingId(@Param("bookingId") Long bookingId);

    @Select("<script>" +
            "SELECT bc.*, b.status AS booking_status, b.payment_status AS booking_payment_status, " +
            "b.booking_mode, b.member_id, c.court_name, c.court_code, c.venue_id, v.venue_name " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time &lt; #{endTime} AND bc.end_time &gt; #{startTime} " +
            "AND b.del_flag = 0 AND b.status IN (1, 2, 3) " +
            "<if test='excludeBookingId != null'> AND bc.booking_id != #{excludeBookingId} </if> " +
            "ORDER BY bc.start_time ASC" +
            "</script>")
    List<BookingCourt> findActiveConflicts(@Param("courtId") Long courtId,
                                           @Param("bookingDate") LocalDate bookingDate,
                                           @Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime,
                                           @Param("excludeBookingId") Long excludeBookingId);

    @Select("<script>" +
            "SELECT bc.*, b.status AS booking_status, b.payment_status AS booking_payment_status, " +
            "b.booking_mode, b.member_id, c.court_name, c.court_code, c.venue_id, v.venue_name " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time &lt; #{endTime} AND bc.end_time &gt; #{startTime} " +
            "AND b.booking_mode = 'PACKAGE' AND b.del_flag = 0 AND b.status IN (1, 2, 3) " +
            "<if test='excludeBookingId != null'> AND bc.booking_id != #{excludeBookingId} </if> " +
            "ORDER BY bc.start_time ASC" +
            "</script>")
    List<BookingCourt> findPackageConflicts(@Param("courtId") Long courtId,
                                            @Param("bookingDate") LocalDate bookingDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime,
                                            @Param("excludeBookingId") Long excludeBookingId);

    @Select("<script>" +
            "SELECT bc.booking_id, bc.start_time, bc.end_time, b.status, b.booking_mode, " +
            "m.member_name, m.member_type, m.member_level " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time &lt; #{endTime} AND bc.end_time &gt; #{startTime} " +
            "AND b.del_flag = 0 AND b.status IN (1, 2, 3) " +
            "ORDER BY bc.start_time ASC" +
            "</script>")
    List<Map<String, Object>> findOccupancyByTimeRange(@Param("courtId") Long courtId,
                                                       @Param("bookingDate") LocalDate bookingDate,
                                                       @Param("startTime") LocalTime startTime,
                                                       @Param("endTime") LocalTime endTime);

    @Select("SELECT bc.booking_id, bc.start_time, bc.end_time, b.status, b.booking_mode, " +
            "m.member_name, m.member_type, m.member_level " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_member m ON b.member_id = m.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time <= #{nowTime} AND bc.end_time > #{nowTime} " +
            "AND b.del_flag = 0 AND b.status IN (1, 2, 3) " +
            "ORDER BY bc.start_time ASC")
    List<Map<String, Object>> findCurrentOccupancy(@Param("courtId") Long courtId,
                                                   @Param("bookingDate") LocalDate bookingDate,
                                                   @Param("nowTime") LocalTime nowTime);

    @Select("SELECT COUNT(*) FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time < #{endTime} AND bc.end_time > #{startTime} " +
            "AND b.del_flag = 0 AND b.status IN (1, 2, 3)")
    int countBookingsForTimeRange(@Param("courtId") Long courtId,
                                  @Param("bookingDate") LocalDate bookingDate,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    @Select("SELECT COUNT(*) FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "WHERE bc.court_id = #{courtId} " +
            "AND bc.booking_date = #{bookingDate} " +
            "AND bc.start_time < #{endTime} AND bc.end_time > #{startTime} " +
            "AND b.booking_mode = 'SHARED' " +
            "AND b.del_flag = 0 AND b.status IN (1, 2, 3)")
    int countSharedBookingsForTimeRange(@Param("courtId") Long courtId,
                                        @Param("bookingDate") LocalDate bookingDate,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);

    @Select("SELECT bc.*, b.status AS booking_status, b.payment_status AS booking_payment_status, " +
            "b.booking_mode, b.member_id, c.court_name, c.court_code, c.venue_id, v.venue_name " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE bc.court_id = #{courtId} AND b.del_flag = 0 AND b.status IN (1, 2, 3) " +
            "ORDER BY bc.booking_date, bc.start_time")
    List<BookingCourt> findActiveBookingDetailsByCourtId(@Param("courtId") Long courtId);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "WHERE bc.court_id = #{courtId} AND b.del_flag = 0 AND b.status IN " +
            "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>#{status}</foreach>" +
            "</script>")
    int countByCourtIdAndStatusIn(@Param("courtId") Long courtId, @Param("statusList") List<Integer> statusList);

    @Select("<script>" +
            "SELECT DISTINCT c.venue_id AS venueId FROM biz_booking_court bc " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE bc.booking_id = #{bookingId} " +
            "ORDER BY c.venue_id LIMIT 1" +
            "</script>")
    Long findVenueIdByBookingId(@Param("bookingId") Long bookingId);

    @Select("<script>" +
            "SELECT bc.id AS detail_id, bc.booking_date AS date, COUNT(DISTINCT bc.booking_id) AS cnt " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "AND bc.booking_date &gt;= #{startDate} AND bc.booking_date &lt;= #{endDate} " +
            "GROUP BY bc.booking_date ORDER BY bc.booking_date" +
            "</script>")
    List<Map<String, Object>> countDistinctBookingsByDate(@Param("venueId") Long venueId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT COUNT(DISTINCT bc.booking_id) FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 AND bc.booking_date = #{bookingDate} " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "</script>")
    int countDistinctBookingsByBookingDate(@Param("venueId") Long venueId,
                                           @Param("bookingDate") LocalDate bookingDate);

    @Select("<script>" +
            "SELECT HOUR(bc.start_time) AS hour, COUNT(DISTINCT bc.booking_id) AS cnt " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 AND bc.booking_date = #{bookingDate} " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "GROUP BY HOUR(bc.start_time) ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> countDistinctBookingsStartHourForDate(@Param("venueId") Long venueId,
                                                                    @Param("bookingDate") LocalDate bookingDate);

    @Select("<script>" +
            "SELECT c.venue_id AS venueId, v.venue_name AS venueName, bc.booking_date AS date, COUNT(DISTINCT bc.booking_id) AS cnt " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "AND bc.booking_date &gt;= #{startDate} AND bc.booking_date &lt;= #{endDate} " +
            "GROUP BY c.venue_id, v.venue_name, bc.booking_date ORDER BY c.venue_id, bc.booking_date" +
            "</script>")
    List<Map<String, Object>> countDistinctBookingsByVenueAndDate(@Param("venueId") Long venueId,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT bc.booking_date AS date, HOUR(bc.start_time) AS hour, COUNT(DISTINCT bc.booking_id) AS cnt " +
            "FROM biz_booking_court bc " +
            "LEFT JOIN biz_booking b ON bc.booking_id = b.id " +
            "LEFT JOIN sys_court c ON bc.court_id = c.id " +
            "WHERE b.del_flag = 0 AND b.status != 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "AND bc.booking_date &gt;= #{startDate} AND bc.booking_date &lt;= #{endDate} " +
            "AND HOUR(bc.start_time) &gt;= 8 AND HOUR(bc.start_time) &lt;= 21 " +
            "GROUP BY bc.booking_date, HOUR(bc.start_time) ORDER BY bc.booking_date, hour" +
            "</script>")
    List<Map<String, Object>> countDistinctBookingsByHourAndDate(@Param("venueId") Long venueId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
}
