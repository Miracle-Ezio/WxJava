<template>
  <div>
    <h2 class="section-title" style="margin: 0 0 24px;">操作日志</h2>

    <el-card>
      <div class="filters">
        <el-select v-model="filters.module" placeholder="所有模块" clearable style="width: 160px;" @change="reload">
          <el-option label="规划方案" value="plan" />
          <el-option label="预约"     value="appointment" />
          <el-option label="客户"     value="customer" />
          <el-option label="认证"     value="auth" />
          <el-option label="照片"     value="photo" />
        </el-select>
        <el-select v-model="filters.operatorType" placeholder="所有角色" clearable style="width: 160px;" @change="reload">
          <el-option label="员工" :value="1" />
          <el-option label="客户" :value="2" />
          <el-option label="系统" :value="3" />
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
      </div>

      <el-table :data="rows" v-loading="loading" stripe style="margin-top: 16px;">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ fmtDt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="180">
          <template #default="{ row }">
            <el-tag size="small" :type="opTypeTag(row.operatorType)" effect="light" style="margin-right: 6px;">
              {{ row.operatorTypeLabel }}
            </el-tag>
            <span>{{ row.operatorName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="模块" prop="moduleLabel" width="120" />
        <el-table-column label="动作" prop="actionLabel" width="120" />
        <el-table-column label="对象 / 摘要" min-width="320">
          <template #default="{ row }">
            <span>{{ row.targetSummary || '—' }}</span>
            <span v-if="row.targetId" class="text-muted" style="margin-left: 8px; font-size: 12px;">
              #{{ row.targetId }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="IP" prop="ip" width="140" />
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[20, 30, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { listAuditLogs, type AuditLogRow } from '@/api/audit';

const filters = reactive<{ module: string; operatorType: number | ''; }>({
  module: '',
  operatorType: '' as any,
});
const dateRange = ref<[string, string] | null>(null);

const rows = ref<AuditLogRow[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(30);
const loading = ref(false);

async function load() {
  loading.value = true;
  try {
    const result = await listAuditLogs({
      module: filters.module || undefined,
      operatorType: filters.operatorType ? Number(filters.operatorType) : undefined,
      from: dateRange.value?.[0],
      to:   dateRange.value?.[1],
      page: page.value,
      size: size.value,
    });
    rows.value = result.rows;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function reload() { page.value = 1; load(); }

function fmtDt(s: string) { return s ? s.replace('T', ' ').slice(0, 19) : '—'; }
function opTypeTag(t: number) {
  return ({ 1: '', 2: 'success', 3: 'info' } as any)[t];
}

onMounted(load);
</script>

<style scoped>
.filters { display: flex; gap: 12px; }
.pager   { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
