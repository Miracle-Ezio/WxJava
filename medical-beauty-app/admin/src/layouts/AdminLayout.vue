<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        <div class="brand__mono">SR</div>
        <div class="brand__name">STARRY</div>
        <div class="brand__zh">思达芮 · 顾问后台</div>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="menu"
        background-color="transparent"
        text-color="#4A4A4A"
        active-text-color="#0A0A0A"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/customers">
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <span>排期日历</span>
        </el-menu-item>
        <el-menu-item index="/appointments">
          <el-icon><Tickets /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/audit-logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar__crumbs">
          <el-icon><HomeFilled /></el-icon>
          <span class="text-secondary" style="margin-left: 6px;">{{ pageTitle }}</span>
        </div>
        <el-dropdown @command="onCommand">
          <span class="topbar__user">
            <el-avatar :size="32" style="background: #0A0A0A">{{ avatarText }}</el-avatar>
            <span style="margin-left: 8px;">{{ auth.me?.name || '加载中…' }}</span>
            <el-tag v-if="auth.me?.jobTitle" size="small" style="margin-left: 8px;">{{ auth.me.jobTitle }}</el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

onMounted(() => { if (!auth.me) auth.fetchMe(); });

const avatarText = computed(() => (auth.me?.name || 'S')[0]);
const pageTitle = computed(() => {
  const t: Record<string, string> = {
    Customers: '客户管理',
    CustomerDetail: '客户详情',
    PlanNew: '新建规划方案',
    PlanEdit: '编辑规划方案',
    Dashboard: '数据看板',
    Schedule: '排期日历',
    Appointments: '预约管理',
    AuditLogs: '操作日志',
  };
  return t[String(route.name)] || '';
});

function onCommand(c: string) {
  if (c === 'logout') { auth.logout(); router.push('/login'); }
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar {
  background: #FFFFFF;
  border-right: 1px solid #ECECEC;
  padding: 0;
}
.brand {
  padding: 32px 24px 24px;
  border-bottom: 1px solid #ECECEC;
}
.brand__mono {
  font-family: "Times New Roman", serif;
  font-size: 48px;
  letter-spacing: -4px;
  color: #0A0A0A;
  line-height: 1;
}
.brand__name {
  margin-top: 8px;
  font-family: "Times New Roman", serif;
  font-size: 20px;
  letter-spacing: 6px;
}
.brand__zh {
  margin-top: 4px;
  font-size: 12px;
  color: #8A8A8A;
  letter-spacing: 2px;
}
.menu { border-right: none; padding: 16px 8px; }
:deep(.el-menu-item) { border-radius: 8px; margin-bottom: 4px; }
:deep(.el-menu-item.is-active) { background: #FAF9F6; font-weight: 500; }

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FFFFFF;
  border-bottom: 1px solid #ECECEC;
}
.topbar__user { display: flex; align-items: center; cursor: pointer; }

.main { background: #FAF9F6; padding: 24px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
