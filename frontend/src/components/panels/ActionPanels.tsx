import type { ReactNode } from 'react';
import { apiCall } from '../../services/api';
import type { ApiRunner, Session } from '../../types';

type PanelProps = {
  session: NonNullable<Session>;
  loading: boolean;
  runRequest: ApiRunner;
};

type ActionCardProps = {
  title: string;
  body: string;
  actionLabel: string;
  disabled: boolean;
  onAction: () => Promise<unknown>;
};

type MetricProps = {
  label: string;
  value: string;
  detail: string;
};

function WorkspaceHero({
  eyebrow,
  title,
  body,
  tone,
}: {
  eyebrow: string;
  title: string;
  body: string;
  tone: 'student' | 'supervisor' | 'coordinator' | 'admin' | 'support';
}) {
  return (
    <section className={`workspace-hero ${tone}`}>
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
        <p>{body}</p>
      </div>
      <div className="hero-orb" aria-hidden="true" />
    </section>
  );
}

function MetricsRow({ items }: { items: MetricProps[] }) {
  return (
    <div className="metrics-grid">
      {items.map((item) => (
        <article key={item.label} className="metric-card">
          <p className="metric-label">{item.label}</p>
          <h3>{item.value}</h3>
          <p className="meta">{item.detail}</p>
        </article>
      ))}
    </div>
  );
}

function ActionCard({ title, body, actionLabel, disabled, onAction }: ActionCardProps) {
  return (
    <article className="action-card">
      <div>
        <h3>{title}</h3>
        <p>{body}</p>
      </div>
      <button disabled={disabled} className="secondary-button" onClick={() => void onAction()}>
        {actionLabel}
      </button>
    </article>
  );
}

function InsightList({ title, items }: { title: string; items: string[] }) {
  return (
    <article className="insight-card">
      <h3>{title}</h3>
      <div className="timeline-list">
        {items.map((item) => (
          <div key={item} className="timeline-item">
            <span className="timeline-dot" />
            <p>{item}</p>
          </div>
        ))}
      </div>
    </article>
  );
}

function ActionGrid({ children }: { children: ReactNode }) {
  return <section className="action-grid">{children}</section>;
}

export function ApplicationPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="student"
        eyebrow="Student cockpit"
        title="Everything needed to launch an internship application with confidence."
        body="Track your readiness, submit the internship request, and keep your academic flow aligned from one curated student workspace."
      />

      <MetricsRow
        items={[
          { label: 'Eligibility', value: 'Ready', detail: 'Profile and minimum period assumptions look valid.' },
          { label: 'Target window', value: 'June 2026', detail: 'Suggested internship period for the sample application flow.' },
          { label: 'Lecture binding', value: 'BBM325', detail: 'Default academic mapping included in the request payload.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="Send internship application"
          body="Submit the sample internship application to the backend using the active student session."
          actionLabel="Create application"
          disabled={loading}
          onAction={() =>
            runRequest('Internship application created', () =>
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
        />
        <ActionCard
          title="Open report studio"
          body="Jump into the report workspace and continue with draft-based reporting once the internship starts."
          actionLabel="Save sample draft"
          disabled={loading}
          onAction={() =>
            runRequest('Draft report saved', () =>
              apiCall('/api/internships/1/report/draft', 'POST', session.token, {
                templateContent: 'Draft report content',
              }),
            )
          }
        />
        <InsightList
          title="Suggested sequence"
          items={[
            'Confirm the academic period and company record before submitting.',
            'Use the student report workspace to keep weekly progress current.',
            'Review final history and feedback after the internship is closed.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function ReportPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="student"
        eyebrow="Report studio"
        title="Build a clean internship report workflow instead of chasing scattered drafts."
        body="This screen is tuned for students who need to keep draft momentum, version clarity and final submission discipline."
      />

      <MetricsRow
        items={[
          { label: 'Current mode', value: 'Drafting', detail: 'Live save flow connected to the backend draft endpoint.' },
          { label: 'Review state', value: 'Pending', detail: 'Coordinator feedback becomes visible after formal submission.' },
          { label: 'Attachment policy', value: 'Tracked', detail: 'Documents and signatures can be added in later iterations.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="Save draft"
          body="Persist the current report body as a draft and keep the workflow moving without a final submit."
          actionLabel="Save draft now"
          disabled={loading}
          onAction={() =>
            runRequest('Report draft updated', () =>
              apiCall('/api/internships/1/report/draft', 'POST', session.token, {
                templateContent: 'Draft report content',
              }),
            )
          }
        />
        <InsightList
          title="Report quality checklist"
          items={[
            'Explain daily work with concrete technical detail.',
            'Align the narrative with the lecture code and internship scope.',
            'Leave enough time for coordinator revisions before the deadline.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function CompaniesPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="admin"
        eyebrow="Admin approval center"
        title="Approve company onboarding with a sharper operational overview."
        body="The admin workspace emphasizes queue health, onboarding throughput and high-signal action paths for registration decisions."
      />

      <MetricsRow
        items={[
          { label: 'Pending queue', value: 'Live', detail: 'Fetch real pending company data with one action.' },
          { label: 'Decision path', value: 'Fast lane', detail: 'Approve or reject records directly from the workspace.' },
          { label: 'Traceability', value: 'Audited', detail: 'Every action can be cross-checked through the audit stream.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="List pending companies"
          body="Load the company approval queue and inspect the latest onboarding candidates."
          actionLabel="Fetch pending queue"
          disabled={loading}
          onAction={() => runRequest('Pending companies fetched', () => apiCall('/api/companies/pending', 'GET', session.token))}
        />
        <ActionCard
          title="Approve company #1"
          body="Run a sample approval action for the first company record and validate the admin approval workflow."
          actionLabel="Approve sample"
          disabled={loading}
          onAction={() => runRequest('Company approved', () => apiCall('/api/companies/1/approve', 'PUT', session.token))}
        />
        <InsightList
          title="Operations focus"
          items={[
            'Use pending queue visibility to reduce company onboarding delays.',
            'Pair approval actions with audit log review in the command room.',
            'Keep period rules aligned when new organizations enter the system.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function PeriodsPanel({ session, loading, runRequest }: PanelProps) {
  const isAdmin = session.role === 'ADMIN';

  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone={isAdmin ? 'admin' : 'coordinator'}
        eyebrow={isAdmin ? 'System rules' : 'Calendar rules'}
        title={isAdmin ? 'Control global constraints that shape the whole internship platform.' : 'Steer the academic calendar with coordinator-grade precision.'}
        body="Manage periods, rules and thresholds in a workspace designed to feel deliberate, structured and operationally trustworthy."
      />

      <MetricsRow
        items={[
          { label: 'Rule engine', value: 'Mutable', detail: 'System rules can be listed and updated on demand.' },
          { label: 'Academic cadence', value: 'Aligned', detail: 'Period definitions keep application windows coherent.' },
          { label: 'Risk level', value: 'Controlled', detail: 'One workspace for governance instead of scattered admin forms.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="List academic periods"
          body="Fetch the active period catalog to inspect timeline definitions and lifecycle dates."
          actionLabel="Load periods"
          disabled={loading}
          onAction={() => runRequest('Periods listed', () => apiCall('/api/periods', 'GET', session.token))}
        />
        <ActionCard
          title="Update minimum day rule"
          body="Apply a sample rule update for minimum internship day count and validate rule management."
          actionLabel="Upsert rule"
          disabled={loading}
          onAction={() =>
            runRequest('Rule upserted', () =>
              apiCall('/api/periods/rules', 'POST', session.token, {
                ruleKey: 'MIN_INTERNSHIP_DAYS',
                ruleValue: '20',
                description: 'Minimum internship day count',
              }),
            )
          }
        />
        <InsightList
          title="Governance notes"
          items={[
            'Period health directly affects application and reporting flow quality.',
            'Minimum day requirements should stay visible to students early.',
            'Rule changes are safer when paired with a quick audit review.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function CompanyEvaluationPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="supervisor"
        eyebrow="Supervisor evaluation desk"
        title="Review performance with a calmer, more executive evaluation surface."
        body="Supervisors get a focused environment for internship outcome reporting, report assessment and signature-aware documentation."
      />

      <MetricsRow
        items={[
          { label: 'Evaluation stage', value: 'Open', detail: 'Ready for document-backed company review submission.' },
          { label: 'Report quality', value: 'Manual', detail: 'Narrative review remains under supervisor judgment.' },
          { label: 'Signature path', value: 'Prepared', detail: 'Signature file metadata is included in the sample payload.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="Submit company evaluation"
          body="Send the supervisor evaluation payload, including result notes and signature file metadata."
          actionLabel="Submit evaluation"
          disabled={loading}
          onAction={() =>
            runRequest('Company evaluation submitted', () =>
              apiCall('/api/company-evaluations/1', 'POST', session.token, {
                internshipResultDocument: 'Result details',
                reportEvaluationDocument: 'Evaluation details',
                signatureFilePath: '/tmp/signature.png',
              }),
            )
          }
        />
        <InsightList
          title="Review posture"
          items={[
            'Tie comments to real deliverables and observed workplace output.',
            'Use concise language that supports coordinator decisions later.',
            'Capture signature artifacts consistently for audit readiness.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function CoordinatorPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="coordinator"
        eyebrow="Coordinator decision board"
        title="Move from queue chaos to a disciplined review command center."
        body="The coordinator dashboard prioritizes pending reviews, decision velocity and feedback quality with a stronger information hierarchy."
      />

      <MetricsRow
        items={[
          { label: 'Pending reviews', value: 'Queue based', detail: 'Load real pending coordinator items from the backend.' },
          { label: 'Decision mode', value: 'Revision', detail: 'Sample action requests additional work on a report.' },
          { label: 'Feedback quality', value: 'Visible', detail: 'Structured comments stay central to the decision flow.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="List pending reviews"
          body="Fetch all internships waiting for coordinator attention and inspect the active decision queue."
          actionLabel="Load review queue"
          disabled={loading}
          onAction={() => runRequest('Coordinator queue fetched', () => apiCall('/api/coordinator/pending', 'GET', session.token))}
        />
        <ActionCard
          title="Request revision"
          body="Mark a sample internship as revision required and send clear guidance back into the process."
          actionLabel="Mark revision"
          disabled={loading}
          onAction={() =>
            runRequest('Revision requested', () =>
              apiCall('/api/coordinator/internships/1/decision', 'PUT', session.token, {
                status: 'REVISION_REQUIRED',
                feedback: 'Please improve report details.',
              }),
            )
          }
        />
        <InsightList
          title="Coordinator principles"
          items={[
            'Shorter feedback loops reduce student uncertainty dramatically.',
            'Review queues work best when policy rules stay current.',
            'Consistent revision notes lead to cleaner second submissions.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function AdminOpsPanel({ session, loading, runRequest }: PanelProps) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="admin"
        eyebrow="Admin command room"
        title="Operate the platform like a real production control center."
        body="Audit visibility, announcement publishing and system guardrails now live in a denser, more polished operations workspace."
      />

      <MetricsRow
        items={[
          { label: 'Audit access', value: 'Enabled', detail: 'Inspect the operational trail from a single click.' },
          { label: 'Comms lane', value: 'Broadcast', detail: 'Announcements can be published directly to the platform.' },
          { label: 'System stance', value: 'Proactive', detail: 'Pair this area with approvals and rules for full control.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="Read audit logs"
          body="Open the audit trail to inspect critical actions, login events and approval history."
          actionLabel="Fetch audit logs"
          disabled={loading}
          onAction={() => runRequest('Audit logs loaded', () => apiCall('/api/admin/audit-logs', 'GET', session.token))}
        />
        <ActionCard
          title="Create announcement"
          body="Send a sample platform announcement about upcoming internship report deadlines."
          actionLabel="Publish notice"
          disabled={loading}
          onAction={() =>
            runRequest('Announcement created', () =>
              apiCall('/api/admin/announcements', 'POST', session.token, {
                title: 'Submission Deadline',
                content: 'Do not miss the upcoming report deadline.',
              }),
            )
          }
        />
        <InsightList
          title="Admin rhythm"
          items={[
            'Use announcements to reduce repetitive support load.',
            'Audit review is the fastest way to validate critical operations.',
            'Company approvals and rules should be treated as one shared system surface.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}

export function SupportPanel({ session, loading, runRequest }: { session: Session; loading: boolean; runRequest: ApiRunner }) {
  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="support"
        eyebrow="Support companion"
        title="A shared help layer that still feels native to the premium workspace."
        body="Search FAQs, use autocomplete and ask the assistant without leaving the visual system or breaking your current role flow."
      />

      <MetricsRow
        items={[
          { label: 'Access', value: session ? 'Authenticated' : 'Guest mode', detail: 'Support remains useful before and after login.' },
          { label: 'Discovery', value: 'Fast', detail: 'Autocomplete helps users find known tasks quickly.' },
          { label: 'Self-service', value: 'Enabled', detail: 'Chatbot and FAQ endpoints reduce dependency on manual support.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="List FAQs"
          body="Load the FAQ set from the support service and review the latest self-service content."
          actionLabel="Show FAQs"
          disabled={loading}
          onAction={() => runRequest('FAQs fetched', () => apiCall('/api/support/faqs', 'GET', session?.token))}
        />
        <ActionCard
          title="Try autocomplete"
          body="Query support autocomplete with a sample report-related term to test guided discovery."
          actionLabel="Run autocomplete"
          disabled={loading}
          onAction={() => runRequest('Autocomplete results fetched', () => apiCall('/api/support/autocomplete?query=report', 'GET', session?.token))}
        />
        <ActionCard
          title="Ask the chatbot"
          body="Send a sample support question and inspect the assistant response from the backend."
          actionLabel="Ask question"
          disabled={loading}
          onAction={() => runRequest('Chatbot answer received', () => apiCall('/api/support/chatbot?question=How to submit report?', 'GET', session?.token))}
        />
      </ActionGrid>
    </div>
  );
}

export function HistoryPanel({ session, loading, runRequest }: PanelProps) {
  const studentMode = session.role === 'STUDENT';

  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone={studentMode ? 'student' : 'supervisor'}
        eyebrow={studentMode ? 'Journey archive' : 'Review archive'}
        title={studentMode ? 'Look back at your internship path with richer context and feedback tools.' : 'Track previous evaluations and close the loop with structured feedback.'}
        body="History is no longer just a response dump. It becomes a curated archive area with action points and clear next steps."
      />

      <MetricsRow
        items={[
          { label: 'Archive state', value: 'Available', detail: 'History endpoints remain accessible from the active role.' },
          { label: 'Feedback loop', value: studentMode ? 'Open' : 'Shared', detail: 'Capture qualitative input after the workflow is complete.' },
          { label: 'Visibility', value: 'Consolidated', detail: 'Past activity lives in one readable workspace.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title={studentMode ? 'Load internship history' : 'Load review history'}
          body="Fetch the history endpoint and inspect previous records linked to the authenticated account."
          actionLabel="Open archive"
          disabled={loading}
          onAction={() => runRequest('History loaded', () => apiCall('/api/history/internships', 'GET', session.token))}
        />
        <ActionCard
          title="Submit feedback"
          body="Send structured feedback to validate the post-process review channel."
          actionLabel="Send feedback"
          disabled={loading}
          onAction={() =>
            runRequest('Feedback submitted', () =>
              apiCall('/api/history/feedback', 'POST', session.token, {
                internshipId: 1,
                answersJson: JSON.stringify({ q1: 5, q2: 'Great process' }),
              }),
            )
          }
        />
        <InsightList
          title="Why this matters"
          items={[
            'A good archive helps users understand the full workflow retrospectively.',
            'Feedback creates a visible loop between product operations and real experience.',
            'Historical visibility supports both support and governance investigations.',
          ]}
        />
      </ActionGrid>
    </div>
  );
}
