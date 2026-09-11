package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Settlement;
import com.scaffolding.service.SettlementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 结算管理控制器（津贴与普通工时分列）
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/settlement")
@Api(tags = "结算管理")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping("/page")
    @ApiOperation("分页查询结算单")
    public Result<PageResult<Settlement>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status) {
        Page<Settlement> page = settlementService.pageQuery(current, size, workerName, period, status);
        PageResult<Settlement> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/preview")
    @ApiOperation("预览结算（不落库）")
    public Result<Settlement> preview(@RequestParam String workerName, @RequestParam String period) {
        try {
            return Result.success(settlementService.preview(workerName, period));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/generate")
    @ApiOperation("生成结算单")
    public Result<Settlement> generate(@RequestParam String workerName, @RequestParam String period) {
        try {
            Settlement settlement = settlementService.generate(workerName, period);
            return Result.success("结算单生成成功", settlement);
        } catch (Exception e) {
            log.error("生成结算单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/settle")
    @ApiOperation("标记已结算")
    public Result<?> settle(@PathVariable Long id) {
        try {
            settlementService.settle(id);
            return Result.success("结算完成");
        } catch (Exception e) {
            log.error("结算失败", e);
            return Result.error(e.getMessage());
        }
    }
}
