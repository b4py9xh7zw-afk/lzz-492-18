<template>
  <div class="settlement-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">结算管理</span>
          <el-button
            type="primary"
            @click="openGenerate"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            生成结算单
          </el-button>
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
        <el-form-item label="结算周期">
          <el-date-picker
            v-model="searchForm.period"
            type="month"
            placeholder="请选择月份"
            value-format="YYYY-MM"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="待结算" value="pending" />
            <el-option label="已结算" value="settled" />
          </el-select>
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

      <!-- 津贴与普通工时分组表头，分开展示 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        show-summary
        :summary-method="getSummary"
      >
        <el-table-column prop="workerName" label="工人" width="100" fixed />
        <el-table-column prop="period" label="周期" width="90" />
        <el-table-column label="普通工时" align="center">
          <el-table-column prop="normalHours" label="工时(h)" width="90" align="right" />
          <el-table-column prop="hourlyWage" label="时薪(元)" width="90" align="right">
            <template #default="{ row }">{{ formatMoney(row.hourlyWage) }}</template>
          </el-table-column>
          <el-table-column prop="normalAmount" label="工时工资(元)" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.normalAmount) }}</template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="高温津贴（单列）" align="center">
          <el-table-column prop="heatDays" label="高温天数" width="90" align="right">
            <template #default="{ row }">
              <span :class="row.heatDays > 0 ? 'text-red-600 font-medium' : ''">{{ row.heatDays }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="allowancePerDay" label="标准(元/天)" width="100" align="right">
            <template #default="{ row }">{{ formatMoney(row.allowancePerDay) }}</template>
          </el-table-column>
          <el-table-column prop="allowanceAmount" label="津贴合计(元)" width="110" align="right">
            <template #default="{ row }">
              <span :class="row.allowanceAmount > 0 ? 'text-red-600 font-medium' : ''">
                {{ formatMoney(row.allowanceAmount) }}
              </span>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column prop="totalAmount" label="应发合计(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="font-bold text-gray-800">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'settled' ? 'success' : 'warning'" class="rounded-full px-3">
              {{ row.status === 'settled' ? '已结算' : '待结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              size="small"
              @click="handleSettle(row)"
              class="rounded-lg"
            >
              结算
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

    <!-- 生成结算单弹窗 -->
    <el-dialog v-model="generateVisible" title="生成结算单" width="520px" class="rounded-lg" destroy-on-close>
      <el-form :model="generateForm" label-width="90px">
        <el-form-item label="工人姓名" required>
          <el-input v-model="generateForm.workerName" placeholder="请输入工人姓名" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="结算周期" required>
          <el-date-picker
            v-model="generateForm.period"
            type="month"
            placeholder="请选择月份"
            value-format="YYYY-MM"
            class="rounded-lg w-full"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handlePreview" :loading="previewing" class="rounded-lg">预览</el-button>
        </el-form-item>
      </el-form>

      <!-- 预览结果：津贴与工时分列 -->
      <el-descriptions
        v-if="previewData"
        :column="2"
        border
        class="rounded-lg overflow-hidden"
        title="结算预览"
      >
        <el-descriptions-item label="普通工时">{{ previewData.normalHours }} 小时</el-descriptions-item>
        <el-descriptions-item label="时薪">¥{{ formatMoney(previewData.hourlyWage) }}/h</el-descriptions-item>
        <el-descriptions-item label="工时工资">¥{{ formatMoney(previewData.normalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="高温天数">{{ previewData.heatDays }} 天</el-descriptions-item>
        <el-descriptions-item label="津贴标准">¥{{ formatMoney(previewData.allowancePerDay) }}/天</el-descriptions-item>
        <el-descriptions-item label="高温津贴">
          <span class="text-red-600 font-medium">¥{{ formatMoney(previewData.allowanceAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="应发合计" :span="2">
          <span class="font-bold text-lg">¥{{ formatMoney(previewData.totalAmount) }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="generateVisible = false" class="rounded-lg">取消</el-button>
        <el-button
          type="primary"
          @click="handleGenerate"
          :loading="generating"
          :disabled="!previewData"
          class="rounded-lg"
        >
          确认生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { heatApi } from '@/api/heat'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ workerName: '', period: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const generateVisible = ref(false)
const generateForm = reactive({ workerName: '', period: '' })
const previewData = ref(null)
const previewing = ref(false)
const generating = ref(false)

const formatMoney = (val) => (val == null ? '0.00' : Number(val).toFixed(2))

const loadData = async () => {
  loading.value = true
  try {
    const res = await heatApi.settlementPage({
      current: pagination.current,
      size: pagination.size,
      workerName: searchForm.workerName || undefined,
      period: searchForm.period || undefined,
      status: searchForm.status || undefined
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
  searchForm.period = ''
  searchForm.status = ''
  handleSearch()
}

const openGenerate = () => {
  generateForm.workerName = ''
  generateForm.period = ''
  previewData.value = null
  generateVisible.value = true
}

const handlePreview = async () => {
  if (!generateForm.workerName || !generateForm.period) {
    ElMessage.warning('请填写工人姓名和结算周期')
    return
  }
  previewing.value = true
  try {
    const res = await heatApi.previewSettlement(generateForm.workerName, generateForm.period)
    previewData.value = res.data
  } catch (e) {
    previewData.value = null
  } finally {
    previewing.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    await heatApi.generateSettlement(generateForm.workerName, generateForm.period)
    ElMessage.success('结算单生成成功')
    generateVisible.value = false
    loadData()
  } catch (e) { /* 拦截器已提示 */ } finally {
    generating.value = false
  }
}

const handleSettle = (row) => {
  ElMessageBox.confirm(
    `确认结算 ${row.workerName} ${row.period} 周期：工时工资 ¥${formatMoney(row.normalAmount)} + 高温津贴 ¥${formatMoney(row.allowanceAmount)} = ¥${formatMoney(row.totalAmount)}？`,
    '结算确认',
    { confirmButtonText: '确认结算', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await heatApi.settle(row.id)
    ElMessage.success('结算完成')
    loadData()
  }).catch(() => {})
}

// 合计行：工时工资、津贴、总额分别汇总
const getSummary = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '本页合计'
      return
    }
    const prop = column.property
    if (['normalAmount', 'allowanceAmount', 'totalAmount'].includes(prop)) {
      const total = data.reduce((sum, row) => sum + Number(row[prop] || 0), 0)
      sums[index] = '¥' + total.toFixed(2)
    } else if (prop === 'normalHours') {
      sums[index] = data.reduce((sum, row) => sum + Number(row[prop] || 0), 0) + ' h'
    } else if (prop === 'heatDays') {
      sums[index] = data.reduce((sum, row) => sum + Number(row[prop] || 0), 0) + ' 天'
    } else {
      sums[index] = ''
    }
  })
  return sums
}

onMounted(loadData)
</script>
