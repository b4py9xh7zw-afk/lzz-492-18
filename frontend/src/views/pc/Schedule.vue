<template>
  <div class="schedule-page">
    <!-- 高温预警横幅 -->
    <el-alert
      v-if="heatCount > 0"
      type="error"
      :closable="false"
      class="mb-4 rounded-lg"
      show-icon
    >
      <template #title>
        <span class="font-medium">
          当前列表有 {{ heatCount }} 条高温排班（室外岗位气温≥{{ policy.tempThreshold }}℃），
          请落实高温津贴、休息频次与防暑物资发放
        </span>
      </template>
    </el-alert>

    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">排班管理</span>
          <div class="flex gap-2">
            <el-button
              type="warning"
              plain
              @click="openPolicyDialog"
              class="rounded-lg"
            >
              <el-icon><Sunny /></el-icon>
              高温策略配置
            </el-button>
            <el-button
              type="primary"
              @click="handleAdd"
              class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <el-icon><Plus /></el-icon>
              新增排班
            </el-button>
          </div>
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
        <el-form-item label="作业环境">
          <el-select
            v-model="searchForm.workEnv"
            placeholder="请选择"
            clearable
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="室外" value="outdoor" />
            <el-option label="室内" value="indoor" />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期">
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
        <el-form-item label="只看高温">
          <el-switch v-model="searchForm.heatOnly" @change="handleSearch" />
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
        :row-class-name="heatRowClass"
      >
        <el-table-column prop="workerName" label="工人" width="100" />
        <el-table-column prop="position" label="岗位" min-width="120" />
        <el-table-column prop="workEnv" label="环境" width="90">
          <template #default="{ row }">
            <el-tag :type="row.workEnv === 'outdoor' ? 'warning' : 'info'" class="rounded-full px-3">
              {{ row.workEnv === 'outdoor' ? '室外' : '室内' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workDate" label="日期" width="110" />
        <el-table-column prop="shiftName" label="班次" width="90" />
        <el-table-column prop="temperature" label="气温" width="90">
          <template #default="{ row }">
            <span :class="row.heatAlert ? 'text-red-600 font-bold' : ''">
              {{ row.temperature != null ? row.temperature + '℃' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时(h)" width="90" />
        <el-table-column label="高温保障提示" min-width="300">
          <template #default="{ row }">
            <div v-if="row.heatAlert" class="flex flex-col gap-1 py-1">
              <el-tag type="danger" size="small" class="rounded-full w-fit">
                高温津贴 ¥{{ row.allowance }}/天
              </el-tag>
              <span class="text-xs text-gray-600">休息：{{ row.restFrequency }}</span>
              <span class="text-xs text-gray-600">
                物资：{{ row.suppliesTip }}
                <el-tag
                  v-if="row.supplyStatus"
                  :type="row.supplyStatus === 'issued' ? 'success' : 'warning'"
                  size="small"
                  class="ml-1 rounded-full"
                >
                  {{ row.supplyStatus === 'issued' ? '已发放' : '待发放' }}
                </el-tag>
              </span>
            </div>
            <span v-else class="text-gray-400 text-sm">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑排班弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      class="rounded-lg"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="工人姓名" prop="workerName">
          <el-input v-model="formData.workerName" placeholder="请输入工人姓名" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-input v-model="formData.position" placeholder="请输入岗位名称" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="作业环境" prop="workEnv">
          <el-radio-group v-model="formData.workEnv">
            <el-radio value="outdoor">室外</el-radio>
            <el-radio value="indoor">室内</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排班日期" prop="workDate">
          <el-date-picker
            v-model="formData.workDate"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            class="rounded-lg w-full"
          />
        </el-form-item>
        <el-form-item label="班次" prop="shiftName">
          <el-select v-model="formData.shiftName" placeholder="请选择班次" class="rounded-lg w-full">
            <el-option label="白班" value="白班" />
            <el-option label="夜班" value="夜班" />
          </el-select>
        </el-form-item>
        <el-form-item label="当日气温" prop="temperature">
          <el-input-number
            v-model="formData.temperature"
            :min="-20"
            :max="50"
            :precision="1"
            :step="0.5"
            class="rounded-lg"
          />
          <span class="ml-2 text-gray-500">℃</span>
        </el-form-item>
        <el-form-item label="工时" prop="workHours">
          <el-input-number
            v-model="formData.workHours"
            :min="0"
            :max="24"
            :precision="1"
            class="rounded-lg"
          />
          <span class="ml-2 text-gray-500">小时</span>
        </el-form-item>
        <el-form-item label="时薪" prop="hourlyWage">
          <el-input-number
            v-model="formData.hourlyWage"
            :min="0"
            :precision="2"
            class="rounded-lg"
          />
          <span class="ml-2 text-gray-500">元/小时</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" class="rounded-lg" />
        </el-form-item>

        <!-- 实时高温提示 -->
        <el-alert
          v-if="formHeatAlert"
          type="error"
          :closable="false"
          show-icon
          class="rounded-lg mb-2"
        >
          <template #title>
            <div class="text-sm">
              <p class="font-bold mb-1">已触发高温保障（室外 + 气温≥{{ policy.tempThreshold }}℃）</p>
              <p>· 高温津贴：¥{{ policy.allowancePerDay }}/天</p>
              <p>· 休息频次：{{ policy.restFrequency }}</p>
              <p>· 防暑物资：盐丸{{ policy.saltPillQty }}粒、冰袖{{ policy.iceSleeveQty }}副、饮水券{{ policy.waterVoucherQty }}张（保存后自动生成发放单）</p>
            </div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="rounded-lg">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting" class="rounded-lg">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 高温策略配置弹窗 -->
    <el-dialog v-model="policyDialogVisible" title="高温策略配置" width="520px" class="rounded-lg" destroy-on-close>
      <el-form :model="policyForm" label-width="130px">
        <el-form-item label="高温阈值">
          <el-input-number v-model="policyForm.tempThreshold" :min="30" :max="45" :precision="1" />
          <span class="ml-2 text-gray-500">℃（室外岗位达到即触发）</span>
        </el-form-item>
        <el-form-item label="高温津贴标准">
          <el-input-number v-model="policyForm.allowancePerDay" :min="0" :precision="2" />
          <span class="ml-2 text-gray-500">元/人/天</span>
        </el-form-item>
        <el-form-item label="休息频次">
          <el-input v-model="policyForm.restFrequency" type="textarea" :rows="2" class="rounded-lg" />
        </el-form-item>
        <el-form-item label="盐丸发放量">
          <el-input-number v-model="policyForm.saltPillQty" :min="0" />
          <span class="ml-2 text-gray-500">粒/人/天</span>
        </el-form-item>
        <el-form-item label="冰袖发放量">
          <el-input-number v-model="policyForm.iceSleeveQty" :min="0" />
          <span class="ml-2 text-gray-500">副/人/天</span>
        </el-form-item>
        <el-form-item label="饮水券发放量">
          <el-input-number v-model="policyForm.waterVoucherQty" :min="0" />
          <span class="ml-2 text-gray-500">张/人/天</span>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="policyForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="policyDialogVisible = false" class="rounded-lg">取消</el-button>
        <el-button type="primary" @click="savePolicy" :loading="savingPolicy" class="rounded-lg">
          保存策略
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sunny } from '@element-plus/icons-vue'
import { heatApi } from '@/api/heat'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dateRange = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref(null)

const searchForm = reactive({
  workerName: '',
  workEnv: '',
  heatOnly: false
})

const pagination = reactive({ current: 1, size: 10, total: 0 })

const defaultForm = {
  id: null,
  workerName: '',
  position: '',
  workEnv: 'outdoor',
  workDate: '',
  shiftName: '白班',
  temperature: null,
  workHours: 8,
  hourlyWage: 25,
  remark: ''
}
const formData = reactive({ ...defaultForm })

const formRules = {
  workerName: [{ required: true, message: '请输入工人姓名', trigger: 'blur' }],
  position: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  workEnv: [{ required: true, message: '请选择作业环境', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  temperature: [{ required: true, message: '请填写当日气温', trigger: 'blur' }]
}

// 高温策略
const policy = ref({})
const policyDialogVisible = ref(false)
const savingPolicy = ref(false)
const policyForm = reactive({})

// 表单实时高温判定
const formHeatAlert = computed(() => {
  return formData.workEnv === 'outdoor'
    && formData.temperature != null
    && policy.value.tempThreshold != null
    && formData.temperature >= policy.value.tempThreshold
})

// 当前页高温排班数
const heatCount = computed(() => tableData.value.filter(r => r.heatAlert).length)

const heatRowClass = ({ row }) => (row.heatAlert ? 'heat-row' : '')

const loadPolicy = async () => {
  try {
    const res = await heatApi.getPolicy()
    policy.value = res.data || {}
  } catch (e) { /* 无策略时忽略 */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      workerName: searchForm.workerName || undefined,
      workEnv: searchForm.workEnv || undefined,
      workDateStart: dateRange.value?.[0],
      workDateEnd: dateRange.value?.[1],
      heatOnly: searchForm.heatOnly || undefined
    }
    const res = await heatApi.schedulePage(params)
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
  searchForm.workEnv = ''
  searchForm.heatOnly = false
  dateRange.value = []
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, defaultForm)
  dialogTitle.value = '新增排班'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    workerName: row.workerName,
    position: row.position,
    workEnv: row.workEnv,
    workDate: row.workDate,
    shiftName: row.shiftName,
    temperature: row.temperature,
    workHours: row.workHours,
    hourlyWage: row.hourlyWage,
    remark: row.remark
  })
  dialogTitle.value = '编辑排班'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return // 校验不通过，Element Plus 已提示
  }
  submitting.value = true
  try {
    if (formData.id) {
      await heatApi.updateSchedule(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      const res = await heatApi.saveSchedule(formData)
      ElMessage.success(res.message || '新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* 拦截器已提示 */ } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除 ${row.workerName} ${row.workDate} 的排班吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await heatApi.deleteSchedule(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const openPolicyDialog = () => {
  Object.assign(policyForm, policy.value)
  policyDialogVisible.value = true
}

const savePolicy = async () => {
  savingPolicy.value = true
  try {
    await heatApi.updatePolicy(policyForm.id, policyForm)
    ElMessage.success('策略保存成功')
    policyDialogVisible.value = false
    await loadPolicy()
    loadData()
  } finally {
    savingPolicy.value = false
  }
}

onMounted(() => {
  loadPolicy()
  loadData()
})
</script>

<style scoped>
.schedule-page :deep(.heat-row) {
  background-color: #fef0f0;
}
.schedule-page :deep(.heat-row:hover > td) {
  background-color: #fde2e2 !important;
}
</style>
