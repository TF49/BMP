package com.badminton.bmp.modules.tournament.service;

import com.badminton.bmp.modules.tournament.entity.TournamentRegistration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TournamentRegistrationService {
    TournamentRegistration findById(Long id);
    List<TournamentRegistration> findAll(Long tournamentId, Long memberId, Integer status,
                                         String registrationNo, String memberKeyword,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         int page, int size);
    int count(Long tournamentId, Long memberId, Integer status,
              String registrationNo, String memberKeyword,
              LocalDateTime startTime, LocalDateTime endTime);
    int add(TournamentRegistration registration);
    int update(TournamentRegistration registration);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Map<String, Object> getStatistics();
    String generateRegistrationNo();

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param registrationId 报名ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    int processPayment(Long registrationId, String paymentMethod);

    /**
     * 普通用户支付本人报名单（含余额扣减、消费记录）
     * @param registrationId 报名ID
     * @param paymentMethod 支付方式
     * @param userId 当前用户ID
     * @return 处理结果
     */
    int processMemberPayment(Long registrationId, String paymentMethod, Long userId);

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param registrationId 报名ID
     * @return 处理结果
     */
    int processRefund(Long registrationId);

    /**
     * 定时任务：按赛事开始时间将「已支付」的报名自动改为「已参赛」
     */
    void autoUpdateTournamentRegistrationStatusByTime();
}
