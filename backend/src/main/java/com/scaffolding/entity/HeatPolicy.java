package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 高温保障策略实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("heat_policy")
public class HeatPolicy extends BaseEntity {

    /**
     * 高温阈值（℃），室外岗位气温达到即触发保障
     */
    private BigDecimal tempThreshold;

    /**
     * 高温津贴标准（元/人/天）
     */
    private BigDecimal allowancePerDay;

    /**
     * 休息频次说明
     */
    private String restFrequency;

    /**
     * 盐丸发放量（粒/人/天）
     */
    private Integer saltPillQty;

    /**
     * 冰袖发放量（副/人/天）
     */
    private Integer iceSleeveQty;

    /**
     * 饮水券发放量（张/人/天）
     */
    private Integer waterVoucherQty;

    /**
     * 是否启用（1-启用，0-停用）
     */
    private Integer enabled;
}
