package com.badminton.bmp.modules.agent.support;

import com.badminton.bmp.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Agent 写动作幂等基础设施（第四防线：幂等处理）。
 *
 * <p>基于 Redis 原子 {@code setIfAbsent} 实现：首次以 {@code Idempotency-Key} 抢占
 * {@link AgentActionStatus#PENDING} 记录并放行执行；重复请求读取既有记录，从而在
 * 确认/拒绝动作上做到"只执行一次并可回放首次结果"。数据库唯一约束作为最终兜底。</p>
 *
 * <p>P1 阶段仅提供并测试该基础设施，写 Tool 在 P2 智能预订 Agent 中接入。</p>
 */
@Component
public class AgentActionIdempotencyService {

    private static final String KEY_PREFIX = "agent:idem:";
    private static final long DEFAULT_TTL_SECONDS = 3600L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AgentActionIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 尝试开始一次幂等操作。
     *
     * @param idempotencyKey 幂等键，不能为空
     * @return 首次抢占成功返回空（调用方应执行业务并随后调用
     *         {@link #complete}）；键已存在则返回既有记录（重复请求）
     */
    public Optional<Record> begin(String idempotencyKey) {
        String redisKey = redisKey(idempotencyKey);
        String pending = write(new Record(AgentActionStatus.PENDING, null));
        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, pending, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(created)) {
            return Optional.empty();
        }
        String existing = redisTemplate.opsForValue().get(redisKey);
        if (existing == null) {
            // 记录在读取前恰好过期：视为可重新开始。
            return Optional.empty();
        }
        return Optional.of(read(existing));
    }

    /**
     * 将 {@link AgentActionStatus#PENDING} 记录一次性终结为确认或拒绝。
     *
     * @param idempotencyKey 幂等键
     * @param finalStatus    终态，仅允许 {@code CONFIRMED} 或 {@code REJECTED}
     * @param resultJson     首次执行结果（可为空），用于后续重复请求回放
     * @throws BusinessException 记录不存在（410）或已终结（409）
     */
    public void complete(String idempotencyKey, AgentActionStatus finalStatus, String resultJson) {
        if (finalStatus != AgentActionStatus.CONFIRMED && finalStatus != AgentActionStatus.REJECTED) {
            throw new IllegalArgumentException("终态只能是 CONFIRMED 或 REJECTED");
        }
        String redisKey = redisKey(idempotencyKey);
        String existing = redisTemplate.opsForValue().get(redisKey);
        if (existing == null) {
            throw new BusinessException(410, "操作已过期，请重新发起");
        }
        Record current = read(existing);
        if (current.status() != AgentActionStatus.PENDING) {
            // 已经流转到终态，禁止重复流转。
            throw new BusinessException(409, "该操作已处理，请勿重复提交");
        }
        redisTemplate.opsForValue()
                .set(redisKey, write(new Record(finalStatus, resultJson)), DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String redisKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(400, "缺少幂等键");
        }
        return KEY_PREFIX + idempotencyKey;
    }

    private String write(Record record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "幂等记录序列化失败");
        }
    }

    private Record read(String json) {
        try {
            return objectMapper.readValue(json, Record.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "幂等记录解析失败");
        }
    }

    /** 幂等记录：当前状态与首次执行结果。 */
    public record Record(AgentActionStatus status, String resultJson) {
    }
}
