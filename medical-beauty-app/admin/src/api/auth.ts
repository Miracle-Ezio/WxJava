import client from './client';

export interface LoginPayload { phone: string; password: string; }
export interface LoginResp {
  token: string;
  employeeId: number;
  name: string;
  avatarUrl: string;
  jobTitle: string;
  roleCode: string;
  storeId: number;
}
export interface MeResp {
  id: number;
  name: string;
  avatarUrl: string;
  jobTitle: string;
  roleCode: string;
  storeId: number;
}

export const login = (data: LoginPayload) =>
  client.post<LoginResp, LoginResp>('/api/admin/auth/login', data);

export const me = () => client.get<MeResp, MeResp>('/api/admin/auth/me');
