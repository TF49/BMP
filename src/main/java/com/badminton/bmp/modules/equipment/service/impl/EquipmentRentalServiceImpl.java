package com.badminton.bmp.modules.equipment.service.impl;

import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.entity.EquipmentRental;
import com.badminton.bmp.modules.equipment.mapper.EquipmentMapper;
import com.badminton.bmp.modules.equipment.mapper.EquipmentRentalMapper;
import com.badminton.bmp.modules.equipment.service.EquipmentRentalService;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badminton.bmp.common.util.SecurityUtils;

/**
 * 器材租借业务实现类
 */
@Service
public class EquipmentRentalServiceImpl implements EquipmentRentalService {

    @Autowired
    private EquipmentRentalMapper equipmentRentalMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    @Override
    public EquipmentRental findById(Long id) {
        EquipmentRental rental = equipmentRentalMapper.findById(id);
        if (rental == null) {
            return null;
        }

        // 数据范围过滤
        if (SecurityUtils.isPresident()) {
            return rental;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            // 优先使用租借记录中的 venueId（若有），否则通过器材判断
            if (rental.getVenueId() != null) {
                if (vId != null && vId.equals(rental.getVenueId())) {
                    return rental;
                }
                throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
            } else {
                Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
                if (equipment != null && vId != null && vId.equals(equipment.getVenueId())) {
                    return rental;
                }
                throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
            }
        } else {
            // 普通用户：只能查看自己的租借记录（通过 memberId -> 当前用户）
            com.badminton.bmp.modules.system.entity.User current = com.badminton.bmp.common.util.SecurityUtils.getCurrentUser();
            if (current != null) {
                com.badminton.bmp.modules.member.entity.Member member = memberMapper.findByUserId(current.getId());
                if (member != null && member.getId() != null && member.getId().equals(rental.getMemberId())) {
                    return rental;
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public List<EquipmentRental> findAll(Long memberId, Long equipmentId, Integer status, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅能查看自己的租赁记录，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) memberFilter = m.getId();
                else memberFilter = null;
            } else {
                memberFilter = null;
            }
        }
        return equipmentRentalMapper.findAll(venueFilter, memberFilter, equipmentId, status, keyword, offset, size);
    }

    @Override
    public int count(Long memberId, Long equipmentId, Integer status, String keyword) {
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅统计自己的租赁记录，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) memberFilter = m.getId();
                else memberFilter = null;
            } else {
                memberFilter = null;
            }
        }
        return equipmentRentalMapper.count(venueFilter, memberFilter, equipmentId, status, keyword);
    }

    @Override
    @Transactional
    public int add(EquipmentRental rental) {
        // 验证会员是否存在
        Member member = memberMapper.findById(rental.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 验证器材是否存在
        Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("器材不存在");
        }
        // 仅正常状态（1）可租借；停用（0）、维护中（2）不可租借
        if (equipment.getStatus() == null || equipment.getStatus() != 1) {
            throw new RuntimeException("该器材暂不可租借（停用或维护中）");
        }

        // 数据范围：场馆管理者只能操作自己场馆的器材租借（通过器材venueId判断）
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new RuntimeException("当前账号未绑定场馆，无法新增租借记录");
            }
            if (equipment.getVenueId() == null || !vId.equals(equipment.getVenueId())) {
                throw new RuntimeException("权限不足：只能操作自己场馆的器材租借");
            }
        } else if (!SecurityUtils.isPresident() && !SecurityUtils.getCurrentUserRoles().contains("USER")) {
            throw new RuntimeException("权限不足");
        }

        // 仅当最终为租借中(1)时才占库存，故仅此时校验可用数量
        int effectiveStatus = rental.getStatus() != null ? rental.getStatus() : 1;
        if (effectiveStatus == 1) {
            int avail = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
            int qty = rental.getQuantity() != null ? rental.getQuantity() : 0;
            if (qty > avail) {
                throw new RuntimeException("租借数量超过可用数量");
            }
        }

        // 生成租借单号
        if (rental.getRentalNo() == null || rental.getRentalNo().trim().isEmpty()) {
            rental.setRentalNo(generateRentalNo());
        }

        // 验证租借单号是否已存在
        if (equipmentRentalMapper.existsByRentalNo(rental.getRentalNo(), null)) {
            throw new RuntimeException("租借单号已存在");
        }

        // 根据器材价格与数量计算金额、押金与计费小时数（如前端未指定或为0）
        if (rental.getQuantity() == null || rental.getQuantity() <= 0) {
            throw new RuntimeException("租借数量必须大于0");
        }

        // 目前 rentalDate 是 LocalDate，仅记录日期；计费小时数暂按 1 小时起步 * 租期天数
        // 如后续将 rentalDate 升级为 LocalDateTime，可精细到按起止时间计算。
        int durationHours = 1;
        if (rental.getRentalDate() != null) {
            durationHours = 1;
        }

        // 单价快照与押金
        if (rental.getUnitPrice() == null || rental.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            if (equipment.getRentalPrice() != null) {
                rental.setUnitPrice(equipment.getRentalPrice());
            }
        }
        if (rental.getDepositAmount() == null || rental.getDepositAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            if (equipment.getRentalDeposit() != null) {
                rental.setDepositAmount(equipment.getRentalDeposit().multiply(java.math.BigDecimal.valueOf(rental.getQuantity())));
            } else {
                rental.setDepositAmount(java.math.BigDecimal.ZERO);
            }
        }

        // 若金额未指定或为0，则按 单价 × 数量 × 小时数 计算
        if (rental.getRentalAmount() == null || rental.getRentalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            if (rental.getUnitPrice() == null) {
                throw new RuntimeException("当前器材未配置租借价格，无法自动计算租借金额");
            }
            java.math.BigDecimal hours = java.math.BigDecimal.valueOf(durationHours);
            java.math.BigDecimal qty = java.math.BigDecimal.valueOf(rental.getQuantity());
            rental.setRentalAmount(rental.getUnitPrice().multiply(hours).multiply(qty));
        }

        rental.setDurationHours(durationHours);

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        rental.setCreateTime(now);
        rental.setUpdateTime(now);

        // 设置预计归还日期（默认为租借日期+7天）
        if (rental.getExpectedReturnDate() == null && rental.getRentalDate() != null) {
            rental.setExpectedReturnDate(rental.getRentalDate().plusDays(7));
        }

        // 设置默认状态为租借中（1）
        if (rental.getStatus() == null) {
            rental.setStatus(1);
        }

        // 新增订单强制为未支付（0），仅点击支付按钮后才扣款
        rental.setPaymentStatus(0);

        // 插入租借记录
        int result = equipmentRentalMapper.insert(rental);

        // 更新器材可用数量（仅当状态为租借中时扣减，与 update/updateStatus 语义一致）
        if (result > 0 && rental.getStatus() != null && rental.getStatus() == 1) {
            int avail = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
            int qty = rental.getQuantity() != null ? rental.getQuantity() : 0;
            if (qty > 0) {
                equipmentService.updateAvailableQuantity(rental.getEquipmentId(), avail - qty);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public int update(EquipmentRental rental) {
        // 查询原有租借记录
        EquipmentRental existingRental = equipmentRentalMapper.findById(rental.getId());
        if (existingRental == null) {
            throw new RuntimeException("租借记录不存在");
        }

        // 数据范围：场馆管理者只能更新自己场馆的租借记录（通过原记录equipment的venueId判断）
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new RuntimeException("当前账号未绑定场馆，无法更新租借记录");
            }
            Equipment oldEquipmentForScope = equipmentMapper.findById(existingRental.getEquipmentId());
            if (oldEquipmentForScope == null || oldEquipmentForScope.getVenueId() == null || !vId.equals(oldEquipmentForScope.getVenueId())) {
                throw new RuntimeException("权限不足：只能操作自己场馆的器材租借");
            }
        } else if (!SecurityUtils.isPresident() && !SecurityUtils.getCurrentUserRoles().contains("USER")) {
            throw new RuntimeException("权限不足");
        }

        // 本次有效的新状态（用于器材/数量块：仅当“新状态为租借中”时才从新器材扣减）
        Integer effectiveNewStatus = rental.getStatus() != null ? rental.getStatus() : existingRental.getStatus();
        boolean newStatusRenting = effectiveNewStatus != null && effectiveNewStatus == 1;

        // 如果修改了器材或数量，需要更新可用数量
        if (rental.getEquipmentId() != null || rental.getQuantity() != null) {
            Equipment equipment = equipmentMapper.findById(
                    rental.getEquipmentId() != null ? rental.getEquipmentId() : existingRental.getEquipmentId());
            if (equipment == null) {
                throw new RuntimeException("器材不存在");
            }

            // 若场馆管理者修改了equipmentId，仍需确保新器材属于自己场馆
            if (SecurityUtils.isVenueManager()) {
                Long vId = SecurityUtils.getCurrentUserVenueId();
                if (vId == null) {
                    throw new RuntimeException("当前账号未绑定场馆，无法更新租借记录");
                }
                if (equipment.getVenueId() == null || !vId.equals(equipment.getVenueId())) {
                    throw new RuntimeException("权限不足：只能操作自己场馆的器材租借");
                }
            }

            // 如果修改了器材，仅当原状态为租借中时恢复原器材，仅当新状态为租借中时从新器材扣减
            if (rental.getEquipmentId() != null && !rental.getEquipmentId().equals(existingRental.getEquipmentId())) {
                Integer oldQty = existingRental.getQuantity();
                int oldQtyVal = oldQty != null ? oldQty : 0;
                if (existingRental.getStatus() != null && existingRental.getStatus() == 1 && oldQtyVal > 0) {
                    Equipment oldEquipment = equipmentMapper.findById(existingRental.getEquipmentId());
                    if (oldEquipment != null) {
                        int oldAvailableQuantity = (oldEquipment.getAvailableQuantity() != null ? oldEquipment.getAvailableQuantity() : 0) + oldQtyVal;
                        equipmentService.updateAvailableQuantity(existingRental.getEquipmentId(), oldAvailableQuantity);
                    }
                }
                Integer newQty = rental.getQuantity() != null ? rental.getQuantity() : existingRental.getQuantity();
                int newQtyVal = newQty != null ? newQty : 0;
                if (newStatusRenting && newQtyVal > 0) {
                    int currentNew = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                    if (currentNew < newQtyVal) {
                        throw new RuntimeException("新器材的可用数量不足");
                    }
                    int newAvailableQuantity = currentNew - newQtyVal;
                    equipmentService.updateAvailableQuantity(equipment.getId(), newAvailableQuantity);
                }
            } else if (rental.getQuantity() != null && !rental.getQuantity().equals(existingRental.getQuantity())
                    && existingRental.getStatus() != null && existingRental.getStatus() == 1 && newStatusRenting) {
                // 仅当原状态与新状态均为租借中时，调整可用数量（原占 existing 数量，现占 rental 数量）
                int oldQ = existingRental.getQuantity() != null ? existingRental.getQuantity() : 0;
                int newQ = rental.getQuantity() != null ? rental.getQuantity() : 0;
                int quantityDiff = newQ - oldQ;
                int currentAvail = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                int newAvailableQuantity = currentAvail - quantityDiff;
                if (newAvailableQuantity < 0) {
                    throw new RuntimeException("可用数量不足");
                }
                equipmentService.updateAvailableQuantity(equipment.getId(), newAvailableQuantity);
            }
        }

        // 保留原有值（如果未传入）
        if (rental.getRentalNo() == null) {
            rental.setRentalNo(existingRental.getRentalNo());
        }
        if (rental.getMemberId() == null) {
            rental.setMemberId(existingRental.getMemberId());
        }
        if (rental.getEquipmentId() == null) {
            rental.setEquipmentId(existingRental.getEquipmentId());
        }
        if (rental.getQuantity() == null) {
            rental.setQuantity(existingRental.getQuantity());
        }
        if (rental.getRentalDate() == null) {
            rental.setRentalDate(existingRental.getRentalDate());
        }
        if (rental.getRentalAmount() == null) {
            rental.setRentalAmount(existingRental.getRentalAmount());
        }
        if (rental.getUnitPrice() == null) {
            rental.setUnitPrice(existingRental.getUnitPrice());
        }
        if (rental.getDepositAmount() == null) {
            rental.setDepositAmount(existingRental.getDepositAmount());
        }
        if (rental.getDurationHours() == null) {
            rental.setDurationHours(existingRental.getDurationHours());
        }
        if (rental.getPaymentMethod() == null) {
            rental.setPaymentMethod(existingRental.getPaymentMethod());
        }
        if (rental.getPaymentStatus() == null) {
            rental.setPaymentStatus(existingRental.getPaymentStatus());
        }
        if (rental.getStatus() == null) {
            rental.setStatus(existingRental.getStatus());
        }
        if (rental.getRemark() == null) {
            rental.setRemark(existingRental.getRemark());
        }

        // 按状态变更同步器材可用数量（编辑改为租借中则扣减，改为归还/取消/逾期则加回）
        Integer oldStatus = existingRental.getStatus();
        Integer newStatus = rental.getStatus();
        boolean equipmentChanged = rental.getEquipmentId() != null && !rental.getEquipmentId().equals(existingRental.getEquipmentId());
        if (oldStatus != null && !oldStatus.equals(newStatus)) {
            int qty = existingRental.getQuantity() != null ? existingRental.getQuantity() : 0;
            if (qty <= 0) {
                // 数量无效时不调整库存
            } else {
                boolean oldRenting = oldStatus == 1;
                boolean newRenting = newStatus != null && newStatus == 1;
                boolean newReturnedOrCancelled = newStatus != null && (newStatus == 0 || newStatus == 2 || newStatus == 3);
                if (oldRenting && newReturnedOrCancelled) {
                    // 加回应针对“本次被扣减的器材”：若本次改了器材，器材块已恢复原器材并从新器材扣减，故应加回新器材
                    Long equipmentIdToRestore = equipmentChanged ? rental.getEquipmentId() : existingRental.getEquipmentId();
                    int qtyToRestore = equipmentChanged ? (rental.getQuantity() != null ? rental.getQuantity() : existingRental.getQuantity()) : qty;
                    if (equipmentIdToRestore != null && qtyToRestore > 0) {
                        Equipment equipmentToRestore = equipmentMapper.findById(equipmentIdToRestore);
                        if (equipmentToRestore != null) {
                            int currentAvailable = equipmentToRestore.getAvailableQuantity() != null ? equipmentToRestore.getAvailableQuantity() : 0;
                            int newAvailableQuantity = currentAvailable + qtyToRestore;
                            equipmentService.updateAvailableQuantity(equipmentIdToRestore, newAvailableQuantity);
                        }
                    }
                    if (newStatus != null && newStatus == 2 && existingRental.getReturnDate() == null) {
                        rental.setReturnDate(LocalDateTime.now());
                    }
                } else if (!oldRenting && newRenting) {
                    // 从非租借中→租借中：扣减当前记录对应的器材（保留原有值后即为 rental.getEquipmentId()）
                    Long equipmentIdToDeduct = rental.getEquipmentId() != null ? rental.getEquipmentId() : existingRental.getEquipmentId();
                    if (equipmentIdToDeduct != null) {
                        Equipment equipmentForStatus = equipmentMapper.findById(equipmentIdToDeduct);
                        if (equipmentForStatus != null) {
                            int currentAvailable = equipmentForStatus.getAvailableQuantity() != null ? equipmentForStatus.getAvailableQuantity() : 0;
                            if (currentAvailable < qty) {
                                throw new RuntimeException("器材可用数量不足");
                            }
                            int newAvailableQuantity = currentAvailable - qty;
                            equipmentService.updateAvailableQuantity(equipmentIdToDeduct, newAvailableQuantity);
                        }
                    }
                }
            }
        }

        // 设置更新时间
        rental.setUpdateTime(LocalDateTime.now());

        return equipmentRentalMapper.update(rental);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        // 查询租借记录
        EquipmentRental rental = equipmentRentalMapper.findById(id);
        if (rental == null) {
            throw new RuntimeException("租借记录不存在");
        }

        // 权限校验：VENUE_MANAGER只能删除自己场馆的器材租借记录
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new RuntimeException("当前账号未绑定场馆，无法删除租借记录");
            }
            Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
            if (equipment == null || equipment.getVenueId() == null || !vId.equals(equipment.getVenueId())) {
                throw new RuntimeException("权限不足：只能删除自己场馆的器材租借记录");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new RuntimeException("权限不足：只有会长和场馆管理员可以删除租借记录");
        }

        // 如果状态是租借中，需要恢复器材可用数量
        if (rental.getStatus() != null && rental.getStatus() == 1) {
            Integer qty = rental.getQuantity();
            if (qty != null && qty > 0) {
                Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
                if (equipment != null) {
                    int current = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                    equipmentService.updateAvailableQuantity(rental.getEquipmentId(), current + qty);
                }
            }
        }

        return equipmentRentalMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int updateStatus(Long id, Integer status) {
        // 验证租借记录是否存在
        EquipmentRental rental = equipmentRentalMapper.findById(id);
        if (rental == null) {
            throw new RuntimeException("租借记录不存在");
        }

        // 验证状态值是否有效
        if (status == null || status < 0 || status > 3) {
            throw new RuntimeException("无效的状态值，必须在0-3之间（0-已取消，1-租借中，2-已归还，3-逾期）");
        }

        // 如果状态变更，需要处理器材可用数量的变化
        Integer oldStatus = rental.getStatus();
        if (oldStatus != null && !oldStatus.equals(status)) {
            Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
            if (equipment != null) {
                boolean oldStatusRenting = oldStatus == 1; // 租借中
                boolean newStatusRenting = status == 1; // 租借中
                boolean newStatusReturned = status == 2; // 已归还
                boolean newStatusCancelled = status == 0; // 已取消

                if (oldStatusRenting && (newStatusReturned || newStatusCancelled)) {
                    // 从"租借中"变为"已归还"或"已取消"，恢复器材可用数量
                    int qty = rental.getQuantity() != null ? rental.getQuantity() : 0;
                    if (qty > 0) {
                        int current = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                        equipmentService.updateAvailableQuantity(rental.getEquipmentId(), current + qty);
                    }
                    
                    // 如果状态变更为已归还，更新归还时间（使用完整 rental 避免 update 把 member_id 等置为 null）
                    if (newStatusReturned && rental.getReturnDate() == null) {
                        rental.setReturnDate(LocalDateTime.now());
                        rental.setUpdateTime(LocalDateTime.now());
                        equipmentRentalMapper.update(rental);
                    }
                } else if (!oldStatusRenting && newStatusRenting) {
                    // 从其他状态变为"租借中"，扣减器材可用数量
                    int qty = rental.getQuantity() != null ? rental.getQuantity() : 0;
                    if (qty > 0) {
                        int current = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                        if (current < qty) {
                            throw new RuntimeException("器材可用数量不足");
                        }
                        equipmentService.updateAvailableQuantity(rental.getEquipmentId(), current - qty);
                    }
                } else if (newStatusReturned && rental.getReturnDate() == null) {
                    // 如果状态变更为已归还（且之前不是租借中），更新归还时间（使用完整 rental 避免 member_id 置空）
                    rental.setReturnDate(LocalDateTime.now());
                    rental.setUpdateTime(LocalDateTime.now());
                    equipmentRentalMapper.update(rental);
                }
            }
        } else if (status == 2 && rental.getReturnDate() == null) {
            // 如果状态已经是已归还但归还时间为空，更新归还时间（使用完整 rental 避免 member_id 置空）
            rental.setReturnDate(LocalDateTime.now());
            rental.setUpdateTime(LocalDateTime.now());
            equipmentRentalMapper.update(rental);
        }

        int result = equipmentRentalMapper.updateStatus(id, status);
        try {
            Long userId = null;
            Member m = memberMapper.findById(rental.getMemberId());
            if (m != null && m.getUserId() != null) userId = m.getUserId();
            String statusText = equipmentRentalStatusText(status);
            webSocketPushService.pushOrderStatusToUser(userId, "equipmentRental", id, status, statusText, "器材租借");
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(EquipmentRentalServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    private static String equipmentRentalStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "租借中";
            case 2: return "已归还";
            case 3: return "逾期";
            default: return "未知";
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        // 获取总租借记录数
        int total = equipmentRentalMapper.countAll();
        statistics.put("total", total);

        // 获取各状态的租借记录数量
        List<Map<String, Object>> statusCounts = equipmentRentalMapper.countByStatus();

        // 初始化各状态数量
        int cancelled = 0;  // 已取消（0）
        int renting = 0;   // 租借中（1）
        int returned = 0;  // 已归还（2）
        int overdue = 0;   // 逾期（3）

        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");

            if (statusObj != null && countObj != null) {
                int statusValue = ((Number) statusObj).intValue();
                int countValue = ((Number) countObj).intValue();

                switch (statusValue) {
                    case 0:
                        cancelled = countValue;
                        break;
                    case 1:
                        renting = countValue;
                        break;
                    case 2:
                        returned = countValue;
                        break;
                    case 3:
                        overdue = countValue;
                        break;
                }
            }
        }

        statistics.put("cancelled", cancelled);
        statistics.put("renting", renting);
        statistics.put("returned", returned);
        statistics.put("overdue", overdue);

        // 按器材统计租借占比（Dashboard）
        List<Map<String, Object>> categoryRows = equipmentRentalMapper.sumQuantityByEquipment(venueFilter, 6);
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        long sum = 0;
        if (categoryRows != null) {
            for (Map<String, Object> r : categoryRows) {
                Object cntObj = r.get("cnt");
                if (cntObj == null) cntObj = r.get("CNT");
                if (cntObj instanceof Number) sum += ((Number) cntObj).longValue();
            }
            for (Map<String, Object> r : categoryRows) {
                String name = r.get("name") != null ? r.get("name").toString() : "器材";
                Object cntObj = r.get("cnt");
                if (cntObj == null) cntObj = r.get("CNT");
                long cnt = (cntObj instanceof Number) ? ((Number) cntObj).longValue() : 0;
                double percentage = sum > 0 ? ((double) cnt / sum) * 100.0 : 0;
                Map<String, Object> item = new HashMap<>();
                item.put("name", name);
                item.put("count", cnt);
                item.put("percentage", percentage);
                categoryStats.add(item);
            }
        }
        statistics.put("categoryStats", categoryStats);

        return statistics;
    }

    @Override
    public synchronized String generateRentalNo() {
        // 生成格式：ER+日期+序号，如 ER20250118001
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ER" + dateStr;

        // 直接根据前缀查询数据库中当前最大的单号（包含已逻辑删除的数据），
        // 确保整个系统生命周期内 rental_no 全局唯一，避免与唯一索引冲突。
        String maxRentalNo = equipmentRentalMapper.findMaxRentalNoByPrefix(prefix);

        int newSequence = 1;
        if (maxRentalNo != null && maxRentalNo.startsWith(prefix)) {
            try {
                String sequenceStr = maxRentalNo.substring(prefix.length());
                int maxSequence = Integer.parseInt(sequenceStr);
                newSequence = maxSequence + 1;
            } catch (NumberFormatException ignored) {
                // 忽略无效历史数据，继续使用默认序号1
            }
        }

        return prefix + String.format("%03d", newSequence);
    }

    @Override
    public List<EquipmentRental> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return equipmentRentalMapper.findByDateRange(startDate, endDate);
    }

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param rentalId 租借ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long rentalId, String paymentMethod) {
        if (!"BALANCE".equals(paymentMethod)) {
            throw new RuntimeException("业务订单仅支持余额支付");
        }

        // 查询租借记录
        EquipmentRental rental = equipmentRentalMapper.findById(rentalId);
        if (rental == null) {
            throw new RuntimeException("租借记录不存在");
        }

        // 权限兜底：管理员可处理任意订单；普通用户仅可为自己的订单支付
        Equipment equipmentForAuth = equipmentMapper.findById(rental.getEquipmentId());
        Long venueIdForAuth = equipmentForAuth != null ? equipmentForAuth.getVenueId() : null;
        if (SecurityUtils.isPresident()) {
            // 会长：不限制
        } else if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId == null || venueIdForAuth == null || !currentVenueId.equals(venueIdForAuth)) {
                throw new RuntimeException("权限不足，只能处理自己场馆的器材租借支付");
            }
        } else {
            // 普通用户：仅能为自己（当前登录用户关联的会员）的订单支付
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null) {
                throw new RuntimeException("请先登录");
            }
            Member currentMember = memberMapper.findByUserId(current.getId());
            if (currentMember == null || !currentMember.getId().equals(rental.getMemberId())) {
                throw new RuntimeException("权限不足，只能为自己的租借订单支付");
            }
        }

        // 验证租借支付状态，按状态返回明确提示
        Integer payStatus = rental.getPaymentStatus();
        if (payStatus != null && payStatus != 0) {
            String msg = (payStatus != null && payStatus == 1)
                ? "该租借已支付，无需重复支付"
                : "该租借已退款，无法再次支付";
            throw new RuntimeException(msg);
        }

        memberConsumeRecordService.createConsumeRecord(
            rental.getMemberId(),
            rental.getRentalAmount(),
            "EQUIPMENT",
            rentalId,
            paymentMethod,
            "器材租借支付：" + rental.getRentalNo()
        );

        // 获取场馆ID用于财务记录
        Equipment equipmentForFinance = equipmentMapper.findById(rental.getEquipmentId());
        Long venueIdForFinance = equipmentForFinance != null ? equipmentForFinance.getVenueId() : null;

        // 创建财务记录（收入）
        financeService.createFromBusiness(
            Finance.TYPE_EQUIPMENT,
            rentalId,
            rental.getRentalAmount(),
            Finance.INCOME,
            paymentMethod,
            venueIdForFinance,
            "器材租借支付：" + rental.getRentalNo()
        );

        // 更新租借支付状态
        rental.setPaymentMethod(paymentMethod);
        rental.setPaymentStatus(1); // 已支付
        rental.setUpdateTime(LocalDateTime.now());
        return equipmentRentalMapper.update(rental);
    }

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param rentalId 租借ID
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long rentalId) {
        // 查询租借记录
        EquipmentRental rental = equipmentRentalMapper.findById(rentalId);
        if (rental == null) {
            throw new RuntimeException("租借记录不存在");
        }

        // 权限兜底：只有管理员，且场馆管理者仅限自己场馆
        Equipment equipmentForAuth = equipmentMapper.findById(rental.getEquipmentId());
        Long venueIdForAuth = equipmentForAuth != null ? equipmentForAuth.getVenueId() : null;
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || venueIdForAuth == null || !currentVenueId.equals(venueIdForAuth)) {
                    throw new RuntimeException("权限不足，只能处理自己场馆的器材租借退款");
                }
            } else {
                throw new RuntimeException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证租借支付状态，按状态返回明确提示
        Integer payStatus = rental.getPaymentStatus();
        if (payStatus == null || payStatus != 1) {
            String msg;
            if (payStatus == null || payStatus == 0) {
                msg = "该租借尚未支付，无法退款";
            } else if (payStatus == 2) {
                msg = "该租借已退款，无需重复退款";
            } else {
                msg = "只能对已支付的租借进行退款";
            }
            throw new RuntimeException(msg);
        }

        // 调用退款记录服务（只有余额支付才需要）
        if ("BALANCE".equals(rental.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                rental.getMemberId(),
                rental.getRentalAmount(),
                "EQUIPMENT",
                rentalId,
                rental.getPaymentMethod(),
                "器材租借退款：" + rental.getRentalNo()
            );
        }

        // 获取场馆ID用于财务记录
        Equipment equipmentForRefund = equipmentMapper.findById(rental.getEquipmentId());
        Long venueIdForRefund = equipmentForRefund != null ? equipmentForRefund.getVenueId() : null;

        // 创建财务记录（支出/退款）
        financeService.createFromBusiness(
            Finance.TYPE_EQUIPMENT,
            rentalId,
            rental.getRentalAmount(),
            Finance.EXPENSE,
            rental.getPaymentMethod(),
            venueIdForRefund,
            "器材租借退款：" + rental.getRentalNo()
        );

        // 更新租借状态
        Integer oldStatus = rental.getStatus();
        rental.setPaymentStatus(2); // 已退款
        rental.setStatus(0); // 已取消
        rental.setUpdateTime(LocalDateTime.now());
        int result = equipmentRentalMapper.update(rental);

        // 恢复器材可用数量（只有原状态是租借中时才需要恢复，已归还的不需要重复恢复）
        if (oldStatus != null && oldStatus == 1) {
            int qty = rental.getQuantity() != null ? rental.getQuantity() : 0;
            if (qty > 0) {
                Equipment equipment = equipmentMapper.findById(rental.getEquipmentId());
                if (equipment != null) {
                    int current = equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0;
                    equipmentService.updateAvailableQuantity(rental.getEquipmentId(), current + qty);
                }
            }
        }
        if (result > 0) {
            try {
                Long userId = null;
                Member m = memberMapper.findById(rental.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(userId, "equipmentRental", rentalId, 0, "已取消", "器材租借");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(EquipmentRentalServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoMarkOverdueRentals() {
        List<Long> overdueIds = equipmentRentalMapper.findOverdueRentalIds();
        if (overdueIds != null) {
            for (Long id : overdueIds) {
                equipmentRentalMapper.updateStatus(id, 3); // 逾期
            }
            try {
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(EquipmentRentalServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
    }
}
