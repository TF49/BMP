package com.badminton.bmp.modules.tournament.service;

import com.badminton.bmp.modules.tournament.entity.Tournament;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TournamentService {
    Tournament findById(Long id);
    /**
     * 查找所有赛事（支持条件筛选和分页）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param type 赛事类型（SINGLE/DOUBLE/MIXED，可选）
     * @param keyword 关键词（匹配赛事名称）
     * @param startTime 开始时间（按比赛开始时间筛选，可选）
     * @param endTime 结束时间（按比赛开始时间筛选，可选）
     * @param page 页码
     * @param size 每页数量
     * @return 赛事列表
     */
    List<Tournament> findAll(Long venueId, Integer status, String type, String keyword,
                             LocalDateTime startTime, LocalDateTime endTime,
                             int page, int size);

    /**
     * 统计赛事数量（支持条件筛选）
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param type 赛事类型（SINGLE/DOUBLE/MIXED，可选）
     * @param keyword 关键词（匹配赛事名称）
     * @param startTime 开始时间（按比赛开始时间筛选，可选）
     * @param endTime 结束时间（按比赛开始时间筛选，可选）
     * @return 赛事数量
     */
    int count(Long venueId, Integer status, String type, String keyword,
              LocalDateTime startTime, LocalDateTime endTime);
    int add(Tournament tournament);
    int update(Tournament tournament);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Map<String, Object> getStatistics();
    int updateCurrentParticipants(Long id, Integer currentParticipants);

    /**
     * 定时任务：按当前时间将「报名中」→「进行中」、「进行中」→「已结束」（仅时间已到的赛事）
     */
    void autoUpdateTournamentStatusByTime();
}
