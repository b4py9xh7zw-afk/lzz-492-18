package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatPolicy;
import com.scaffolding.mapper.HeatPolicyMapper;
import com.scaffolding.service.HeatPolicyService;
import org.springframework.stereotype.Service;

/**
 * 高温保障策略服务实现类
 *
 * @author scaffolding
 */
@Service
public class HeatPolicyServiceImpl extends ServiceImpl<HeatPolicyMapper, HeatPolicy> implements HeatPolicyService {

    @Override
    public HeatPolicy getActivePolicy() {
        return this.getOne(new LambdaQueryWrapper<HeatPolicy>()
                .eq(HeatPolicy::getEnabled, 1)
                .orderByDesc(HeatPolicy::getId)
                .last("LIMIT 1"));
    }
}
