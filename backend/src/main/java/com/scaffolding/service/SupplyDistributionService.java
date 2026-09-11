package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.SupplyDistribution;

/**
 * 防暑物资发放服务接口
 *
 * @author scaffolding
 */
public interface SupplyDistributionService extends IService<SupplyDistribution> {

    /**
     * 分页查询物资发放记录
     */
    Page<SupplyDistribution> pageQuery(Long current, Long size, String workerName, String status,
                                       String dateStart, String dateEnd);

    /**
     * 仓管确认发放
     */
    void confirm(Long id, String keeperName);
}
