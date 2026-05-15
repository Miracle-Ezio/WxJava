import client from './client';

export interface Project {
  id: number;
  name: string;
  category: string;
  durationMin: number;
  unitPrice: string;
  description: string;
  coverUrl?: string;
}

export const listProjects = () =>
  client.get<Project[], Project[]>('/api/projects');
