import client from './client';

export interface ScheduleDay {
  date: string;
  storeId: number;
  storeName: string;
  capacity: number;
  businessHours: string;     // JSON
  appointments: any[];
}

export const getSchedule = (params: { storeId: number; date: string }) =>
  client.get<ScheduleDay, ScheduleDay>('/api/admin/schedule', { params });
