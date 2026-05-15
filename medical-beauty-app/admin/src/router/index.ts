import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/pages/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '',                  redirect: '/customers' },
      { path: 'dashboard',         name: 'Dashboard',     component: () => import('@/pages/Dashboard.vue') },
      { path: 'customers',         name: 'Customers',     component: () => import('@/pages/Customers.vue') },
      { path: 'customers/:id',     name: 'CustomerDetail', component: () => import('@/pages/CustomerDetail.vue') },
      { path: 'plans/new',         name: 'PlanNew',       component: () => import('@/pages/PlanEditor.vue') },
      { path: 'plans/:id',         name: 'PlanEdit',      component: () => import('@/pages/PlanEditor.vue') },
    ],
  },
];

const router = createRouter({ history: createWebHashHistory(), routes });

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (!to.meta.public && !auth.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
  if (to.name === 'Login' && auth.isLoggedIn) {
    return { name: 'Customers' };
  }
});

export default router;
