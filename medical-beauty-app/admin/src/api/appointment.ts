import client from './client';

export interface AdminAppointment {
  id: number;
  storeId: number;
  storeName: string;
  projectId: number;
  projectName: string;
  unitPrice: string;
  durationMin: number;
  startAt: string;
  endAt: string;
  status: number;
  statusLabel: string;
  source: number;
  customerName: string;
  customerNote: string;
  cancelReason: string;
}

export interface AppointmentListResult {
  rows: AdminAppointment[];
  total: number;
}

export const listAdminAppointments = (params: {
  status?: number;
  from?: string;
  to?: string;
  keyword?: string;
  page?: number;
  size?: number;
}) => client.get<AppointmentListResult, AppointmentListResult>(
  '/api/admin/appointments', { params });

export const confirmAppointment = (id: number, staffNote?: string) =>
  client.post<AdminAppointment, AdminAppointment>(`/api/admin/appointments/${id}/confirm`, { staffNote });

export const checkInAppointment = (id: number) =>
  client.post<AdminAppointment, AdminAppointment>(`/api/admin/appointments/${id}/check-in`);

export const completeAppointment = (id: number, staffNote?: string) =>
  client.post<AdminAppointment, AdminAppointment>(`/api/admin/appointments/${id}/complete`, { staffNote });

export const cancelAppointment = (id: number, reason?: string) =>
  client.post<AdminAppointment, AdminAppointment>(`/api/admin/appointments/${id}/cancel`, { reason });

export const markNoShow = (id: number) =>
  client.post<AdminAppointment, AdminAppointment>(`/api/admin/appointments/${id}/no-show`);

export const STATUS_LABEL: Record<number, string> = {
  1: '待确认', 2: '已确认', 3: '已到店', 4: '已完成',
  5: '客户取消', 6: '机构取消', 7: '未到',
};

export const STATUS_TAG: Record<number, string> = {
  1: 'warning', 2: 'success', 3: 'primary',
  4: 'info', 5: 'info', 6: 'info', 7: 'danger',
};
