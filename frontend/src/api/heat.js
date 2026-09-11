import request from '@/utils/request'

/**
 * 高温作业保障API（策略、排班、物资发放、结算）
 */
export const heatApi = {
  // ---------- 高温策略 ----------
  // 获取当前启用的策略
  getPolicy() {
    return request({ url: '/heat-policy/active', method: 'get' })
  },
  // 更新策略
  updatePolicy(id, data) {
    return request({ url: `/heat-policy/${id}`, method: 'put', data })
  },

  // ---------- 排班 ----------
  // 分页查询排班（带高温提示）
  schedulePage(params) {
    return request({ url: '/schedule/page', method: 'get', params })
  },
  // 新增排班
  saveSchedule(data) {
    return request({ url: '/schedule', method: 'post', data })
  },
  // 更新排班
  updateSchedule(id, data) {
    return request({ url: `/schedule/${id}`, method: 'put', data })
  },
  // 删除排班
  deleteSchedule(id) {
    return request({ url: `/schedule/${id}`, method: 'delete' })
  },

  // ---------- 物资发放 ----------
  // 分页查询物资发放记录
  supplyPage(params) {
    return request({ url: '/supply/page', method: 'get', params })
  },
  // 仓管确认发放
  confirmSupply(id, keeperName) {
    return request({ url: `/supply/${id}/confirm`, method: 'put', params: { keeperName } })
  },

  // ---------- 结算 ----------
  // 分页查询结算单
  settlementPage(params) {
    return request({ url: '/settlement/page', method: 'get', params })
  },
  // 预览结算
  previewSettlement(workerName, period) {
    return request({ url: '/settlement/preview', method: 'get', params: { workerName, period } })
  },
  // 生成结算单
  generateSettlement(workerName, period) {
    return request({ url: '/settlement/generate', method: 'post', params: { workerName, period } })
  },
  // 标记已结算
  settle(id) {
    return request({ url: `/settlement/${id}/settle`, method: 'put' })
  }
}
