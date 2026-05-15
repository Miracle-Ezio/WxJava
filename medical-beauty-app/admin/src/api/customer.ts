import client from './client';

export interface CustomerSummary {
  id: number;
  nickname: string;
  avatarUrl: string;
  realName: string;
  gender: number;
  levelCode: string;
  totalRecharge: string;
  balance: string;
  lastVisitAt: string;
  firstVisitAt: string;
  consultantName: string;
}

export interface CustomerDetail {
  customer: any;
  consultantName: string;
}

export const listCustomers = (params: { keyword?: string; levelCode?: string }) =>
  client.get<CustomerSummary[], CustomerSummary[]>('/api/admin/customers', { params });

export const getCustomer = (id: number) =>
  client.get<CustomerDetail, CustomerDetail>(`/api/admin/customers/${id}`);
