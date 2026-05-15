<template>
  <div v-if="customer">
    <el-page-header @back="$router.back()" :title="' '">
      <template #content>
        <span style="font-weight: 500;">{{ customer.realName || customer.nickname }}</span>
        <el-tag style="margin-left: 12px;" effect="light">{{ levelLabel(customer.levelCode) }}</el-tag>
      </template>
    </el-page-header>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="8">
        <el-card>
          <div style="display: flex; flex-direction: column; align-items: center; padding: 16px 0;">
            <el-avatar :size="80" :src="customer.avatarUrl" style="background: #ECECEC;">
              {{ (customer.realName || customer.nickname || 'C')[0] }}
            </el-avatar>
            <div style="margin-top: 12px; font-size: 18px; font-weight: 600;">
              {{ customer.realName || customer.nickname }}
            </div>
            <div class="text-secondary" style="margin-top: 4px;">{{ customer.nickname }}</div>
          </div>
          <el-divider />
          <el-descriptions :column="1" :label-style="{ color: '#8A8A8A' }">
            <el-descriptions-item label="性别">{{ ['未知','女','男'][customer.gender || 0] }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ customer.birthday || '—' }}</el-descriptions-item>
            <el-descriptions-item label="主顾问">{{ consultantName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="累计充值">
              <span class="text-gold" style="font-weight: 500;">¥{{ customer.totalRecharge || '0.00' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="余额">¥{{ customer.balance || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="首次到店">{{ formatDt(customer.firstVisitAt) }}</el-descriptions-item>
            <el-descriptions-item label="最近到店">{{ formatDt(customer.lastVisitAt) }}</el-descriptions-item>
            <el-descriptions-item label="过敏史">{{ customer.allergy || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: 600; letter-spacing: 1px;">规划方案</span>
              <el-button type="primary" :icon="Plus" @click="newPlan">新建规划</el-button>
            </div>
          </template>

          <el-empty v-if="plans.length === 0" description="暂无规划方案" />

          <el-table v-else :data="plans" @row-click="onPlanRow" style="cursor: pointer;">
            <el-table-column label="标题" min-width="280">
              <template #default="{ row }">
                <div style="font-weight: 500;">{{ row.title }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ row.subtitle }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="info">草稿</el-tag>
                <el-tag v-else-if="row.status === 2" type="warning">已推送</el-tag>
                <el-tag v-else-if="row.status === 3" type="success">已签约</el-tag>
                <el-tag v-else-if="row.status === 4">执行中</el-tag>
                <el-tag v-else-if="row.status === 5" type="success">已完成</el-tag>
                <el-tag v-else type="danger">已作废</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="140">
              <template #default="{ row }">
                <span class="text-gold">¥{{ row.discountPrice || row.totalPrice || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="推送时间" width="180">
              <template #default="{ row }">{{ formatDt(row.pushedAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Plus } from '@element-plus/icons-vue';
import { getCustomer } from '@/api/customer';
import { listPlansByCustomer, type Plan } from '@/api/plan';

const route = useRoute();
const router = useRouter();
const customer = ref<any>(null);
const consultantName = ref('');
const plans = ref<Plan[]>([]);

async function load() {
  const id = Number(route.params.id);
  const detail = await getCustomer(id);
  customer.value = detail.customer;
  consultantName.value = detail.consultantName;
  plans.value = await listPlansByCustomer(id);
}

function newPlan() {
  router.push({ path: '/plans/new', query: { customerId: route.params.id } });
}

function onPlanRow(row: Plan) {
  router.push(`/plans/${row.id}`);
}

function levelLabel(code: string) {
  return ({ STARDUST: '星尘', SILVER: '银河', GOLD: '星辰', NIGHT: '夜空', NOVA: '新星' } as any)[code] || code;
}
function formatDt(s: string) { return s ? s.replace('T', ' ').slice(0, 16) : '—'; }

onMounted(load);
</script>
