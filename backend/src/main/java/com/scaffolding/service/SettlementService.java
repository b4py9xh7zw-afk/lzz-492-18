package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Settlement;

/**
 * 结算单服务接口
 *
 * @author scaffolding
 */
public interface SettlementService extends IService<Settlement> {

    /**
     * 分页查询结算单
     */
    Page<Settlement> pageQuery(Long current, Long size, String workerName, String period, String status);

    /**
     * 生成（或重新生成）某工人某周期的结算单，津贴与普通工时分列
     */
    Settlement generate(String workerName, String period);

    /**
     * 预览结算（不落库）
     */
    Settlement preview(String workerName, String period);

    /**
     * 标记已结算
     */
    void settle(Long id);
}
