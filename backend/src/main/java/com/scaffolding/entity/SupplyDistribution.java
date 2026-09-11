package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 防暑物资发放记录实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_distribution")
public class SupplyDistribution extends BaseEntity {

    /**
     * 工人姓名
     */
    private String workerName;

    /**
     * 关联排班ID
     */
    private Long scheduleId;

    /**
     * 发放日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate distributeDate;

    /**
     * 盐丸数量（粒）
     */
    private Integer saltPillQty;

    /**
     * 冰袖数量（副）
     */
    private Integer iceSleeveQty;

    /**
     * 饮水券数量（张）
     */
    private Integer waterVoucherQty;

    /**
     * 发放状态（pending-待发放，issued-已发放）
     */
    private String status;

    /**
     * 仓管确认人
     */
    private String keeperName;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime confirmTime;
}
