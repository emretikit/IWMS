import type { MenuPanel, Session } from '../types';

export function getPanels(session: Session): MenuPanel[] {
  const common: MenuPanel[] = [
    { key: 'auth', label: 'Home', description: 'Secure sign-in and system overview' },
    { key: 'support', label: 'Support', description: 'FAQ, chatbot and guided help' },
  ];

  if (!session) return common;

  if (session.role === 'STUDENT') {
    return [
      { key: 'application', label: 'Apply', description: 'Start and track internship applications' },
      { key: 'report', label: 'Reports', description: 'Draft and submit internship reports' },
      { key: 'profile', label: 'My Profile', description: 'View your information and update your password' },
      { key: 'history', label: 'History', description: 'History, feedback and personal archive' },
      common[1],
    ];
  }

  if (session.role === 'SUPERVISOR') {
    return [
      { key: 'company-eval', label: 'Review', description: 'Approve internship applications' },
      { key: 'supervisor-complete', label: 'Internships', description: 'Mark approved internships as completed' },
      { key: 'profile', label: 'My Profile', description: 'Manage company info and your password' },
      { key: 'history', label: 'History', description: 'Past evaluations and shared feedback' },
      common[1],
    ];
  }

  if (session.role === 'COORDINATOR') {
    return [
      { key: 'coordinator', label: 'Reviews', description: 'Approve, reject or request revisions' },
      { key: 'periods', label: 'Periods', description: 'Manage periods, quotas and policy rules' },
      common[1],
    ];
  }

  return [
    { key: 'companies', label: 'Approvals', description: 'Handle company onboarding decisions' },
    { key: 'admin-ops', label: 'Ops', description: 'Audit logs, announcements and control actions' },
    { key: 'manage', label: 'Yönet', description: 'Onaylanmış şirketleri yönetin' },
    { key: 'periods', label: 'Periods', description: 'Configure periods and operational thresholds' },
    common[1],
  ];
}
