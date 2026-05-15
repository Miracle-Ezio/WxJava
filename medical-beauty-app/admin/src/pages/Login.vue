<template>
  <div class="login">
    <div class="login__card">
      <div class="login__brand">
        <div class="login__mono">SR</div>
        <div class="login__name">STARRY</div>
        <div class="login__zh">思达芮 · 顾问后台</div>
        <div class="login__tagline">天空中闪耀的繁星</div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="13800000001" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="••••••" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          style="width: 100%; height: 44px; letter-spacing: 4px;"
          @click="onSubmit"
        >登 录</el-button>
      </el-form>

      <div class="login__hint">
        演示账号 13800000001 / starry123
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, type FormInstance } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const loading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({ phone: '', password: '' });
const rules = {
  phone:    [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function onSubmit() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await auth.login(form);
    ElMessage.success('欢迎回来');
    const redirect = (route.query.redirect as string) || '/customers';
    router.push(redirect);
  } catch (e) {
    // ElMessage.error 已由 client 拦截
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFFFFF 0%, #FAF9F6 100%);
}
.login__card {
  width: 420px;
  padding: 56px 48px 48px;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 16px 64px rgba(10, 10, 10, 0.08);
}
.login__brand { text-align: center; margin-bottom: 40px; }
.login__mono {
  font-family: "Times New Roman", serif;
  font-size: 64px;
  letter-spacing: -6px;
  color: #0A0A0A;
  line-height: 1;
}
.login__name {
  margin-top: 12px;
  font-family: "Times New Roman", serif;
  font-size: 28px;
  letter-spacing: 8px;
}
.login__zh {
  margin-top: 4px;
  font-size: 14px;
  color: #8A8A8A;
  letter-spacing: 6px;
}
.login__tagline {
  margin-top: 24px;
  font-size: 12px;
  color: #B8B8B8;
  letter-spacing: 2px;
}
.login__hint {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #B8B8B8;
}
</style>
