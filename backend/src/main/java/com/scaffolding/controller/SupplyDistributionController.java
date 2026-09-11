package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.SupplyDistribution;
import com.scaffolding.service.SupplyDistributionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 防暑物资发放控制器
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/supply")
@Api(tags = "防暑物资发放")
public class SupplyDistributionController {

    @Autowired
    private SupplyDistributionService supplyDistributionService;

    @GetMapping("/page")
    @ApiOperation("分页查询物资发放记录（工人手机端按姓名查询）")
    public Result<PageResult<SupplyDistribution>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateStart,
            @RequestParam(required = false) String dateEnd) {
        Page<SupplyDistribution> page = supplyDistributionService.pageQuery(
                current, size, workerName, status, dateStart, dateEnd);
        PageResult<SupplyDistribution> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/{id}/confirm")
    @ApiOperation("仓管确认发放")
    public Result<?> confirm(@PathVariable Long id, @RequestParam String keeperName) {
        try {
            supplyDistributionService.confirm(id, keeperName);
            return Result.success("确认发放成功");
        } catch (Exception e) {
            log.error("确认发放失败", e);
            return Result.error(e.getMessage());
        }
    }
}
