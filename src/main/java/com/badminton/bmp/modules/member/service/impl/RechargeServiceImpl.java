package com.badminton.bmp.modules.member.service.impl;

import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.entity.RechargeRecord;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.mapper.RechargeRecordMapper;
import com.badminton.bmp.modules.member.service.RechargeService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 充值服务实现类
 * 包含充值逻辑和自动升级逻辑
 */
@Service
public class RechargeServiceImpl implements RechargeService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;
    
    @Autowired
    private com.badminton.bmp.modules.system.mapper.UserMapper userMapper;

    @Autowired
    private FinanceService financeService;

    /**
     * 升级阈值：单次充值金额达到200元时自动升级为会员
     */
    private static final BigDecimal UPGRADE_THRESHOLD = new BigDecimal("200");

    /**
     * 会员有效期：1年
     */
    private static final int MEMBER_VALIDITY_YEARS = 1;

    /**
     * 累计充值等级阈值（只升不降）：Lv1=0, Lv2=300, Lv3=500, Lv4=800, Lv5=1000
     */
    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("300");
    private static final BigDecimal LEVEL3_THRESHOLD = new BigDecimal("500");
    private static final BigDecimal LEVEL4_THRESHOLD = new BigDecimal("800");
    private static final BigDecimal LEVEL5_THRESHOLD = new BigDecimal("1000");

    /**
     * 日期格式化器：用于生成充值单号
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成充值单号（线程安全）
     * 格式：RC+日期(yyyyMMdd)+3位序号
     * 例如：RC20250202001
     * @return 充值单号
     */
    private synchronized String generateRechargeNo() {
        // 1. 获取当前日期，格式化为yyyyMMdd
        String datePrefix = LocalDateTime.now().format(DATE_FORMATTER);

        // 2. 查询当天已有的充值记录数量
        int count = rechargeRecordMapper.countByDatePrefix(datePrefix);

        // 3. 生成新的序号（数量+1），格式化为3位数字
        int sequence = count + 1;
        String sequenceStr = String.format("%03d", sequence);

        // 4. 拼接成完整的充值单号
        return "RC" + datePrefix + sequenceStr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeRecord userRecharge(Long userId, BigDecimal amount, String method) {
        // 1. 根据用户ID查找会员记录
        Member member = memberMapper.findByUserId(userId);
        if (member == null) {
            // 如果用户没有会员记录，尝试自动创建一个
            try {
                member = createMemberForUser(userId);
            } catch (RuntimeException e) {
                // 创建会员记录失败，抛出更友好的错误信息
                throw new RuntimeException("充值失败：无法创建会员记录。原因：" + e.getMessage() + 
                    "。请确保用户信息完整，或联系管理员处理。", e);
            }
            if (member == null) {
                throw new RuntimeException("充值失败：创建会员记录失败，请稍后重试或联系管理员");
            }
        }

        // 2. 调用通用充值方法
        return doRecharge(member.getId(), amount, method, "USER", userId, null, null);
    }

    /**
     * 为用户创建会员记录
     * @param userId 用户ID
     * @return 创建的会员记录
     * @throws RuntimeException 如果创建失败，抛出包含详细错误信息的异常
     */
    private Member createMemberForUser(Long userId) {
        try {
            // 获取用户信息
            com.badminton.bmp.modules.system.entity.User user = userMapper.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在（用户ID: " + userId + "），无法创建会员记录");
            }

            // 验证用户信息完整性（手机号虽然不是必填，但建议有）
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                throw new RuntimeException("用户信息不完整：用户名为空，无法创建会员记录");
            }

            // 创建会员记录
            Member member = new Member();
            member.setUserId(userId);
            member.setMemberName(user.getUsername());
            member.setPhone(user.getPhone()); // 手机号可以为空
            member.setMemberType("NORMAL"); // 默认为普通用户
            member.setMemberLevel(0); // 默认等级为0
            member.setStatus(1); // 状态：正常
            member.setBalance(BigDecimal.ZERO); // 初始余额为0
            member.setTotalConsumption(BigDecimal.ZERO); // 累计消费为0
            member.setTotalRecharge(BigDecimal.ZERO); // 累计充值为0
            member.setCreateTime(java.time.LocalDateTime.now());
            member.setUpdateTime(java.time.LocalDateTime.now());

            // 插入数据库
            int result = memberMapper.insert(member);
            if (result <= 0) {
                throw new RuntimeException("数据库插入失败：会员记录创建失败");
            }

            return member;
        } catch (RuntimeException e) {
            // 重新抛出RuntimeException，保留详细错误信息
            throw e;
        } catch (Exception e) {
            // 捕获其他异常，包装成RuntimeException并包含详细错误信息
            String errorMsg = "创建会员记录时发生异常：" + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMsg += " - " + e.getMessage();
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeRecord adminRecharge(Long memberId, BigDecimal amount, String method, Long operatorId, String remark) {
        // 1. 验证会员是否存在
        Member member = memberMapper.findById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 2. 调用通用充值方法
        return doRecharge(memberId, amount, method, "ADMIN", null, operatorId, remark);
    }

    /**
     * 通用充值方法（带重试机制）
     * @param memberId 会员ID
     * @param amount 充值金额
     * @param method 充值方式
     * @param operatorType 操作类型（USER/ADMIN）
     * @param userId 用户ID（用户自己充值时使用）
     * @param operatorId 操作人ID（管理员充值时使用）
     * @param remark 备注
     * @return 充值记录对象
     */
    private RechargeRecord doRecharge(Long memberId, BigDecimal amount, String method,
                                      String operatorType, Long userId, Long operatorId, String remark) {
        // 1. 验证充值金额
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        // 2. 重试机制：最多重试3次
        int maxRetries = 3;
        int retryCount = 0;
        Member member = null;
        Integer version = null;
        BigDecimal oldBalance = null;
        BigDecimal newBalance = null;
        int updateResult = 0;

        while (retryCount < maxRetries) {
            try {
                // #region agent log
                try {
                    FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                    fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"充值开始-重试次数\",\"data\":{\"memberId\":%d,\"amount\":%s,\"retryCount\":%d,\"maxRetries\":%d},\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n",
                        System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 190, memberId, amount, retryCount, maxRetries));
                    fw.close();
                } catch (IOException e) {}
                // #endregion

                // 获取会员信息（每次重试都重新查询，获取最新版本号）
                member = memberMapper.findById(memberId);
                if (member == null) {
                    throw new RuntimeException("会员不存在");
                }

                // #region agent log
                try {
                    FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                    fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"查询会员信息\",\"data\":{\"memberId\":%d,\"balance\":%s,\"version\":%d},\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n",
                        System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 195, memberId, member.getBalance(), member.getVersion()));
                    fw.close();
                } catch (IOException e) {}
                // #endregion

                // 计算新余额
                oldBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
                newBalance = oldBalance.add(amount);
                version = member.getVersion() != null ? member.getVersion() : 0;

                // #region agent log
                try {
                    FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                    fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"准备更新余额\",\"data\":{\"memberId\":%d,\"oldBalance\":%s,\"newBalance\":%s,\"version\":%d,\"versionFromDB\":%s},\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n",
                        System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 229, memberId, oldBalance, newBalance, version,
                        member.getVersion() != null ? member.getVersion().toString() : "null"));
                    fw.close();
                } catch (IOException e) {}
                // #endregion

                // 如果数据库中 version 是 NULL，先初始化为 0
                if (member.getVersion() == null) {
                    // #region agent log
                    try {
                        FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                        fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"初始化version为0\",\"data\":{\"memberId\":%d},\"runId\":\"run1\",\"hypothesisId\":\"G\"}\n",
                            System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 237, memberId));
                        fw.close();
                    } catch (IOException e) {}
                    // #endregion
                    // 初始化 version 为 0
                    int initResult = memberMapper.updateVersionToZero(memberId);
                    // #region agent log
                    try {
                        FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                        fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"初始化version结果\",\"data\":{\"memberId\":%d,\"initResult\":%d},\"runId\":\"run1\",\"hypothesisId\":\"H\"}\n",
                            System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 240, memberId, initResult));
                        fw.close();
                    } catch (IOException e) {}
                    // #endregion
                    // 重新查询获取最新的 version
                    member = memberMapper.findById(memberId);
                    if (member != null) {
                        version = member.getVersion() != null ? member.getVersion() : 0;
                        // #region agent log
                        try {
                            FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                            fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"重新查询后version\",\"data\":{\"memberId\":%d,\"version\":%s},\"runId\":\"run1\",\"hypothesisId\":\"I\"}\n",
                                System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 245, memberId,
                                member.getVersion() != null ? member.getVersion().toString() : "null"));
                            fw.close();
                        } catch (IOException e) {}
                        // #endregion
                    }
                }

                // 更新会员余额（使用乐观锁）
                updateResult = memberMapper.updateBalanceWithVersion(memberId, newBalance, version);

                // #region agent log
                try {
                    FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                    fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"更新余额结果\",\"data\":{\"memberId\":%d,\"updateResult\":%d,\"version\":%s,\"versionIsNull\":%s},\"runId\":\"run1\",\"hypothesisId\":\"D\"}\n",
                        System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 241, memberId, updateResult, 
                        version != null ? version.toString() : "null", (version == null ? "true" : "false")));
                    fw.close();
                } catch (IOException e) {}
                // #endregion

                // 如果更新成功，跳出循环
                if (updateResult > 0) {
                    break;
                }

                // 如果更新失败（乐观锁冲突），增加重试次数
                retryCount++;
                if (retryCount < maxRetries) {
                    // #region agent log
                    try {
                        FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                        fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"乐观锁冲突-准备重试\",\"data\":{\"memberId\":%d,\"retryCount\":%d,\"maxRetries\":%d},\"runId\":\"run1\",\"hypothesisId\":\"E\"}\n",
                            System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 210, memberId, retryCount, maxRetries));
                        fw.close();
                    } catch (IOException e) {}
                    // #endregion
                    // 短暂等待后重试（给其他操作时间完成）
                    try {
                        Thread.sleep(50 * retryCount); // 递增等待时间：50ms, 100ms, 150ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (RuntimeException e) {
                // 如果是非乐观锁冲突的异常，直接抛出
                if (!e.getMessage().contains("数据已被其他操作修改")) {
                    throw e;
                }
                // 乐观锁冲突，继续重试
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(50 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 如果重试后仍然失败，抛出异常
        if (updateResult == 0) {
            // #region agent log
            try {
                FileWriter fw = new FileWriter("debug-c7daf4.log", true);
                fw.write(String.format("{\"sessionId\":\"c7daf4\",\"id\":\"log_%d_%d\",\"timestamp\":%d,\"location\":\"RechargeServiceImpl.java:%d\",\"message\":\"充值失败-重试耗尽\",\"data\":{\"memberId\":%d,\"retryCount\":%d,\"maxRetries\":%d},\"runId\":\"run1\",\"hypothesisId\":\"F\"}\n",
                    System.currentTimeMillis(), retryCount, System.currentTimeMillis(), 240, memberId, retryCount, maxRetries));
                fw.close();
            } catch (IOException e) {}
            // #endregion
            throw new RuntimeException("充值失败，数据已被其他操作修改，请重试");
        }

        // 5. 判断是否触发升级（身份：NORMAL -> MEMBER）
        boolean isUpgraded = false;
        if (amount.compareTo(UPGRADE_THRESHOLD) >= 0 && "NORMAL".equals(member.getMemberType())) {
            upgradeToMember(memberId);
            isUpgraded = true;
            // 刚升级为会员后重新拉取，使 levelBefore 使用最新等级 1，避免与后续等级计算不一致
            member = memberMapper.findById(memberId);
            if (member == null) throw new RuntimeException("会员不存在");
        }

        // 5.1 判断是否等级升级（本笔充值是否使会员等级 Lv1~Lv5 提升）
        // 说明：若本笔前累计已>=1000（已是 Lv5），再充 1000 不会产生等级变化，等级升级=否 属正常
        int levelBefore = "NORMAL".equals(member.getMemberType())
            ? 0
            : (member.getMemberLevel() != null ? member.getMemberLevel() : 1);
        BigDecimal totalAfter = (member.getTotalRecharge() == null ? BigDecimal.ZERO : member.getTotalRecharge()).add(amount);
        int levelAfter = computeLevelFromTotalRecharge(totalAfter);
        boolean isLevelUpgraded = (levelAfter > levelBefore);

        // 6. 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setRechargeNo(generateRechargeNo()); // 生成充值单号
        record.setMemberId(memberId);
        record.setUserId(userId);
        record.setRechargeAmount(amount);
        record.setRechargeMethod(method);
        record.setRechargeTime(LocalDateTime.now());
        record.setOperatorType(operatorType);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        record.setIsUpgraded(isUpgraded ? 1 : 0);
        record.setIsLevelUpgraded(isLevelUpgraded ? 1 : 0);
        record.setCreateTime(LocalDateTime.now());
        // 推导 venueId：优先使用 operatorId 对应用户的 venueId（管理员充值场景）
        try {
            Long venueId = null;
            if (operatorId != null) {
                com.badminton.bmp.modules.system.entity.User operator = userMapper.findById(operatorId);
                if (operator != null) venueId = operator.getVenueId();
            }
            record.setVenueId(venueId);
        } catch (Exception ignored) {}

        rechargeRecordMapper.insert(record);

        // 7. 累加累计充值金额
        memberMapper.updateTotalRecharge(memberId, amount);

        // 8. 仅对会员按累计充值重算等级并只升不降
        Member memberAfter = memberMapper.findById(memberId);
        if (memberAfter != null && "MEMBER".equals(memberAfter.getMemberType())) {
            int newLevel = computeLevelFromTotalRecharge(memberAfter.getTotalRecharge());
            int currentLevel = memberAfter.getMemberLevel() != null ? memberAfter.getMemberLevel() : 1;
            if (newLevel > currentLevel) {
                memberMapper.updateMemberLevel(memberId, newLevel);
            }
        }

        // 创建财务记录（充值收入）
        financeService.createFromBusiness(
            Finance.TYPE_RECHARGE,
            record.getId(),
            amount,
            Finance.INCOME,
            method,
            record.getVenueId(),
            "会员充值：" + member.getMemberName() + (remark != null ? " - " + remark : "")
        );

        return record;
    }

    /**
     * 根据累计充值金额计算会员等级（1-5）
     * @param totalRecharge 累计充值金额
     * @return 等级 1-5
     */
    private int computeLevelFromTotalRecharge(BigDecimal totalRecharge) {
        if (totalRecharge == null || totalRecharge.compareTo(BigDecimal.ZERO) < 0) {
            return 1;
        }
        if (totalRecharge.compareTo(LEVEL5_THRESHOLD) >= 0) {
            return 5;
        }
        if (totalRecharge.compareTo(LEVEL4_THRESHOLD) >= 0) {
            return 4;
        }
        if (totalRecharge.compareTo(LEVEL3_THRESHOLD) >= 0) {
            return 3;
        }
        if (totalRecharge.compareTo(LEVEL2_THRESHOLD) >= 0) {
            return 2;
        }
        return 1;
    }

    /**
     * 将普通用户升级为会员（同步更新 sys_member 与 sys_user.role）
     * 业务含义：USER 与 MEMBER 同属“普通用户”角色，新注册为 USER，充值达标后升级为 MEMBER。
     * @param memberId 会员ID
     */
    private void upgradeToMember(Long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member == null) return;

        // 1. 更新会员表：类型=MEMBER，等级=1，到期时间=当前+1年
        String memberType = "MEMBER";
        Integer memberLevel = 1;
        LocalDateTime expireTime = LocalDateTime.now().plusYears(MEMBER_VALIDITY_YEARS);
        memberMapper.updateMemberTypeAndLevel(memberId, memberType, memberLevel, expireTime);

        // 2. 同步更新用户表角色：USER -> MEMBER，使登录后角色与会员身份一致
        Long userId = member.getUserId();
        if (userId != null) {
            userMapper.updateRole(userId, "MEMBER");
        }
    }

    @Override
    public List<RechargeRecord> getRechargeRecordsByMemberId(Long memberId, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        int offset = (page - 1) * size;
        return rechargeRecordMapper.findByMemberIdWithPage(memberId, offset, size);
    }

    @Override
    public int countRechargeRecordsByMemberId(Long memberId) {
        return rechargeRecordMapper.countByMemberId(memberId);
    }

    @Override
    public List<RechargeRecord> getAllRechargeRecords(int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        int offset = (page - 1) * size;
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        }
        return rechargeRecordMapper.findAllWithPage(venueFilter, offset, size);
    }

    @Override
    public int countAllRechargeRecords() {
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        }
        return rechargeRecordMapper.countAll(venueFilter);
    }

    @Override
    public List<RechargeRecord> getRechargeRecordsWithFilters(String memberKeyword, String startTime, String endTime, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        int offset = (page - 1) * size;
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        }
        return rechargeRecordMapper.findWithFilters(venueFilter, memberKeyword, startTime, endTime, offset, size);
    }

    @Override
    public int countRechargeRecordsWithFilters(String memberKeyword, String startTime, String endTime) {
        Long venueFilter = null;
        if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        }
        return rechargeRecordMapper.countWithFilters(venueFilter, memberKeyword, startTime, endTime);
    }
}
