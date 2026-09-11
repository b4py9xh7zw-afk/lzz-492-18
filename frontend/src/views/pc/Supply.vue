<template>
  <div class="supply-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">防暑物资发放</span>
          <el-tag v-if="pendingCount > 0" type="warning" class="rounded-full px-3">
            {{ pendingCount }} 条待发放
          </el-tag>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="工人姓名">
          <el-input
            v-model="searchForm.workerName"
            placeholder="请输入工人姓名"
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="发放状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="待发放" value="pending" />
            <el-option label="已发放" value="issued" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button @click="handleReset" class="rounded-lg">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="workerName" label="工人" width="110" />
        <el-table-column prop="distributeDate" label="发放日期" width="120" />
        <el-table-column label="发放物资" min-width="260">
          <template #default="{ row }">
            <div class="flex gap-2 flex-wrap">
              <el-tag type="danger" effect="plain" class="rounded-full">盐丸 ×{{ row.saltPillQty }}</el-tag>
              <el-tag type="primary" effect="plain" class="rounded-full">冰袖 ×{{ row.iceSleeveQty }}</el-tag>
              <el-tag type="success" effect="plain" class="rounded-full">饮水券 ×{{ row.waterVoucherQty }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'issued' ? 'success' : 'warning'" class="rounded-full px-3">
              {{ row.status === 'issued' ? '已发放' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keeperName" label="仓管确认人" width="120">
          <template #default="{ row }">{{ row.keeperName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" width="170">
          <template #default="{ row }">{{ row.confirmTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              size="small"
              @click="handleConfirm(row)"
              class="rounded-lg"
            >
              确认发放
            </el-button>
            <span v-else class="text-gray-400 text-sm">已完成</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { heatApi } from '@/api/heat'

const loading = ref(false)
const tableData = ref([])
const dateRange = ref([])

const searchForm = reactive({ workerName: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const pendingCount = computed(() => tableData.value.filter(r => r.status === 'pending').length)

const loadData = async () => {
  loading.value = true
  try {
    const res = await heatApi.supplyPage({
      current: pagination.current,
      size: pagination.size,
      workerName: searchForm.workerName || undefined,
      status: searchForm.status || undefined,
      dateStart: dateRange.value?.[0],
      dateEnd: dateRange.value?.[1]
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.workerName = ''
  searchForm.status = ''
  dateRange.value = []
  handleSearch()
}

const handleConfirm = (row) => {
  ElMessageBox.prompt(
    `确认为 ${row.workerName} 发放：盐丸${row.saltPillQty}粒、冰袖${row.iceSleeveQty}副、饮水券${row.waterVoucherQty}张`,
    '仓管确认发放',
    {
      confirmButtonText: '确认发放',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入仓管姓名',
      inputValidator: (val) => (val && val.trim() ? true : '仓管姓名不能为空')
    }
  ).then(async ({ value }) => {
    await heatApi.confirmSupply(row.id, value.trim())
    ElMessage.success('确认发放成功，工人手机端可查看')
    loadData()
  }).catch(() => {})
}

onMounted(loadData)
</script>
