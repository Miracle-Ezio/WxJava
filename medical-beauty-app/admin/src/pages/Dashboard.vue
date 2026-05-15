<template>
  <div v-loading="loading">
    <h2 class="section-title" style="margin: 0 0 24px;">数据看板</h2>

    <!-- KPI 卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="kpi">
          <div class="kpi__label">今日预约</div>
          <div class="kpi__num">{{ data?.todayAppointments ?? '—' }}</div>
          <div class="kpi__sub">未来 7 天 <b>{{ data?.upcomingAppointments ?? '—' }}</b> 个</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi kpi--gold">
          <div class="kpi__label">今日营业额</div>
          <div class="kpi__num">¥ {{ formatMoney(data?.todayRevenue) }}</div>
          <div class="kpi__sub">本月累计 <b>¥ {{ formatMoney(data?.monthlyRevenue) }}</b></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi">
          <div class="kpi__label">客户总数</div>
          <div class="kpi__num">{{ data?.totalCustomers ?? '—' }}</div>
          <div class="kpi__sub">本月新增 <b>{{ data?.monthlyNewCustomers ?? 0 }}</b></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi">
          <div class="kpi__label">本月推送规划</div>
          <div class="kpi__num">{{ data?.monthlyPlansPushed ?? '—' }}</div>
          <div class="kpi__sub">替代 PPT 的力量 ✨</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约趋势 -->
    <el-card style="margin-top: 16px;">
      <template #header><span class="card-title">📈 近 14 天预约趋势</span></template>
      <v-chart class="chart" :option="chartOption" autoresize />
    </el-card>

    <el-row :gutter="16" style="margin-top: 16px;">
      <!-- 未来 24h 提醒 -->
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">🔔 未来 24 小时</span></template>
          <el-empty v-if="!data?.upcomingReminders?.length" description="暂无待到店预约" />
          <div v-for="a in data?.upcomingReminders || []" :key="a.id" class="appt-row">
            <div>
              <div class="appt-row__name">{{ a.customerName || '—' }} · {{ a.projectName }}</div>
              <div class="text-secondary" style="font-size: 12px;">{{ fmtDt(a.startAt) }} · {{ a.durationMin }} 分钟</div>
            </div>
            <el-tag :type="statusTag(a.status)" effect="light">{{ a.statusLabel }}</el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 最近预约 -->
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">🕐 最近预约</span></template>
          <el-empty v-if="!data?.recentAppointments?.length" description="还没有预约" />
          <div v-for="a in data?.recentAppointments || []" :key="a.id" class="appt-row">
            <div>
              <div class="appt-row__name">{{ a.customerName || '—' }} · {{ a.projectName }}</div>
              <div class="text-secondary" style="font-size: 12px;">{{ fmtDt(a.startAt) }}</div>
            </div>
            <el-tag :type="statusTag(a.status)" effect="light">{{ a.statusLabel }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, TitleComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import { getDashboardSummary, type DashboardSummary } from '@/api/dashboard';

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, TitleComponent]);

const data = ref<DashboardSummary | null>(null);
const loading = ref(true);

onMounted(async () => {
  try { data.value = await getDashboardSummary(); }
  finally { loading.value = false; }
});

const chartOption = computed(() => {
  const points = data.value?.appointmentTrend || [];
  return {
    grid: { top: 20, right: 16, bottom: 24, left: 32 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: points.map(p => p.date.slice(5)),
      axisLine: { lineStyle: { color: '#D8D8D8' } },
      axisLabel: { color: '#8A8A8A' },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#ECECEC' } },
      axisLabel: { color: '#8A8A8A' },
    },
    series: [{
      type: 'bar',
      data: points.map(p => p.count),
      itemStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                 colorStops: [{ offset: 0, color: '#D8BC82' }, { offset: 1, color: '#B8995A' }] },
        borderRadius: [4, 4, 0, 0],
      },
      barWidth: '60%',
    }],
  };
});

function formatMoney(s: string | undefined | null) {
  if (s == null || s === '') return '0.00';
  return Number(s).toFixed(2);
}
function fmtDt(s: string) { return s ? s.replace('T', ' ').slice(0, 16) : '—'; }
function statusTag(s: number) {
  return ({ 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'info', 6: 'info', 7: 'danger' } as any)[s] || 'info';
}
</script>

<style scoped>
.card-title { font-weight: 600; letter-spacing: 1px; }
.kpi {
  height: 130px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}
.kpi__label { font-size: 12px; color: #8A8A8A; letter-spacing: 2px; }
.kpi__num   { font-family: "Times New Roman", serif; font-size: 36px; font-weight: 600; margin: 4px 0; }
.kpi__sub   { font-size: 12px; color: #8A8A8A; }
.kpi--gold .kpi__num { color: #B8995A; }

.chart { height: 260px; }

.appt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ECECEC;
}
.appt-row:last-child { border-bottom: none; }
.appt-row__name { font-weight: 500; }
</style>
