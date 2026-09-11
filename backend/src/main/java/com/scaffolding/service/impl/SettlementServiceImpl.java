package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatPolicy;
import com.scaffolding.entity.Schedule;
import com.scaffolding.entity.Settlement;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.SettlementMapper;
import com.scaffolding.service.HeatPolicyService;
import com.scaffolding.service.ScheduleService;
import com.scaffolding.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 结算单服务实现类（高温津贴与普通工时分列计算）
 *
 * @author scaffolding
 */
@Service
public class SettlementServiceImpl extends ServiceImpl<SettlementMapper, Settlement> implements SettlementService {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private HeatPolicyService heatPolicyService;

    @Override
    public Page<Settlement> pageQuery(Long current, Long size, String workerName, String period, String status) {
        Page<Settlement> page = new Page<>(current, size);
        LambdaQueryWrapper<Settlement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(workerName)) {
            wrapper.like(Settlement::getWorkerName, workerName);
        }
        if (StringUtils.hasText(period)) {
            wrapper.eq(Settlement::getPeriod, period);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Settlement::getStatus, status);
        }
        wrapper.orderByDesc(Settlement::getPeriod).orderByDesc(Settlement::getId);
        return this.page(page, wrapper);
    }

    @Override
    public Settlement generate(String workerName, String period) {
        Settlement calculated = calculate(workerName, period);
        // 幂等：同工人同周期已存在则更新（已结算的不允许覆盖）
        Settlement existing = this.getOne(new LambdaQueryWrapper<Settlement>()
                .eq(Settlement::getWorkerName, workerName)
                .eq(Settlement::getPeriod, period));
        if (existing != null) {
            if ("settled".equals(existing.getStatus())) {
                throw new BusinessException("该周期已结算，不可重新生成");
            }
            calculated.setId(existing.getId());
            this.updateById(calculated);
            return calculated;
        }
        calculated.setStatus("pending");
        this.save(calculated);
        return calculated;
    }

    @Override
    public Settlement preview(String workerName, String period) {
        return calculate(workerName, period);
    }

    @Override
    public void settle(Long id) {
        Settlement settlement = this.getById(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在");
        }
        if ("settled".equals(settlement.getStatus())) {
            throw new BusinessException("该结算单已结算");
        }
        settlement.setStatus("settled");
        this.updateById(settlement);
    }

    /**
     * 按排班数据计算：普通工时工资与高温津贴分列
     */
    private Settlement calculate(String workerName, String period) {
        if (!StringUtils.hasText(workerName) || !StringUtils.hasText(period)) {
            throw new BusinessException("工人姓名和结算周期不能为空");
        }
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            throw new BusinessException("结算周期格式应为 yyyy-MM");
        }
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Schedule> schedules = scheduleService.list(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getWorkerName, workerName)
                .ge(Schedule::getWorkDate, start)
                .le(Schedule::getWorkDate, end));
        if (schedules.isEmpty()) {
            throw new BusinessException("该工人在此周期内无排班记录");
        }

        // 普通工时：全部排班工时合计
        BigDecimal normalHours = schedules.stream()
                .map(s -> s.getWorkHours() == null ? BigDecimal.ZERO : s.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 时薪：取周期内最近一次排班的时薪
        BigDecimal hourlyWage = schedules.stream()
                .max(Comparator.comparing(Schedule::getWorkDate))
                .map(Schedule::getHourlyWage)
                .orElse(BigDecimal.ZERO);
        BigDecimal normalAmount = normalHours.multiply(hourlyWage).setScale(2, RoundingMode.HALF_UP);

        // 高温津贴：触发高温保障的排班天数（按日期去重）× 津贴标准
        HeatPolicy policy = heatPolicyService.getActivePolicy();
        BigDecimal allowancePerDay = policy != null ? policy.getAllowancePerDay() : BigDecimal.ZERO;
        long heatDays = schedules.stream()
                .filter(scheduleService::isHeatAlert)
                .map(Schedule::getWorkDate)
                .distinct()
                .count();
        BigDecimal allowanceAmount = allowancePerDay.multiply(BigDecimal.valueOf(heatDays))
                .setScale(2, RoundingMode.HALF_UP);

        Settlement settlement = new Settlement();
        settlement.setWorkerName(workerName);
        settlement.setPeriod(period);
        settlement.setNormalHours(normalHours);
        settlement.setHourlyWage(hourlyWage);
        settlement.setNormalAmount(normalAmount);
        settlement.setHeatDays((int) heatDays);
        settlement.setAllowancePerDay(allowancePerDay);
        settlement.setAllowanceAmount(allowanceAmount);
        settlement.setTotalAmount(normalAmount.add(allowanceAmount).setScale(2, RoundingMode.HALF_UP));
        return settlement;
    }
}
