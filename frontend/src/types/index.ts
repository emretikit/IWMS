export type Role = 'STUDENT' | 'SUPERVISOR' | 'COORDINATOR' | 'ADMIN';
export type ApiMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';
export type Session = { token: string; username: string; role: Role } | null;
export type MenuPanel = { key: string; label: string; description: string };

export type ApiRunner = (request: () => Promise<unknown>) => Promise<void>;
