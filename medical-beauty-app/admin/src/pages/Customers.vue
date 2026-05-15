<template>
  <div>
    <div class="header">
      <h2 class="section-title" style="margin: 0;">客户管理</h2>
      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索昵称 / 姓名"
          style="width: 280px;"
          clearable
          @keyup.enter="load"
          @clear="load"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="levelCode" placeholder="所有等级" clearable style="width: 160px;" @change="load">
          <el-option label="星尘"   value="STARDUST" />
          <el-option label="银河"   value="SILVER" />
          <el-option label="星辰（金卡）" value="GOLD" />
          <el-option label="夜空（黑卡）" value="NIGHT" />
          <el-option label="新星（钻石）" value="NOVA" />
        </el-select>
      </div>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe @row-click="onRow" highlight-current-row>
        <el-table-column label="客户" min-width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="36" :src="row.avatarUrl" style="background: #ECECEC; margin-right: 12px;">
                {{ (row.realName || row.nickname || 'C')[0] }}
              </el-avatar>
              <div>
                <div style="font-weight: 500;">{{ row.realName || row.nickname || '—' }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ row.nickname }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="120">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.levelCode)" effect="light">
              {{ levelLabel(row.levelCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="顾问" prop="consultantName" width="120" />
        <el-table-column label="累计充值" width="140">
          <template #default="{ row }">
            <span class="text-gold" style="font-weight: 500;">¥{{ row.totalRecharge || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="余额" prop="balance" width="120">
          <template #default="{ row }">¥{{ row.balance || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="最近到店" width="180">
          <template #default="{ row }">
            <span class="text-secondary">{{ formatDt(row.lastVisitAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="right">
          <template #default="{ row }">
            <el-button text type="primary" @click.stop="onPlan(row.id)">
              <el-icon><Document /></el-icon>制作规划
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { listCustomers, type CustomerSummary } from '@/api/customer';

const router = useRouter();
const list = ref<CustomerSummary[]>([]);
const loading = ref(false);
const keyword = ref('');
const levelCode = ref('');

async function load() {
  loading.value = true;
  try {
    list.value = await listCustomers({ keyword: keyword.value, levelCode: levelCode.value });
  } finally {
    loading.value = false;
  }
}

function onRow(row: CustomerSummary) {
  router.push(`/customers/${row.id}`);
}

function onPlan(customerId: number) {
  router.push({ path: '/plans/new', query: { customerId } });
}

function levelLabel(code: string) {
  return ({ STARDUST: '星尘', SILVER: '银河', GOLD: '星辰', NIGHT: '夜空', NOVA: '新星' } as any)[code] || code;
}
function levelTagType(code: string) {
  return ({ STARDUST: 'info', SILVER: 'info', GOLD: 'warning', NIGHT: 'danger', NOVA: 'primary' } as any)[code] || 'info';
}
function formatDt(s: string) {
  return s ? s.replace('T', ' ').slice(0, 16) : '—';
}

onMounted(load);
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.actions { display: flex; gap: 12px; }
</style>
