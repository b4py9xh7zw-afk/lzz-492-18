<template>
  <div class="h5-schedule-page">
    <van-search
      v-model="workerName"
      placeholder="输入姓名查询我的排班"
      @search="handleSearch"
      class="mb-3"
    />

    <van-notice-bar
      v-if="heatList.length > 0"
      type="danger"
      left-icon="warning-o"
      :text="`您有 ${heatList.length} 天高温排班，请注意防暑降温，及时领取津贴与物资`"
      class="mb-3 rounded-lg"
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
          class="schedule-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
          :class="{ 'heat-card': item.heatAlert }"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-semibold text-gray-800">
                {{ item.workDate }} {{ item.shiftName }}
              </h3>
              <van-tag v-if="item.heatAlert" type="danger" class="rounded-full">高温</van-tag>
            </div>
            <div class="text-sm text-gray-600 space-y-1">
              <p>
                <van-icon name="location-o" /> {{ item.position }}
                <van-tag :type="item.workEnv === 'outdoor' ? 'warning' : 'default'" size="small" class="ml-1 rounded-full">
                  {{ item.workEnv === 'outdoor' ? '室外' : '室内' }}
                </van-tag>
              </p>
              <p>
                <van-icon name="fire-o" v-if="item.heatAlert" />
                <van-icon name="thermometer" v-else />
                气温 {{ item.temperature != null ? item.temperature + '℃' : '未知' }}
                · 工时 {{ item.workHours }}h
              </p>
            </div>

            <!-- 高温保障提示 -->
            <div v-if="item.heatAlert" class="heat-tips mt-3 p-3 rounded-lg">
              <p class="text-sm font-medium text-red-600 mb-1">
                <van-icon name="umbrella-circle" /> 高温保障
              </p>
              <p class="text-xs text-gray-700">津贴：¥{{ item.allowance }}/天</p>
              <p class="text-xs text-gray-700">休息：{{ item.restFrequency }}</p>
              <p class="text-xs text-gray-700">
                物资：{{ item.suppliesTip }}
                <van-tag
                  v-if="item.supplyStatus"
                  :type="item.supplyStatus === 'issued' ? 'success' : 'warning'"
                  size="small"
                  class="rounded-full"
                >
                  {{ item.supplyStatus === 'issued' ? '已发放' : '待发放' }}
                </van-tag>
              </p>
            </div>
          </div>
        </div>
        <van-empty v-if="!loading && list.length === 0 && finished" description="暂无排班记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { heatApi } from '@/api/heat'

const workerName = ref(localStorage.getItem('h5_worker_name') || '')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const current = ref(1)
const pageSize = 10

const heatList = computed(() => list.value.filter(i => i.heatAlert))

// 由 van-list 的 @load 触发（首次挂载自动触发）
const loadData = async () => {
  if (!workerName.value) {
    loading.value = false
    finished.value = true
    return
  }
  loading.value = true
  try {
    const res = await heatApi.schedulePage({
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
.heat-card {
  border: 1px solid #fde2e2;
}
.heat-tips {
  background-color: #fef0f0;
}
</style>
