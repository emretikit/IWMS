import type { MenuPanel, Session } from '../types';

export function getPanels(session: Session): MenuPanel[] {
  const common: MenuPanel[] = [
    { key: 'auth', label: 'Welcome', description: 'Secure sign-in and system overview' },
    { key: 'support', label: 'Support', description: 'FAQ, chatbot and guided help' },
  ];

  if (!session) return common;

  if (session.role === 'STUDENT') {
    return [
      { key: 'application', label: 'Application Hub', description: 'Start and track internship applications' },
      { key: 'report', label: 'Report Studio', description: 'Draft and submit internship reports' },
      { key: 'history', label: 'Journey', description: 'History, feedback and personal archive' },
      common[1],
    ];
  }

  if (session.role === 'SUPERVISOR') {
    return [
      { key: 'company-eval', label: 'Evaluation Desk', description: 'Review company outcomes and reports' },
      { key: 'history', label: 'Review Archive', description: 'Past evaluations and shared feedback' },
      common[1],
    ];
  }

  if (session.role === 'COORDINATOR') {
    return [
      { key: 'coordinator', label: 'Decision Board', description: 'Approve, reject or request revisions' },
      { key: 'periods', label: 'Calendar Rules', description: 'Manage periods, quotas and policy rules' },
      common[1],
    ];
  }

  return [
    { key: 'companies', label: 'Approval Center', description: 'Handle company onboarding decisions' },
    { key: 'admin-ops', label: 'Command Room', description: 'Audit logs, announcements and control actions' },
    { key: 'periods', label: 'System Rules', description: 'Configure periods and operational thresholds' },
    common[1],
  ];
}
