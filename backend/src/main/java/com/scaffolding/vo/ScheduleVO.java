package com.scaffolding.vo;

import com.scaffolding.entity.Schedule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 排班视图对象（附带高温保障提示）
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleVO extends Schedule {

    /**
     * 是否触发高温保障（室外岗位且气温达到阈值）
     */
    private Boolean heatAlert;

    /**
     * 高温津贴（元/天）
     */
    private BigDecimal allowance;

    /**
     * 休息频次提示
     */
    private String restFrequency;

    /**
     * 防暑物资发放提示
     */
    private String suppliesTip;

    /**
     * 物资发放状态（pending-待发放，issued-已发放，null-无记录）
     */
    private String supplyStatus;
}
