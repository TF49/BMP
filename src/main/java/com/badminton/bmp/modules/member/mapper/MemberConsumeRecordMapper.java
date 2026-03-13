package com.badminton.bmp.modules.member.mapper;

import com.badminton.bmp.modules.member.entity.MemberConsumeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员消费记录Mapper
 */
@Mapper
public interface MemberConsumeRecordMapper {

    @Select("SELECT * FROM biz_member_consume_record WHERE member_id = #{memberId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<MemberConsumeRecord> findByMemberId(@Param("memberId") Long memberId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM biz_member_consume_record WHERE member_id = #{memberId}")
    int countByMemberId(@Param("memberId") Long memberId);

    @Select("<script>" +
            "SELECT * FROM biz_member_consume_record " +
            "WHERE 1=1 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "<if test='businessType != null and businessType != \"\"'> AND business_type = #{businessType} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<MemberConsumeRecord> findByVenueIdWithPage(@Param("venueId") Long venueId,
                                                    @Param("businessType") String businessType,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_member_consume_record WHERE 1=1 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "<if test='businessType != null and businessType != \"\"'> AND business_type = #{businessType} </if>" +
            "</script>")
    int countByVenueId(@Param("venueId") Long venueId, @Param("businessType") String businessType);

    /**
     * 插入消费记录
     * @param record 消费记录对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO biz_member_consume_record (member_id, business_type, business_id, description, amount, " +
            "payment_method, payment_status, before_balance, venue_id, after_balance, remark, create_time) " +
            "VALUES (#{memberId}, #{businessType}, #{businessId}, #{description}, #{amount}, " +
            "#{paymentMethod}, #{paymentStatus}, #{beforeBalance}, #{venueId}, #{afterBalance}, #{remark}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberConsumeRecord record);
}

