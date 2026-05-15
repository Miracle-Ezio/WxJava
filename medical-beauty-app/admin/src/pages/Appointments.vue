<template>
  <div>
    <h2 class="section-title" style="margin: 0 0 24px;">预约管理</h2>

    <el-card>
      <div class="filters">
        <el-select v-model="status" placeholder="所有状态" clearable style="width: 160px;" @change="reload">
          <el-option label="待确认"   :value="1" />
          <el-option label="已确认"   :value="2" />
          <el-option label="已到店"   :value="3" />
          <el-option label="已完成"   :value="4" />
          <el-option label="客户取消" :value="5" />
          <el-option label="机构取消" :value="6" />
          <el-option label="未到"     :value="7" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="起"
          end-placeholder="止"
          value-format="YYYY-MM-DD"
          @change="reload"
        />
        <el-input
          v-model="keyword"
          placeholder="搜索客户姓名 / 昵称"
          style="width: 240px;"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <el-table :data="rows" v-loading="loading" stripe style="margin-top: 16px;">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            <div>{{ fmtDt(row.startAt) }}</div>
            <div class="text-muted" style="font-size: 12px;">~ {{ hhmm(row.endAt) }}（{{ row.durationMin }} min）</div>
          </template>
        </el-table-column>
        <el-table-column label="客户" prop="customerName" width="140" />
        <el-table-column label="项目" prop="projectName" min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.status]" effect="light">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="text-gold">¥{{ row.unitPrice || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="160">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.customerNote || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="right" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button v-if="row.status === 1" size="small" type="primary" @click="onConfirm(row)">确认</el-button>
              <el-button v-if="row.status === 2" size="small" type="primary" @click="onCheckIn(row)">到店</el-button>
              <el-button v-if="row.status === 3" size="small" type="success" @click="onComplete(row)">完成</el-button>
              <el-button v-if="[1,2].includes(row.status)" size="small" @click="onCancel(row)">取消</el-button>
              <el-button v-if="row.status === 2" size="small" type="danger" plain @click="onNoShow(row)">爽约</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  listAdminAppointments, confirmAppointment, checkInAppointment,
  completeAppointment, cancelAppointment, markNoShow,
  STATUS_TAG, type AdminAppointment,
} from '@/api/appointment';

const rows = ref<AdminAppointment[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const status = ref<number | ''>('');
const dateRange = ref<[string, string] | null>(null);
const keyword = ref('');
const loading = ref(false);

async function load() {
  loading.value = true;
  try {
    const r = await listAdminAppointments({
      status: status.value === '' ? undefined : Number(status.value),
      from: dateRange.value?.[0],
      to:   dateRange.value?.[1],
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    });
    rows.value = r.rows;
    total.value = r.total;
  } finally {
    loading.value = false;
  }
}

function reload() { page.value = 1; load(); }

async function onConfirm(row: AdminAppointment) {
  await confirmAppointment(row.id);
  ElMessage.success('已确认，订阅消息已推送');
  load();
}
async function onCheckIn(row: AdminAppointment) {
  await checkInAppointment(row.id);
  ElMessage.success('客户已到店');
  load();
}
async function onComplete(row: AdminAppointment) {
  await completeAppointment(row.id);
  ElMessage.success('已完成');
  load();
}
async function onCancel(row: AdminAppointment) {
  const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
    inputPlaceholder: '如：客户来电改期',
    inputType: 'textarea',
  }).catch(() => ({ value: null }));
  if (reason === null) return;
  await cancelAppointment(row.id, reason || undefined);
  ElMessage.success('已取消');
  load();
}
async function onNoShow(row: AdminAppointment) {
  await ElMessageBox.confirm('确认标记客户未到（爽约）？', '提示', { type: 'warning' });
  await markNoShow(row.id);
  ElMessage.success('已标记爽约');
  load();
}

function fmtDt(s: string) { return s ? s.replace('T', ' ').slice(0, 16) : ''; }
function hhmm(s: string)  { return s ? s.split('T')[1]?.slice(0, 5) : ''; }
</script>

<style scoped>
.filters { display: flex; gap: 12px; flex-wrap: wrap; }
.pager   { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
