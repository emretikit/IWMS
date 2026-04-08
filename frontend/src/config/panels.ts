import type { Session } from '../types';

type MenuPanel = { key: string; label: string };

export function getPanels(session: Session): MenuPanel[] {
  const common: MenuPanel[] = [
    { key: 'auth', label: 'Auth' },
    { key: 'support', label: 'Support' },
  ];

  if (!session) return common;

  if (session.role === 'STUDENT') {
    return [
      ...common,
      { key: 'application', label: 'Apply Internship' },
      { key: 'report', label: 'Report' },
      { key: 'history', label: 'History & Feedback' },
    ];
  }

  if (session.role === 'SUPERVISOR') {
    return [
      ...common,
      { key: 'company-eval', label: 'Company Evaluation' },
      { key: 'history', label: 'History & Feedback' },
    ];
  }

  if (session.role === 'COORDINATOR') {
    return [
      ...common,
      { key: 'coordinator', label: 'Coordinator Review' },
      { key: 'periods', label: 'Periods & Rules' },
    ];
  }

  return [
    ...common,
    { key: 'companies', label: 'Company Approval' },
    { key: 'admin-ops', label: 'Admin Ops' },
    { key: 'periods', label: 'Periods & Rules' },
  ];
}
