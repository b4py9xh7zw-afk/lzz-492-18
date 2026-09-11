package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Schedule;
import com.scaffolding.vo.ScheduleVO;

/**
 * 排班服务接口
 *
 * @author scaffolding
 */
public interface ScheduleService extends IService<Schedule> {

    /**
     * 分页查询排班（附带高温保障提示）
     */
    Page<ScheduleVO> pageQuery(Long current, Long size, String workerName, String workEnv,
                               String workDateStart, String workDateEnd, Boolean heatOnly);

    /**
     * 保存排班，若触发高温保障则自动生成待发放物资记录
     */
    void saveWithHeatSupport(Schedule schedule);

    /**
     * 更新排班，同步高温物资记录
     */
    void updateWithHeatSupport(Schedule schedule);

    /**
     * 判断排班是否触发高温保障
     */
    boolean isHeatAlert(Schedule schedule);
}
