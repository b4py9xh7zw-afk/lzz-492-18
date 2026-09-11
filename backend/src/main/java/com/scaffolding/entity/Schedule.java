package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule")
public class Schedule extends BaseEntity {

    /**
     * 工人姓名
     */
    private String workerName;

    /**
     * 岗位名称
     */
    private String position;

    /**
     * 作业环境（outdoor-室外，indoor-室内）
     */
    private String workEnv;

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate workDate;

    /**
     * 班次（白班/夜班等）
     */
    private String shiftName;

    /**
     * 当日气温（℃）
     */
    private BigDecimal temperature;

    /**
     * 工时（小时）
     */
    private BigDecimal workHours;

    /**
     * 时薪（元/小时）
     */
    private BigDecimal hourlyWage;

    /**
     * 备注
     */
    private String remark;
}
