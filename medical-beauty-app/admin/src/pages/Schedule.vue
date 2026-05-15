<template>
  <div v-loading="loading">
    <div class="header">
      <h2 class="section-title" style="margin: 0;">排期日历</h2>
      <div style="display: flex; gap: 12px; align-items: center;">
        <el-button :icon="ArrowLeft" @click="shiftDay(-1)" />
        <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          @change="load"
        />
        <el-button :icon="ArrowRight" @click="shiftDay(+1)" />
        <el-button @click="date = today; load()">今天</el-button>
      </div>
    </div>

    <el-card v-if="schedule">
      <div class="meta">
        <div>
          <span class="text-secondary">{{ schedule.storeName }}</span>
          <el-tag style="margin-left: 8px;" effect="light">同时段并发上限 {{ schedule.capacity }}</el-tag>
        </div>
        <div class="text-secondary">{{ weekdayLabel }} · 共 {{ schedule.appointments.length }} 个预约</div>
      </div>

      <el-empty
        v-if="schedule.appointments.length === 0"
        description="当天没有预约"
        :image-size="80"
      />

      <div v-else class="timeline">
        <div class="timeline__hours">
          <div v-for="t in hourMarks" :key="t" class="hour-mark">{{ t }}</div>
        </div>
        <div class="timeline__lane" :style="{ height: laneHeight + 'px' }">
          <div
            v-for="(appt, idx) in laidOutAppts"
            :key="appt.id"
            class="appt-block"
            :style="appt.style"
            :data-status="appt.status"
            @click="selected = appt; drawerOpen = true"
          >
            <div class="appt-block__top">
              <strong>{{ appt.customerName || '—' }}</strong>
              <span class="text-secondary" style="font-size: 11px;">
                {{ hhmm(appt.startAt) }} - {{ hhmm(appt.endAt) }}
              </span>
            </div>
            <div class="appt-block__proj">{{ appt.projectName }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-drawer v-model="drawerOpen" title="预约详情" size="420px">
      <div v-if="selected">
        <h3 style="margin-top: 0;">{{ selected.projectName }}</h3>
        <el-descriptions :column="1" :label-style="{ color: '#8A8A8A' }">
          <el-descriptions-item label="客户">{{ selected.customerName }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ fmtDt(selected.startAt) }} ~ {{ hhmm(selected.endAt) }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ selected.durationMin }} 分钟</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(selected.status)" effect="light">{{ selected.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参考价格">¥ {{ selected.unitPrice || '—' }}</el-descriptions-item>
          <el-descriptions-item label="客户备注">{{ selected.customerNote || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="drawer-actions">
          <el-button v-if="selected.status === 1" type="primary" @click="actConfirm">确认预约</el-button>
          <el-button v-if="selected.status === 2" type="primary" @click="actCheckIn">客户到店</el-button>
          <el-button v-if="selected.status === 3" type="success" @click="actComplete">标记完成</el-button>
          <el-button v-if="[1,2].includes(selected.status)" @click="actCancel">取消预约</el-button>
          <el-button v-if="selected.status === 2" type="danger" plain @click="actNoShow">标记爽约</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getSchedule, type ScheduleDay } from '@/api/schedule';
import {
  confirmAppointment, checkInAppointment, completeAppointment,
  cancelAppointment, markNoShow,
} from '@/api/appointment';

const today = new Date().toISOString().slice(0, 10);
const date = ref(today);
const schedule = ref<ScheduleDay | null>(null);
const loading = ref(false);
const drawerOpen = ref(false);
const selected = ref<any>(null);

const STORE_ID = 1;     // 一期单门店，多店上线时改成下拉
const HOUR_HEIGHT = 80; // 每小时像素

async function load() {
  loading.value = true;
  try { schedule.value = await getSchedule({ storeId: STORE_ID, date: date.value }); }
  finally { loading.value = false; }
}

function shiftDay(d: number) {
  const dt = new Date(date.value);
  dt.setDate(dt.getDate() + d);
  date.value = dt.toISOString().slice(0, 10);
  load();
}

const hours = computed(() => {
  try {
    const map: Record<string, [string, string]> = schedule.value?.businessHours
      ? JSON.parse(schedule.value.businessHours) : {};
    const dow = ['sun','mon','tue','wed','thu','fri','sat'][new Date(date.value).getDay()];
    const range = map[dow] || ['10:00', '21:00'];
    return { open: range[0], close: range[1] };
  } catch { return { open: '10:00', close: '21:00' }; }
});

const hourMarks = computed(() => {
  const start = parseInt(hours.value.open.slice(0, 2), 10);
  const end = parseInt(hours.value.close.slice(0, 2), 10);
  const marks = [];
  for (let h = start; h <= end; h++) marks.push(`${String(h).padStart(2, '0')}:00`);
  return marks;
});

const laneHeight = computed(() => {
  const start = parseInt(hours.value.open.slice(0, 2), 10);
  const end = parseInt(hours.value.close.slice(0, 2), 10);
  return (end - start) * HOUR_HEIGHT;
});

const laidOutAppts = computed(() => {
  if (!schedule.value) return [];
  const startH = parseInt(hours.value.open.slice(0, 2), 10);
  const capacity = schedule.value.capacity || 1;

  // 简单按 startAt 排序后给每个预约分配一个"道（lane index）"，
  // 同一时间段最多 capacity 道
  type AnyAppt = any;
  const sorted: AnyAppt[] = [...schedule.value.appointments].sort(
    (a, b) => a.startAt.localeCompare(b.startAt));
  const laneEnd: number[] = []; // 每条道当前最晚结束时间（分钟）
  const result: any[] = [];

  for (const a of sorted) {
    const startMin = parseMinutes(a.startAt) - startH * 60;
    const endMin = parseMinutes(a.endAt) - startH * 60;
    let lane = laneEnd.findIndex(e => e <= startMin);
    if (lane === -1) {
      lane = Math.min(laneEnd.length, capacity - 1);
      if (laneEnd.length < capacity) laneEnd.push(endMin);
      else laneEnd[lane] = Math.max(laneEnd[lane], endMin);
    } else {
      laneEnd[lane] = endMin;
    }
    const top = (startMin / 60) * HOUR_HEIGHT;
    const height = Math.max(((endMin - startMin) / 60) * HOUR_HEIGHT - 4, 28);
    const widthPct = 100 / capacity;
    result.push({
      ...a,
      style: {
        top: top + 'px',
        height: height + 'px',
        left: `calc(${widthPct * lane}% + 4px)`,
        width: `calc(${widthPct}% - 8px)`,
      },
    });
  }
  return result;
});

function parseMinutes(iso: string) {
  const t = iso.split('T')[1] || '00:00';
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}
function hhmm(iso: string) { return iso ? iso.split('T')[1]?.slice(0, 5) : ''; }
function fmtDt(iso: string) { return iso ? iso.replace('T', ' ').slice(0, 16) : ''; }
function statusTag(s: number) {
  return ({ 1: 'warning', 2: 'success', 3: 'primary', 4: 'info' } as any)[s] || 'info';
}

const weekdayLabel = computed(() => {
  const w = ['周日','周一','周二','周三','周四','周五','周六'];
  return w[new Date(date.value).getDay()];
});

async function actConfirm() {
  await confirmAppointment(selected.value.id);
  ElMessage.success('已确认');
  drawerOpen.value = false;
  load();
}
async function actCheckIn() {
  await checkInAppointment(selected.value.id);
  ElMessage.success('客户已到店');
  drawerOpen.value = false;
  load();
}
async function actComplete() {
  await completeAppointment(selected.value.id);
  ElMessage.success('已完成');
  drawerOpen.value = false;
  load();
}
async function actCancel() {
  const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
    inputPlaceholder: '如：客户改期',
    inputType: 'textarea',
  }).catch(() => ({ value: null }));
  if (reason === null) return;
  await cancelAppointment(selected.value.id, reason || undefined);
  ElMessage.success('已取消');
  drawerOpen.value = false;
  load();
}
async function actNoShow() {
  await ElMessageBox.confirm('确认标记爽约？', '提示', { type: 'warning' });
  await markNoShow(selected.value.id);
  ElMessage.success('已标记爽约');
  drawerOpen.value = false;
  load();
}

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.timeline {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.timeline__hours {
  width: 60px;
  flex-shrink: 0;
  position: relative;
}
.hour-mark {
  height: 80px;
  font-size: 12px;
  color: #8A8A8A;
  border-top: 1px solid #ECECEC;
  padding-top: 4px;
  letter-spacing: 1px;
}
.hour-mark:first-child { border-top: none; }
.timeline__lane {
  flex: 1;
  position: relative;
  background:
    repeating-linear-gradient(to bottom, #FAFAFA 0, #FAFAFA 80px,
      #FFFFFF 80px, #FFFFFF 160px);
  border-radius: 8px;
  border: 1px solid #ECECEC;
}
.appt-block {
  position: absolute;
  background: #FFFFFF;
  border: 1px solid #ECECEC;
  border-left: 3px solid #0A0A0A;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(10,10,10,0.06);
  overflow: hidden;
  transition: transform 0.1s;
}
.appt-block:hover { transform: translateY(-1px); }
.appt-block[data-status="1"] { border-left-color: #C89A4A; }
.appt-block[data-status="2"] { border-left-color: #5B8A6F; }
.appt-block[data-status="3"] { border-left-color: #1B2541; }
.appt-block[data-status="4"] { border-left-color: #8A8A8A; }
.appt-block__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.appt-block__proj {
  font-size: 12px;
  color: #8A8A8A;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.drawer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ECECEC;
}
</style>
