package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.HeatPolicy;

/**
 * 高温保障策略服务接口
 *
 * @author scaffolding
 */
public interface HeatPolicyService extends IService<HeatPolicy> {

    /**
     * 获取当前启用的高温策略，无则返回null
     */
    HeatPolicy getActivePolicy();
}
