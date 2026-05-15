import client from './client';

export interface AuditLogRow {
  id: number;
  operatorType: number;
  operatorTypeLabel: string;
  operatorId: number;
  operatorName: string;
  module: string;
  moduleLabel: string;
  action: string;
  actionLabel: string;
  targetId: number;
  targetSummary: string;
  ip: string;
  createdAt: string;
}

export interface AuditLogPage {
  rows: AuditLogRow[];
  total: number;
  page: number;
  size: number;
}

export const listAuditLogs = (params: {
  module?: string;
  action?: string;
  operatorType?: number;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}) => client.get<AuditLogPage, AuditLogPage>('/api/admin/audit-logs', { params });
