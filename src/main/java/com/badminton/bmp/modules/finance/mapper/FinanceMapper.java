package com.badminton.bmp.modules.finance.mapper;

import com.badminton.bmp.modules.finance.entity.Finance;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务Mapper接口
 */
@Mapper
public interface FinanceMapper {

    /**
     * 根据ID查询财务记录（联表查询场馆信息）
     */
    @Select("SELECT f.*, v.venue_name FROM biz_finance f " +
            "LEFT JOIN sys_venue v ON f.venue_id = v.id " +
            "WHERE f.id = #{id} AND f.del_flag = 0")
    Finance findById(@Param("id") Long id);

    /**
     * 分页查询财务记录
     */
    @Select("<script>" +
            "SELECT f.*, v.venue_name FROM biz_finance f " +
            "LEFT JOIN sys_venue v ON f.venue_id = v.id " +
            "WHERE f.del_flag = 0 " +
            "<if test='venueId != null'> AND f.venue_id = #{venueId} </if>" +
            "<if test='businessType != null and businessType != \"\"'> AND f.business_type = #{businessType} </if>" +
            "<if test='incomeExpenseType != null'> AND f.income_expense_type = #{incomeExpenseType} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(f.create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(f.create_time) &lt;= #{endDate} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (f.finance_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR f.remark LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY f.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Finance> findAll(@Param("venueId") Long venueId,
                          @Param("businessType") String businessType,
                          @Param("incomeExpenseType") Integer incomeExpenseType,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate,
                          @Param("keyword") String keyword,
                          @Param("offset") int offset,
                          @Param("size") int size);

    /**
     * 统计总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_finance f " +
            "LEFT JOIN sys_venue v ON f.venue_id = v.id " +
            "WHERE f.del_flag = 0 " +
            "<if test='venueId != null'> AND f.venue_id = #{venueId} </if>" +
            "<if test='businessType != null and businessType != \"\"'> AND f.business_type = #{businessType} </if>" +
            "<if test='incomeExpenseType != null'> AND f.income_expense_type = #{incomeExpenseType} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(f.create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(f.create_time) &lt;= #{endDate} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (f.finance_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR f.remark LIKE CONCAT('%', #{keyword}, '%') " +
            "OR v.venue_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("businessType") String businessType,
              @Param("incomeExpenseType") Integer incomeExpenseType,
              @Param("startDate") String startDate,
              @Param("endDate") String endDate,
              @Param("keyword") String keyword);

    /**
     * 新增财务记录
     */
    @Insert("INSERT INTO biz_finance (finance_no, business_type, business_id, income_expense_type, " +
            "amount, payment_method, venue_id, operator, operator_id, remark, record_source, " +
            "is_reconciled, create_time, del_flag) " +
            "VALUES (#{financeNo}, #{businessType}, #{businessId}, #{incomeExpenseType}, " +
            "#{amount}, #{paymentMethod}, #{venueId}, #{operator}, #{operatorId}, #{remark}, #{recordSource}, " +
            "#{isReconciled}, #{createTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Finance finance);

    /**
     * 更新财务记录
     */
    @Update("UPDATE biz_finance SET business_type = #{businessType}, business_id = #{businessId}, " +
            "income_expense_type = #{incomeExpenseType}, amount = #{amount}, payment_method = #{paymentMethod}, " +
            "venue_id = #{venueId}, operator = #{operator}, remark = #{remark}, " +
            "update_time = #{updateTime}, last_modifier = #{lastModifier}, last_modifier_id = #{lastModifierId} " +
            "WHERE id = #{id} AND del_flag = 0")
    int update(Finance finance);

    /**
     * 逻辑删除财务记录
     */
    @Update("UPDATE biz_finance SET del_flag = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查财务单号是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_finance " +
            "WHERE finance_no = #{financeNo} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByFinanceNo(@Param("financeNo") String financeNo,
                              @Param("excludeId") Long excludeId);

    /**
     * 统计总数（不带过滤条件）
     */
    @Select("SELECT COUNT(*) FROM biz_finance WHERE del_flag = 0")
    int countAll();

    /**
     * 按收支类型汇总金额
     */
    @Select("<script>" +
            "SELECT COALESCE(SUM(amount), 0) FROM biz_finance " +
            "WHERE del_flag = 0 AND income_expense_type = #{incomeExpenseType} " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(create_time) &lt;= #{endDate} </if>" +
            "</script>")
    BigDecimal sumByType(@Param("venueId") Long venueId,
                         @Param("incomeExpenseType") Integer incomeExpenseType,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    /**
     * 按业务类型汇总金额（用于饼图）
     */
    @Select("<script>" +
            "SELECT business_type as type, SUM(amount) as amount " +
            "FROM biz_finance " +
            "WHERE del_flag = 0 AND income_expense_type = 1 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(create_time) &lt;= #{endDate} </if>" +
            "GROUP BY business_type " +
            "ORDER BY amount DESC" +
            "</script>")
    List<Map<String, Object>> sumByBusinessType(@Param("venueId") Long venueId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 按日期汇总金额（用于趋势图）
     */
    @Select("<script>" +
            "SELECT DATE(create_time) as date, income_expense_type as type, SUM(amount) as amount " +
            "FROM biz_finance " +
            "WHERE del_flag = 0 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(create_time) &lt;= #{endDate} </if>" +
            "GROUP BY DATE(create_time), income_expense_type " +
            "ORDER BY date" +
            "</script>")
    List<Map<String, Object>> sumByDate(@Param("venueId") Long venueId,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    /**
     * 获取今日最大序号（用于生成财务单号）
     */
    @Select("SELECT COUNT(*) FROM biz_finance WHERE finance_no LIKE CONCAT(#{prefix}, '%')")
    int getMaxSequenceToday(@Param("prefix") String prefix);

    /**
     * 按场馆 + 日期汇总收入（用于 Dashboard 各场馆收入趋势）
     */
    @Select("<script>" +
            "SELECT f.venue_id AS venueId, v.venue_name AS venueName, DATE(f.create_time) AS date, SUM(f.amount) AS amount " +
            "FROM biz_finance f " +
            "LEFT JOIN sys_venue v ON f.venue_id = v.id " +
            "WHERE f.del_flag = 0 AND f.income_expense_type = 1 " +
            "<if test='venueId != null'> AND f.venue_id = #{venueId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(f.create_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(f.create_time) &lt;= #{endDate} </if>" +
            "GROUP BY f.venue_id, v.venue_name, DATE(f.create_time) " +
            "ORDER BY f.venue_id, date" +
            "</script>")
    List<Map<String, Object>> sumIncomeByVenueAndDate(@Param("venueId") Long venueId,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);

    /**
     * 最近财务记录（用于最近活动）
     */
    @Select("<script>" +
            "SELECT f.id, f.create_time, f.amount, f.income_expense_type, v.venue_name " +
            "FROM biz_finance f " +
            "LEFT JOIN sys_venue v ON f.venue_id = v.id " +
            "WHERE f.del_flag = 0 " +
            "<if test='venueId != null'> AND f.venue_id = #{venueId} </if>" +
            "ORDER BY f.create_time DESC LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findRecentFinances(@Param("venueId") Long venueId,
                                                 @Param("limit") int limit);

    /**
     * 按赛事汇总赛事收入（business_id 为报名单 id，联表 biz_tournament_registration）
     * 用于 Dashboard 赛事带动效果图
     */
    @Select("SELECT COALESCE(SUM(f.amount), 0) FROM biz_finance f " +
            "INNER JOIN biz_tournament_registration tr ON tr.id = f.business_id AND tr.tournament_id = #{tournamentId} AND tr.del_flag = 0 " +
            "WHERE f.del_flag = 0 AND f.business_type = 'TOURNAMENT' AND f.income_expense_type = 1")
    BigDecimal sumTournamentIncomeByTournamentId(@Param("tournamentId") Long tournamentId);
}
