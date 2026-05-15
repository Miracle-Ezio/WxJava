import axios, { AxiosError } from 'axios';
import { ElMessage } from 'element-plus';

const TOKEN_KEY = 'starry-admin-token';

export const getToken = () => localStorage.getItem(TOKEN_KEY) || '';
export const setToken = (t: string) => localStorage.setItem(TOKEN_KEY, t);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

const client = axios.create({
  baseURL: '',
  timeout: 15000,
});

client.interceptors.request.use(cfg => {
  const t = getToken();
  if (t) cfg.headers.Authorization = `Bearer ${t}`;
  return cfg;
});

client.interceptors.response.use(
  res => {
    const body = res.data || {};
    if (body.code === 0) return body.data;
    if (body.code === 40100 || body.code === 40101) {
      clearToken();
      window.location.hash = '#/login';
    }
    ElMessage.error(body.message || `请求失败 (${body.code})`);
    return Promise.reject(body);
  },
  (err: AxiosError) => {
    ElMessage.error('网络异常');
    return Promise.reject(err);
  },
);

export default client;
