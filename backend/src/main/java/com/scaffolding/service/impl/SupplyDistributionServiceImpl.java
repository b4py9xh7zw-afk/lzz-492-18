package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.SupplyDistribution;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.SupplyDistributionMapper;
import com.scaffolding.service.SupplyDistributionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 防暑物资发放服务实现类
 *
 * @author scaffolding
 */
@Service
public class SupplyDistributionServiceImpl extends ServiceImpl<SupplyDistributionMapper, SupplyDistribution>
        implements SupplyDistributionService {

    @Override
    public Page<SupplyDistribution> pageQuery(Long current, Long size, String workerName, String status,
                                              String dateStart, String dateEnd) {
        Page<SupplyDistribution> page = new Page<>(current, size);
        LambdaQueryWrapper<SupplyDistribution> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(workerName)) {
            wrapper.like(SupplyDistribution::getWorkerName, workerName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SupplyDistribution::getStatus, status);
        }
        if (StringUtils.hasText(dateStart)) {
            wrapper.ge(SupplyDistribution::getDistributeDate, dateStart);
        }
        if (StringUtils.hasText(dateEnd)) {
            wrapper.le(SupplyDistribution::getDistributeDate, dateEnd);
        }
        wrapper.orderByDesc(SupplyDistribution::getDistributeDate).orderByDesc(SupplyDistribution::getId);
        return this.page(page, wrapper);
    }

    @Override
    public void confirm(Long id, String keeperName) {
        SupplyDistribution distribution = this.getById(id);
        if (distribution == null) {
            throw new BusinessException("发放记录不存在");
        }
        if ("issued".equals(distribution.getStatus())) {
            throw new BusinessException("该记录已确认发放，请勿重复操作");
        }
        distribution.setStatus("issued");
        distribution.setKeeperName(keeperName);
        distribution.setConfirmTime(LocalDateTime.now());
        this.updateById(distribution);
    }
}
