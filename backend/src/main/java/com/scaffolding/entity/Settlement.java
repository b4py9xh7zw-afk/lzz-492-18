package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 结算单实体类（高温津贴与普通工时分列）
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("settlement")
public class Settlement extends BaseEntity {

    /**
     * 工人姓名
     */
    private String workerName;

    /**
     * 结算周期（yyyy-MM）
     */
    private String period;

    /**
     * 普通工时（小时）
     */
    private BigDecimal normalHours;

    /**
     * 时薪（元/小时）
     */
    private BigDecimal hourlyWage;

    /**
     * 普通工时工资（元）
     */
    private BigDecimal normalAmount;

    /**
     * 高温津贴天数（天）
     */
    private Integer heatDays;

    /**
     * 津贴标准（元/天）
     */
    private BigDecimal allowancePerDay;

    /**
     * 高温津贴合计（元）
     */
    private BigDecimal allowanceAmount;

    /**
     * 应发合计（元）
     */
    private BigDecimal totalAmount;

    /**
     * 结算状态（pending-待结算，settled-已结算）
     */
    private String status;
}
