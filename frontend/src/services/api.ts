import type { ApiMethod } from '../types';

export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export async function apiCall(path: string, method: ApiMethod, token?: string, body?: unknown) {
  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    let errorMessage = data?.message ?? 'Request failed';
    if (data?.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
      const fieldErrors = Object.entries(data.data)
        .map(([field, error]) => `${field}: ${error}`)
        .join(', ');
      if (fieldErrors) {
        errorMessage += ` (${fieldErrors})`;
      }
    }
    throw new Error(errorMessage);
  }
  return data;
}

export async function multipartApiCall(path: string, method: ApiMethod, formData: FormData, token?: string) {
  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData,
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    let errorMessage = data?.message ?? 'Request failed';
    if (data?.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
      const fieldErrors = Object.entries(data.data)
        .map(([field, error]) => `${field}: ${error}`)
        .join(', ');
      if (fieldErrors) {
        errorMessage += ` (${fieldErrors})`;
      }
    }
    throw new Error(errorMessage);
  }
  return data;
}
