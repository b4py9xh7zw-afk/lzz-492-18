<template>
  <div class="h5-supply-page">
    <van-search
      v-model="workerName"
      placeholder="输入姓名查询我的物资"
      @search="handleSearch"
      class="mb-3"
    />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <div
          v-for="item in list"
          :key="item.id"
          class="supply-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-base font-semibold text-gray-800">
                <van-icon name="calendar-o" /> {{ item.distributeDate }}
              </h3>
              <van-tag
                :type="item.status === 'issued' ? 'success' : 'warning'"
                class="rounded-full"
              >
                {{ item.status === 'issued' ? '已发放' : '待发放' }}
              </van-tag>
            </div>

            <div class="grid grid-cols-3 gap-2 text-center">
              <div class="supply-item rounded-lg py-2">
                <van-icon name="fire-o" color="#ee0a24" size="20" />
                <p class="text-xs text-gray-500 mt-1">盐丸</p>
                <p class="text-sm font-bold text-gray-800">×{{ item.saltPillQty }}</p>
              </div>
              <div class="supply-item rounded-lg py-2">
                <van-icon name="shield-o" color="#1989fa" size="20" />
                <p class="text-xs text-gray-500 mt-1">冰袖</p>
                <p class="text-sm font-bold text-gray-800">×{{ item.iceSleeveQty }}</p>
              </div>
              <div class="supply-item rounded-lg py-2">
                <van-icon name="coupon-o" color="#07c160" size="20" />
                <p class="text-xs text-gray-500 mt-1">饮水券</p>
                <p class="text-sm font-bold text-gray-800">×{{ item.waterVoucherQty }}</p>
              </div>
            </div>

            <div v-if="item.status === 'issued'" class="mt-3 text-xs text-gray-500 flex items-center gap-1">
              <van-icon name="checked" color="#07c160" />
              仓管 {{ item.keeperName }} 已于 {{ item.confirmTime }} 确认发放
            </div>
            <div v-else class="mt-3 text-xs text-gray-400 flex items-center gap-1">
              <van-icon name="clock-o" />
              等待仓管确认发放
            </div>
          </div>
        </div>
        <van-empty v-if="!loading && list.length === 0 && finished" description="暂无物资发放记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { heatApi } from '@/api/heat'

const workerName = ref(localStorage.getItem('h5_worker_name') || '')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const current = ref(1)
const pageSize = 10

// 由 van-list 的 @load 触发（首次挂载自动触发）
const loadData = async () => {
  if (!workerName.value) {
    loading.value = false
    finished.value = true
    return
  }
  loading.value = true
  try {
    const res = await heatApi.supplyPage({
      current: current.value,
      size: pageSize,
      workerName: workerName.value
    })
    if (refreshing.value || current.value === 1) {
      list.value = res.data.records
      refreshing.value = false
    } else {
      list.value.push(...res.data.records)
    }
    if (list.value.length >= res.data.total) {
      finished.value = true
    } else {
      current.value++
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  localStorage.setItem('h5_worker_name', workerName.value)
  list.value = []
  current.value = 1
  finished.value = false
  refreshing.value = false
  loadData()
}

const onRefresh = () => {
  current.value = 1
  finished.value = false
  refreshing.value = true
  loadData()
}
</script>

<style scoped>
.supply-item {
  background-color: #f7f8fa;
}
</style>
