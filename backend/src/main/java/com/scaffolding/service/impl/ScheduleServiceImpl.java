package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatPolicy;
import com.scaffolding.entity.Schedule;
import com.scaffolding.entity.SupplyDistribution;
import com.scaffolding.mapper.ScheduleMapper;
import com.scaffolding.service.HeatPolicyService;
import com.scaffolding.service.ScheduleService;
import com.scaffolding.service.SupplyDistributionService;
import com.scaffolding.vo.ScheduleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排班服务实现类
 *
 * @author scaffolding
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private HeatPolicyService heatPolicyService;

    @Autowired
    private SupplyDistributionService supplyDistributionService;

    @Override
    public Page<ScheduleVO> pageQuery(Long current, Long size, String workerName, String workEnv,
                                      String workDateStart, String workDateEnd, Boolean heatOnly) {
        Page<Schedule> page = new Page<>(current, size);
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(workerName)) {
            wrapper.like(Schedule::getWorkerName, workerName);
        }
        if (StringUtils.hasText(workEnv)) {
            wrapper.eq(Schedule::getWorkEnv, workEnv);
        }
        if (StringUtils.hasText(workDateStart)) {
            wrapper.ge(Schedule::getWorkDate, workDateStart);
        }
        if (StringUtils.hasText(workDateEnd)) {
            wrapper.le(Schedule::getWorkDate, workDateEnd);
        }
        wrapper.orderByDesc(Schedule::getWorkDate).orderByDesc(Schedule::getId);
        this.page(page, wrapper);

        // 转换为VO并填充高温提示
        HeatPolicy policy = heatPolicyService.getActivePolicy();
        List<Schedule> records = page.getRecords();

        // 批量查询关联的物资发放记录
        List<Long> scheduleIds = records.stream().map(Schedule::getId).collect(Collectors.toList());
        Map<Long, String> supplyStatusMap = new HashMap<>();
        if (!scheduleIds.isEmpty()) {
            supplyStatusMap = supplyDistributionService.list(new LambdaQueryWrapper<SupplyDistribution>()
                            .in(SupplyDistribution::getScheduleId, scheduleIds))
                    .stream()
                    .collect(Collectors.toMap(SupplyDistribution::getScheduleId,
                            SupplyDistribution::getStatus, (a, b) -> a));
        }

        List<ScheduleVO> voList = records.stream()
                .map(s -> toVO(s, policy, supplyStatusMap.get(s.getId())))
                .filter(vo -> heatOnly == null || !heatOnly || Boolean.TRUE.equals(vo.getHeatAlert()))
                .collect(Collectors.toList());

        Page<ScheduleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithHeatSupport(Schedule schedule) {
        this.save(schedule);
        syncSupplyDistribution(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithHeatSupport(Schedule schedule) {
        this.updateById(schedule);
        // 先清理该排班未发放的物资记录，再按最新情况重建
        supplyDistributionService.remove(new LambdaQueryWrapper<SupplyDistribution>()
                .eq(SupplyDistribution::getScheduleId, schedule.getId())
                .eq(SupplyDistribution::getStatus, "pending"));
        syncSupplyDistribution(schedule);
    }

    @Override
    public boolean isHeatAlert(Schedule schedule) {
        HeatPolicy policy = heatPolicyService.getActivePolicy();
        return isHeatAlert(schedule, policy);
    }

    /**
     * 高温判定：启用策略 + 室外岗位 + 气温达到阈值
     */
    private boolean isHeatAlert(Schedule schedule, HeatPolicy policy) {
        return policy != null
                && "outdoor".equals(schedule.getWorkEnv())
                && schedule.getTemperature() != null
                && schedule.getTemperature().compareTo(policy.getTempThreshold()) >= 0;
    }

    /**
     * 触发高温保障时，按策略自动生成待发放物资记录（幂等）
     */
    private void syncSupplyDistribution(Schedule schedule) {
        HeatPolicy policy = heatPolicyService.getActivePolicy();
        if (!isHeatAlert(schedule, policy)) {
            return;
        }
        long count = supplyDistributionService.count(new LambdaQueryWrapper<SupplyDistribution>()
                .eq(SupplyDistribution::getScheduleId, schedule.getId()));
        if (count > 0) {
            return;
        }
        SupplyDistribution distribution = new SupplyDistribution();
        distribution.setWorkerName(schedule.getWorkerName());
        distribution.setScheduleId(schedule.getId());
        distribution.setDistributeDate(schedule.getWorkDate() != null ? schedule.getWorkDate() : LocalDate.now());
        distribution.setSaltPillQty(policy.getSaltPillQty());
        distribution.setIceSleeveQty(policy.getIceSleeveQty());
        distribution.setWaterVoucherQty(policy.getWaterVoucherQty());
        distribution.setStatus("pending");
        supplyDistributionService.save(distribution);
    }

    /**
     * 组装VO：填充高温津贴、休息频次、物资提示
     */
    private ScheduleVO toVO(Schedule schedule, HeatPolicy policy, String supplyStatus) {
        ScheduleVO vo = new ScheduleVO();
        BeanUtils.copyProperties(schedule, vo);
        boolean alert = isHeatAlert(schedule, policy);
        vo.setHeatAlert(alert);
        if (alert) {
            vo.setAllowance(policy.getAllowancePerDay());
            vo.setRestFrequency(policy.getRestFrequency());
            vo.setSuppliesTip(String.format("盐丸%d粒、冰袖%d副、饮水券%d张",
                    policy.getSaltPillQty(), policy.getIceSleeveQty(), policy.getWaterVoucherQty()));
        }
        vo.setSupplyStatus(supplyStatus);
        return vo;
    }
}
