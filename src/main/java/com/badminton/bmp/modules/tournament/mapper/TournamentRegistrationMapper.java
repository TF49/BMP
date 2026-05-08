package com.badminton.bmp.modules.tournament.mapper;

import com.badminton.bmp.modules.tournament.entity.TournamentRegistration;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TournamentRegistrationMapper {
    @Select("SELECT tr.*, " +
            "COALESCE(tr.registrant_name, m1.member_name) as member_name, " +
            "COALESCE(tr.partner_name_snapshot, m2.member_name) as partner_name, " +
            "t.tournament_name, t.tournament_start as tournament_start_time, t.tournament_end as tournament_end_time, " +
            "IFNULL(v.venue_name, '') as venue_name " +
            "FROM biz_tournament_registration tr " +
            "LEFT JOIN sys_member m1 ON tr.member_id = m1.id " +
            "LEFT JOIN sys_member m2 ON tr.partner_id = m2.id " +
            "LEFT JOIN biz_tournament t ON tr.tournament_id = t.id " +
            "LEFT JOIN sys_venue v ON t.venue_id = v.id " +
            "WHERE tr.id = #{id} AND tr.del_flag = 0")
    TournamentRegistration findById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT tr.*, COALESCE(tr.registrant_name, m1.member_name) as member_name, " +
            "COALESCE(tr.partner_name_snapshot, m2.member_name) as partner_name, t.tournament_name, " +
            "t.tournament_start as tournament_start_time, t.tournament_end as tournament_end_time, IFNULL(v.venue_name, '') as venue_name " +
            "FROM biz_tournament_registration tr " +
            "LEFT JOIN sys_member m1 ON tr.member_id = m1.id " +
            "LEFT JOIN sys_member m2 ON tr.partner_id = m2.id " +
            "LEFT JOIN biz_tournament t ON tr.tournament_id = t.id " +
            "LEFT JOIN sys_venue v ON t.venue_id = v.id " +
            "WHERE tr.del_flag = 0 " +
            "<if test='venueId != null'> AND t.venue_id = #{venueId} </if>" +
            "<if test='tournamentId != null'> AND tr.tournament_id = #{tournamentId} </if>" +
            "<if test='participantMemberId != null'> AND (tr.member_id = #{participantMemberId} OR tr.partner_id = #{participantMemberId}) </if>" +
            "<if test='memberId != null and participantMemberId == null'> AND tr.member_id = #{memberId} </if>" +
            "<if test='status != null'> AND tr.status = #{status} </if>" +
            "<if test='registrationNo != null and registrationNo != \"\"'> " +
            "AND tr.registration_no LIKE CONCAT('%', #{registrationNo}, '%') </if>" +
            "<if test='memberKeyword != null and memberKeyword != \"\"'> " +
            "AND (m1.member_name LIKE CONCAT('%', #{memberKeyword}, '%') " +
            "OR m1.phone LIKE CONCAT('%', #{memberKeyword}, '%')) </if>" +
            "<if test='startTime != null'> AND tr.create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND tr.create_time &lt;= #{endTime} </if>" +
            "ORDER BY tr.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<TournamentRegistration> findAll(@Param("venueId") Long venueId,
                                        @Param("tournamentId") Long tournamentId,
                                        @Param("memberId") Long memberId,
                                        @Param("participantMemberId") Long participantMemberId,
                                        @Param("status") Integer status,
                                        @Param("registrationNo") String registrationNo,
                                        @Param("memberKeyword") String memberKeyword,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_tournament_registration tr " +
            "LEFT JOIN sys_member m1 ON tr.member_id = m1.id " +
            "LEFT JOIN biz_tournament t ON tr.tournament_id = t.id " +
            "WHERE tr.del_flag = 0 " +
            "<if test='venueId != null'> AND t.venue_id = #{venueId} </if>" +
            "<if test='tournamentId != null'> AND tr.tournament_id = #{tournamentId} </if>" +
            "<if test='participantMemberId != null'> AND (tr.member_id = #{participantMemberId} OR tr.partner_id = #{participantMemberId}) </if>" +
            "<if test='memberId != null and participantMemberId == null'> AND tr.member_id = #{memberId} </if>" +
            "<if test='status != null'> AND tr.status = #{status} </if>" +
            "<if test='registrationNo != null and registrationNo != \"\"'> " +
            "AND tr.registration_no LIKE CONCAT('%', #{registrationNo}, '%') </if>" +
            "<if test='memberKeyword != null and memberKeyword != \"\"'> " +
            "AND (m1.member_name LIKE CONCAT('%', #{memberKeyword}, '%') " +
            "OR m1.phone LIKE CONCAT('%', #{memberKeyword}, '%')) </if>" +
            "<if test='startTime != null'> AND tr.create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND tr.create_time &lt;= #{endTime} </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("tournamentId") Long tournamentId,
              @Param("memberId") Long memberId,
              @Param("participantMemberId") Long participantMemberId,
              @Param("status") Integer status,
              @Param("registrationNo") String registrationNo,
              @Param("memberKeyword") String memberKeyword,
              @Param("startTime") LocalDateTime startTime,
              @Param("endTime") LocalDateTime endTime);

    /**
     * 检查指定会员在该赛事下是否已有有效报名（作为主报名人或搭档）
     * 有效状态：1-待支付，2-已支付，3-已参赛
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_tournament_registration " +
            "WHERE tournament_id = #{tournamentId} AND del_flag = 0 AND status IN (1, 2, 3) " +
            "AND (member_id = #{memberId} OR partner_id = #{memberId}) " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    int countValidRegistrationInTournament(@Param("tournamentId") Long tournamentId,
                                           @Param("memberId") Long memberId,
                                           @Param("excludeId") Long excludeId);

    @Insert("INSERT INTO biz_tournament_registration (registration_no, tournament_id, member_id, partner_id, " +
            "registrant_name, registrant_phone, registrant_id_card, event_type_snapshot, event_type_name_snapshot, " +
            "partner_name_snapshot, partner_phone_snapshot, entry_fee, payment_method, payment_status, status, match_result, remark, " +
            "create_time, update_time, del_flag) " +
            "VALUES (#{registrationNo}, #{tournamentId}, #{memberId}, #{partnerId}, " +
            "#{registrantName}, #{registrantPhone}, #{registrantIdCard}, #{eventTypeSnapshot}, #{eventTypeNameSnapshot}, " +
            "#{partnerNameSnapshot}, #{partnerPhoneSnapshot}, #{entryFee}, #{paymentMethod}, #{paymentStatus}, #{status}, #{matchResult}, #{remark}, " +
            "#{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TournamentRegistration registration);

    @Update("UPDATE biz_tournament_registration SET tournament_id = #{tournamentId}, member_id = #{memberId}, " +
            "partner_id = #{partnerId}, registrant_name = #{registrantName}, registrant_phone = #{registrantPhone}, registrant_id_card = #{registrantIdCard}, " +
            "event_type_snapshot = #{eventTypeSnapshot}, event_type_name_snapshot = #{eventTypeNameSnapshot}, " +
            "partner_name_snapshot = #{partnerNameSnapshot}, partner_phone_snapshot = #{partnerPhoneSnapshot}, " +
            "entry_fee = #{entryFee}, payment_method = #{paymentMethod}, " +
            "payment_status = #{paymentStatus}, status = #{status}, match_result = #{matchResult}, " +
            "remark = #{remark}, update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(TournamentRegistration registration);

    @Update("UPDATE biz_tournament_registration SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE biz_tournament_registration SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_tournament_registration " +
            "WHERE registration_no = #{registrationNo} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByRegistrationNo(@Param("registrationNo") String registrationNo,
                                  @Param("excludeId") Long excludeId);

    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_tournament_registration " +
            "WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT COUNT(*) FROM biz_tournament_registration WHERE del_flag = 0")
    int countAll();

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_tournament_registration tr " +
            "LEFT JOIN biz_tournament t ON tr.tournament_id = t.id " +
            "WHERE tr.del_flag = 0 AND tr.payment_status = #{paymentStatus} " +
            "<if test='venueId != null'> AND t.venue_id = #{venueId} </if>" +
            "</script>")
    int countByPaymentStatus(@Param("venueId") Long venueId,
                             @Param("paymentStatus") Integer paymentStatus);

    @Select("SELECT COALESCE(SUM(CASE " +
            "WHEN tr.status IN (1, 2, 3) THEN " +
            "CASE WHEN tr.event_type_snapshot IN ('MD', 'WD', 'XD') OR tr.partner_id IS NOT NULL THEN 2 ELSE 1 END " +
            "ELSE 0 END), 0) " +
            "FROM biz_tournament_registration tr " +
            "WHERE tr.tournament_id = #{tournamentId} AND tr.del_flag = 0")
    int countOccupiedParticipants(@Param("tournamentId") Long tournamentId);

    /**
     * 定时任务：查询「已支付」且关联赛事已到开始时间的报名ID（用于自动改为已参赛）
     * 条件：tr.status=2 且 t.tournament_start <= #{now}
     */
    @Select("SELECT tr.id FROM biz_tournament_registration tr INNER JOIN biz_tournament t ON tr.tournament_id = t.id " +
            "WHERE tr.del_flag = 0 AND tr.status = 2 AND t.tournament_start <= #{now}")
    List<Long> findRegistrationIdsToParticipate(@Param("now") LocalDateTime now);
}
