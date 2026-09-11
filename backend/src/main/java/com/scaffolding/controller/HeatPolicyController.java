package com.scaffolding.controller;

import com.scaffolding.common.Result;
import com.scaffolding.entity.HeatPolicy;
import com.scaffolding.service.HeatPolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 高温保障策略控制器
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/heat-policy")
@Api(tags = "高温保障策略")
public class HeatPolicyController {

    @Autowired
    private HeatPolicyService heatPolicyService;

    @GetMapping("/active")
    @ApiOperation("获取当前启用的高温策略")
    public Result<HeatPolicy> getActive() {
        return Result.success(heatPolicyService.getActivePolicy());
    }

    @PutMapping("/{id}")
    @ApiOperation("更新高温策略")
    public Result<HeatPolicy> update(@PathVariable Long id, @RequestBody HeatPolicy policy) {
        try {
            policy.setId(id);
            policy.setUpdateTime(LocalDateTime.now());
            heatPolicyService.updateById(policy);
            return Result.success("策略更新成功", policy);
        } catch (Exception e) {
            log.error("更新高温策略失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("新增高温策略")
    public Result<HeatPolicy> save(@RequestBody HeatPolicy policy) {
        try {
            policy.setCreateTime(LocalDateTime.now());
            policy.setUpdateTime(LocalDateTime.now());
            heatPolicyService.save(policy);
            return Result.success("策略保存成功", policy);
        } catch (Exception e) {
            log.error("新增高温策略失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}
