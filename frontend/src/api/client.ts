import axios from 'axios';
import type { ApiErrorBody } from '../types';

export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1', headers: { 'Content-Type': 'application/json' } });
api.interceptors.request.use(config => { const token = localStorage.getItem('sporthub_token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config; });
api.interceptors.response.use(response => response, error => { if (error.response?.status === 401) { localStorage.removeItem('sporthub_token'); localStorage.removeItem('sporthub_user'); window.dispatchEvent(new Event('sporthub:unauthorized')); } return Promise.reject(error); });
export function errorMessage(error: unknown): string { const body = (error as { response?: { data?: ApiErrorBody } })?.response?.data; return body?.fieldErrors?.map(e => `${e.field ? `${e.field}: ` : ''}${e.message}`).filter(Boolean).join('. ') || body?.message || (error instanceof Error ? error.message : 'Something went wrong. Please try again.'); }
export function listOf<T>(data: unknown): T[] { if (Array.isArray(data)) return data as T[]; if (data && typeof data === 'object') { const d = data as Record<string, unknown>; for (const key of ['content','items','data','results']) if (Array.isArray(d[key])) return d[key] as T[]; } return []; }
