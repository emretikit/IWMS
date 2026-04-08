import { apiCall } from '../../services/api';
import type { ApiRunner, Session } from '../../types';

type PanelProps = {
  session: NonNullable<Session>;
  loading: boolean;
  runRequest: ApiRunner;
};

export function ApplicationPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Submit Internship Application (UC-004)</h2>
      <button
        disabled={loading}
        onClick={() =>
          runRequest(() =>
            apiCall('/api/internships/apply', 'POST', session.token, {
              companyId: 1,
              academicPeriodId: 1,
              startDate: '2026-06-01',
              endDate: '2026-06-30',
              totalWorkingDays: 20,
              lectureCode: 'BBM325',
            }),
          )
        }
      >
        Send Sample Application
      </button>
    </>
  );
}

export function ReportPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Report Actions (UC-005)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/internships/1/report/draft', 'POST', session.token, { templateContent: 'Draft report content' }))}>
          Save Draft
        </button>
      </div>
    </>
  );
}

export function CompaniesPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Company Registration Approval (UC-002)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/companies/pending', 'GET', session.token))}>List Pending</button>
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/companies/1/approve', 'PUT', session.token))}>Approve #1</button>
      </div>
    </>
  );
}

export function PeriodsPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Manage Academic Periods & Rules (UC-003)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/periods', 'GET', session.token))}>List Periods</button>
        <button
          disabled={loading}
          onClick={() =>
            runRequest(() =>
              apiCall('/api/periods/rules', 'POST', session.token, {
                ruleKey: 'MIN_INTERNSHIP_DAYS',
                ruleValue: '20',
                description: 'Minimum internship day count',
              }),
            )
          }
        >
          Upsert Rule
        </button>
      </div>
    </>
  );
}

export function CompanyEvaluationPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Company Evaluation (UC-006)</h2>
      <button
        disabled={loading}
        onClick={() =>
          runRequest(() =>
            apiCall('/api/company-evaluations/1', 'POST', session.token, {
              internshipResultDocument: 'Result details',
              reportEvaluationDocument: 'Evaluation details',
              signatureFilePath: '/tmp/signature.png',
            }),
          )
        }
      >
        Submit Evaluation
      </button>
    </>
  );
}

export function CoordinatorPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Coordinator Workflow (UC-007)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/coordinator/pending', 'GET', session.token))}>List Pending</button>
        <button
          disabled={loading}
          onClick={() =>
            runRequest(() =>
              apiCall('/api/coordinator/internships/1/decision', 'PUT', session.token, {
                status: 'REVISION_REQUIRED',
                feedback: 'Please improve report details.',
              }),
            )
          }
        >
          Mark Revision Required
        </button>
      </div>
    </>
  );
}

export function AdminOpsPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>Admin Operations (UC-008)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/admin/audit-logs', 'GET', session.token))}>Audit Logs</button>
        <button
          disabled={loading}
          onClick={() =>
            runRequest(() =>
              apiCall('/api/admin/announcements', 'POST', session.token, {
                title: 'Submission Deadline',
                content: 'Do not miss the upcoming report deadline.',
              }),
            )
          }
        >
          Create Announcement
        </button>
      </div>
    </>
  );
}

export function SupportPanel({ session, loading, runRequest }: { session: Session; loading: boolean; runRequest: ApiRunner }) {
  return (
    <>
      <h2>Support (UC-009)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/support/faqs', 'GET', session?.token))}>List FAQs</button>
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/support/autocomplete?query=report', 'GET', session?.token))}>Autocomplete</button>
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/support/chatbot?question=How to submit report?', 'GET', session?.token))}>Ask Chatbot</button>
      </div>
    </>
  );
}

export function HistoryPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <>
      <h2>History & Feedback (UC-010)</h2>
      <div className="actions">
        <button disabled={loading} onClick={() => runRequest(() => apiCall('/api/history/internships', 'GET', session.token))}>My History</button>
        <button
          disabled={loading}
          onClick={() =>
            runRequest(() =>
              apiCall('/api/history/feedback', 'POST', session.token, {
                internshipId: 1,
                answersJson: JSON.stringify({ q1: 5, q2: 'Great process' }),
              }),
            )
          }
        >
          Submit Feedback
        </button>
      </div>
    </>
  );
}
