import { defineStore } from 'pinia';
import { login as apiLogin, me as apiMe, type LoginPayload, type MeResp } from '@/api/auth';
import { setToken, clearToken, getToken } from '@/api/client';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    me: null as MeResp | null,
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
  },
  actions: {
    async login(payload: LoginPayload) {
      const resp = await apiLogin(payload);
      setToken(resp.token);
      this.token = resp.token;
      this.me = {
        id: resp.employeeId,
        name: resp.name,
        avatarUrl: resp.avatarUrl,
        jobTitle: resp.jobTitle,
        roleCode: resp.roleCode,
        storeId: resp.storeId,
      };
    },
    async fetchMe() {
      if (!this.token) return;
      try {
        this.me = await apiMe();
      } catch {
        this.logout();
      }
    },
    logout() {
      clearToken();
      this.token = '';
      this.me = null;
    },
  },
});
