<template>
  <div class="editor">
    <!-- 顶栏：标题 + 状态 + 操作 -->
    <el-card class="head-card">
      <div class="head-row">
        <div>
          <div class="text-secondary" style="font-size: 12px;">为 <b>{{ customerLabel }}</b> 制作规划方案</div>
          <el-input
            v-model="form.title"
            placeholder="请输入方案标题，如：抗衰整体规划方案"
            style="width: 480px; margin-top: 8px;"
            size="large"
          />
        </div>
        <div class="head-actions">
          <el-tag v-if="planId" :type="statusTag(form.status)" effect="light" style="margin-right: 12px;">
            {{ statusLabel(form.status) }}
          </el-tag>
          <el-button @click="onSaveDraft" :loading="saving">保存草稿</el-button>
          <el-button class="btn-gold" :loading="pushing" @click="onPush" v-if="planId && (form.status || 1) < 2">
            <el-icon><Position /></el-icon>推送给客户
          </el-button>
        </div>
      </div>

      <el-row :gutter="16" style="margin-top: 16px;">
        <el-col :span="6">
          <el-input v-model="form.subtitle" placeholder="副标题，如：面部抗衰 · 皮肤管理" />
        </el-col>
        <el-col :span="5">
          <el-date-picker v-model="form.validFrom" type="date" placeholder="开始日期" style="width: 100%;" value-format="YYYY-MM-DD" />
        </el-col>
        <el-col :span="5">
          <el-date-picker v-model="form.validTo"   type="date" placeholder="结束日期" style="width: 100%;" value-format="YYYY-MM-DD" />
        </el-col>
        <el-col :span="4">
          <el-input v-model="form.totalPrice" placeholder="原价 ¥" />
        </el-col>
        <el-col :span="4">
          <el-input v-model="form.discountPrice" placeholder="活动价 ¥" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 状况分析 -->
    <el-card>
      <template #header><span class="card-title">📋 面部状况分析</span></template>
      <el-input
        v-model="form.analysisText"
        type="textarea"
        :rows="6"
        placeholder="支持 Markdown：## 优势 / ## 劣势 ..."
      />
    </el-card>

    <!-- 章节区 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="card-title">📑 规划章节</span>
          <el-dropdown @command="addSection">
            <el-button>
              + 添加章节<el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="region_plan">分区规划（如 T 区轮廓固定）</el-dropdown-item>
                <el-dropdown-item command="project_list">项目建议</el-dropdown-item>
                <el-dropdown-item command="material">抗衰材料推荐</el-dropdown-item>
                <el-dropdown-item command="case">案例参考</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>

      <el-empty v-if="form.sections.length === 0" description="还没有章节，点击右上方「+ 添加章节」开始" />

      <div v-for="(sec, idx) in form.sections" :key="idx" class="section-block">
        <div class="section-block__head">
          <el-tag size="small" effect="light" style="margin-right: 8px;">{{ sectionTypeLabel(sec.type) }}</el-tag>
          <el-input v-model="sec.title" placeholder="章节标题" style="flex: 1;" />
          <el-button-group>
            <el-button :icon="Top" :disabled="idx === 0"           @click="moveSection(idx, -1)" />
            <el-button :icon="Bottom" :disabled="idx === form.sections.length - 1" @click="moveSection(idx, +1)" />
            <el-button :icon="Delete" type="danger" plain          @click="removeSection(idx)" />
          </el-button-group>
        </div>
        <el-input
          v-model="sec.content"
          type="textarea"
          :rows="4"
          placeholder="章节内容，支持 Markdown 列表 / 加粗"
          style="margin-top: 12px;"
        />
      </div>
    </el-card>

    <!-- 项目清单（套餐报价） -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="card-title">💎 套餐报价</span>
          <el-button :icon="Plus" @click="pickerVisible = true">+ 添加项目</el-button>
        </div>
      </template>

      <el-empty v-if="form.items.length === 0" description="尚未添加项目" />

      <el-table v-else :data="form.items" stripe>
        <el-table-column label="项目" min-width="200">
          <template #default="{ row }">
            <div style="font-weight: 500;">{{ row.projectName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="次数" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.plannedCount" :min="1" :max="50" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="单价 ¥" width="140">
          <template #default="{ row }">
            <el-input v-model="row.unitPrice" size="small" @change="recalcRow(row)" />
          </template>
        </el-table-column>
        <el-table-column label="活动价 ¥" width="140">
          <template #default="{ row }">
            <el-input v-model="row.activityPrice" size="small" @change="recalcRow(row)" />
          </template>
        </el-table-column>
        <el-table-column label="小计 ¥" width="160">
          <template #default="{ row }">
            <span class="text-gold" style="font-weight: 600;">¥{{ row.totalPrice || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column width="80" align="right">
          <template #default="{ $index }">
            <el-button :icon="Delete" type="danger" plain size="small" @click="form.items.splice($index, 1)" />
          </template>
        </el-table-column>
      </el-table>

      <div v-if="form.items.length" class="totals">
        <span class="text-secondary">合计</span>
        <span class="text-gold" style="font-size: 22px; font-weight: 600; margin-left: 16px;">¥{{ itemsTotal.toFixed(2) }}</span>
      </div>
    </el-card>

    <!-- 项目选择器 -->
    <el-dialog v-model="pickerVisible" title="选择项目" width="640px">
      <el-table :data="projects" @row-click="addItem" highlight-current-row style="cursor: pointer;">
        <el-table-column label="项目" prop="name" min-width="200" />
        <el-table-column label="分类" prop="category" width="100" />
        <el-table-column label="时长" width="100">
          <template #default="{ row }">{{ row.durationMin }} 分钟</template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Top, Bottom, Delete, ArrowDown, Position } from '@element-plus/icons-vue';
import { listProjects, type Project } from '@/api/project';
import { getCustomer } from '@/api/customer';
import {
  createPlan, updatePlan, getPlan, pushPlan,
  type PlanSection, type PlanItem,
} from '@/api/plan';

const route = useRoute();
const router = useRouter();

const planId = ref<number | null>(null);
const customerLabel = ref('—');
const projects = ref<Project[]>([]);
const pickerVisible = ref(false);
const saving = ref(false);
const pushing = ref(false);

const form = reactive({
  customerId: 0,
  title: '',
  subtitle: '',
  validFrom: '',
  validTo: '',
  totalPrice: '',
  discountPrice: '',
  analysisText: '',
  status: 1,
  sections: [] as PlanSection[],
  items: [] as PlanItem[],
});

const itemsTotal = computed(() => form.items.reduce(
  (sum, it) => sum + Number(it.totalPrice || 0), 0));

watch(itemsTotal, (v) => { if (!form.totalPrice) form.totalPrice = v.toFixed(2); });

// ─────────────── 加载 ───────────────
onMounted(async () => {
  projects.value = await listProjects();

  if (route.name === 'PlanEdit') {
    planId.value = Number(route.params.id);
    const detail = await getPlan(planId.value);
    Object.assign(form, {
      customerId:     detail.plan.customerId,
      title:          detail.plan.title,
      subtitle:       detail.plan.subtitle || '',
      validFrom:      detail.plan.validFrom || '',
      validTo:        detail.plan.validTo || '',
      totalPrice:     detail.plan.totalPrice || '',
      discountPrice:  detail.plan.discountPrice || '',
      analysisText:   detail.plan.analysisText || '',
      status:         detail.plan.status || 1,
      sections:       detail.sections,
      items:          detail.items,
    });
    await syncCustomerLabel(detail.plan.customerId);
  } else {
    const cid = Number(route.query.customerId);
    if (cid) {
      form.customerId = cid;
      await syncCustomerLabel(cid);
    }
  }
});

async function syncCustomerLabel(cid: number) {
  try {
    const d = await getCustomer(cid);
    customerLabel.value = d.customer.realName || d.customer.nickname || `#${cid}`;
  } catch { customerLabel.value = `#${cid}`; }
}

// ─────────────── 章节 ───────────────
function addSection(type: string) {
  const titleMap: Record<string, string> = {
    region_plan:  '分区规划',
    project_list: '项目建议',
    material:     '抗衰材料推荐',
    case:         '案例参考',
  };
  form.sections.push({
    type, title: titleMap[type] || '新章节', content: '', sort: form.sections.length,
  });
}
function moveSection(idx: number, dir: number) {
  const target = idx + dir;
  if (target < 0 || target >= form.sections.length) return;
  [form.sections[idx], form.sections[target]] = [form.sections[target], form.sections[idx]];
}
function removeSection(idx: number) {
  ElMessageBox.confirm('确认删除这个章节？', '提示').then(() => form.sections.splice(idx, 1));
}

// ─────────────── 项目 ───────────────
function addItem(p: Project) {
  form.items.push({
    projectId:    p.id,
    projectName:  p.name,
    plannedCount: 1,
    unitPrice:    p.unitPrice,
    activityPrice: p.unitPrice,
    totalPrice:   p.unitPrice,
  });
  pickerVisible.value = false;
}
function recalcRow(row: PlanItem) {
  const price = Number(row.activityPrice || row.unitPrice || 0);
  row.totalPrice = (price * (row.plannedCount || 1)).toFixed(2);
}

// ─────────────── 保存 ───────────────
function buildPayload() {
  // 自动重算每行小计
  form.items.forEach(recalcRow);
  return { ...form };
}

async function onSaveDraft() {
  if (!form.customerId) { ElMessage.warning('缺少客户'); return; }
  if (!form.title)      { ElMessage.warning('请输入标题'); return; }

  saving.value = true;
  try {
    if (planId.value) {
      await updatePlan(planId.value, buildPayload() as any);
      ElMessage.success('已保存');
    } else {
      const created = await createPlan(buildPayload() as any);
      planId.value = created.id!;
      ElMessage.success('草稿已创建');
      router.replace(`/plans/${created.id}`);
    }
  } finally {
    saving.value = false;
  }
}

async function onPush() {
  await ElMessageBox.confirm(
    '推送后客户即可在小程序看到本方案，确认推送？',
    '推送给客户',
    { type: 'warning' });

  pushing.value = true;
  try {
    if (!planId.value) await onSaveDraft();
    const updated = await pushPlan(planId.value!);
    form.status = updated.status!;
    ElMessage.success('已推送给客户 ✨');
  } finally {
    pushing.value = false;
  }
}

// ─────────────── 工具 ───────────────
function statusLabel(s?: number) {
  return ({ 1: '草稿', 2: '已推送', 3: '已签约', 4: '执行中', 5: '已完成', 6: '已作废' } as any)[s || 1];
}
function statusTag(s?: number) {
  return ({ 1: 'info', 2: 'warning', 3: 'success', 4: 'primary', 5: 'success', 6: 'danger' } as any)[s || 1];
}
function sectionTypeLabel(t: string) {
  return ({
    region_plan: '分区规划', project_list: '项目建议',
    material: '材料', case: '案例', analysis: '分析', package: '套餐',
  } as any)[t] || t;
}
</script>

<style scoped>
.editor > .el-card { margin-bottom: 16px; }
.head-card {
  background: linear-gradient(180deg, #FFFFFF 0%, #FAF9F6 100%);
}
.head-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.head-actions { display: flex; align-items: center; }
.card-title { font-weight: 600; letter-spacing: 1px; }

.section-block {
  padding: 16px;
  background: #FAFAFA;
  border-radius: 8px;
  margin-bottom: 12px;
}
.section-block__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.totals {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ECECEC;
  text-align: right;
}
</style>
