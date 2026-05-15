import client from './client';

export interface DayPoint { date: string; count: number; }

export interface DashboardSummary {
  todayAppointments: number;
  todayRevenue: string;
  todayNewCustomers: number;
  monthlyNewCustomers: number;
  monthlyPlansPushed: number;
  monthlyRevenue: string;
  totalCustomers: number;
  upcomingAppointments: number;
  appointmentTrend: DayPoint[];
  recentAppointments: any[];
  upcomingReminders: any[];
}

export const getDashboardSummary = () =>
  client.get<DashboardSummary, DashboardSummary>('/api/admin/dashboard/summary');
