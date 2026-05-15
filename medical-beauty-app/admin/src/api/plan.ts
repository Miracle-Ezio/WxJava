import client from './client';

export interface PlanSection {
  id?: number;
  sort?: number;
  type: string;
  title: string;
  content?: string;
}
export interface PlanItem {
  id?: number;
  sectionId?: number;
  projectId: number;
  projectName?: string;
  plannedCount?: number;
  doneCount?: number;
  unitPrice?: string | number;
  activityPrice?: string | number;
  totalPrice?: string | number;
  status?: number;
  notes?: string;
}
export interface Plan {
  id?: number;
  customerId: number;
  title: string;
  subtitle?: string;
  coverUrl?: string;
  validFrom?: string;
  validTo?: string;
  totalPrice?: string | number;
  discountPrice?: string | number;
  analysisText?: string;
  status?: number;
  version?: number;
  pushedAt?: string;
}
export interface PlanDetail {
  plan: Plan;
  sections: PlanSection[];
  items: PlanItem[];
}
export interface PlanUpsert extends Plan {
  sections: PlanSection[];
  items: PlanItem[];
}

export const listPlansByCustomer = (customerId: number) =>
  client.get<Plan[], Plan[]>('/api/admin/plans', { params: { customerId } });

export const getPlan = (id: number) =>
  client.get<PlanDetail, PlanDetail>(`/api/admin/plans/${id}`);

export const createPlan = (data: PlanUpsert) =>
  client.post<Plan, Plan>('/api/admin/plans', data);

export const updatePlan = (id: number, data: PlanUpsert) =>
  client.put<Plan, Plan>(`/api/admin/plans/${id}`, data);

export const pushPlan = (id: number) =>
  client.post<Plan, Plan>(`/api/admin/plans/${id}/push`);

export const deletePlan = (id: number) =>
  client.delete<void, void>(`/api/admin/plans/${id}`);
