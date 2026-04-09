import { useEffect, useRef, useState, type ReactNode } from 'react';
import { apiCall, multipartApiCall } from '../../services/api';
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

type ApprovedCompany = {
  id: number;
  name: string;
  address: string;
  approvalStatus: string;
  supervisors?: Array<{
    id: number;
    firstName: string;
    lastName: string;
    title: string;
    companyEmail: string;
    engineerType: string;
  }>;
};

type AcademicPeriod = {
  id: number;
  name: string;
  semesterType: string;
  year: number;
  submissionDeadline: string;
  lateDeadline: string;
  minInternshipDays: number;
  maxOrgsPerPeriod: number;
  active: boolean;
};

type InternshipRecord = {
  id: number;
  studentName: string;
  companyName: string;
  status: string;
  startDate: string;
  endDate: string;
  totalWorkingDays: number;
  hasReport: boolean;
  hasEvaluation: boolean;
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

function DateField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  const inputRef = useRef<HTMLInputElement | null>(null);

  function openPicker() {
    const input = inputRef.current as (HTMLInputElement & { showPicker?: () => void }) | null;
    if (!input) return;
    if (typeof input.showPicker === 'function') {
      input.showPicker();
      return;
    }
    input.focus();
    input.click();
  }

  return (
    <label className="field date-field">
      <span>{label}</span>
      <div className="date-input-shell">
        <input ref={inputRef} type="date" value={value} onChange={(e) => onChange(e.target.value)} required />
        <button type="button" className="date-picker-button" aria-label={`Open ${label}`} onClick={openPicker}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
        </button>
      </div>
    </label>
  );
}

function getInclusiveDayDifference(startDate: string, endDate: string) {
  const start = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);
  const millisecondsPerDay = 24 * 60 * 60 * 1000;
  return Math.floor((end.getTime() - start.getTime()) / millisecondsPerDay) + 1;
}

export function ApplicationPanel({ session, loading, runRequest }: PanelProps) {
  const [approvedCompanies, setApprovedCompanies] = useState<ApprovedCompany[]>([]);
  const [activePeriods, setActivePeriods] = useState<AcademicPeriod[]>([]);
  const [internships, setInternships] = useState<InternshipRecord[]>([]);
  const [dataWarning, setDataWarning] = useState('');
  const [submitMessage, setSubmitMessage] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [selectedCompanyId, setSelectedCompanyId] = useState('');
  const [selectedPeriodId, setSelectedPeriodId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [totalWorkingDays, setTotalWorkingDays] = useState('20');
  const [lectureCode, setLectureCode] = useState('BBM325');

  async function loadUc4Data() {
    const [companyResponse, periodResponse, internshipResponse] = await Promise.allSettled([
      apiCall('/api/companies/approved', 'GET', session.token),
      apiCall('/api/periods/active', 'GET', session.token),
      apiCall('/api/internships/my', 'GET', session.token),
    ]);

    const nextCompanies = companyResponse.status === 'fulfilled' ? companyResponse.value?.data ?? [] : [];
    const nextPeriods = periodResponse.status === 'fulfilled' ? periodResponse.value?.data ?? [] : [];
    const nextInternships = internshipResponse.status === 'fulfilled' ? internshipResponse.value?.data ?? [] : [];
    const warnings = [
      companyResponse.status === 'rejected' ? 'Approved companies could not be loaded.' : '',
      periodResponse.status === 'rejected' ? 'Active periods could not be loaded. Restart the backend if the new endpoint is not active yet.' : '',
      internshipResponse.status === 'rejected' ? 'Internship history could not be loaded.' : '',
    ].filter(Boolean);

    setApprovedCompanies(nextCompanies);
    setActivePeriods(nextPeriods);
    setInternships(nextInternships);
    setDataWarning(warnings.join(' '));
    setSelectedCompanyId((current) => current || (nextCompanies[0] ? String(nextCompanies[0].id) : ''));
    setSelectedPeriodId((current) => current || (nextPeriods[0] ? String(nextPeriods[0].id) : ''));

    return {
      approvedCompanies: nextCompanies.length,
      activePeriods: nextPeriods.length,
      internships: nextInternships.length,
      warnings,
    };
  }

  useEffect(() => {
    void loadUc4Data();
  }, [session.token]);

  const selectedCompany = approvedCompanies.find((company) => String(company.id) === selectedCompanyId);
  const selectedPeriod = activePeriods.find((period) => String(period.id) === selectedPeriodId);
  const requestedDays = Number(totalWorkingDays);

  async function submitApplication() {
    const minDays = selectedPeriod?.minInternshipDays ?? 20;

    setSubmitMessage('');
    setSubmitError('');

    if (Number.isNaN(requestedDays) || requestedDays < minDays) {
      const message = `Total working days has to be at least ${minDays}.`;
      setSubmitError(message);
      throw new Error(message);
    }

    if (selectedCompany && internships.some((internship) => internship.companyName === selectedCompany.name)) {
      const message = 'You cannot apply to the same company more than once.';
      setSubmitError(message);
      throw new Error(message);
    }

    if (!startDate || !endDate) {
      const message = 'Start date and end date are required.';
      setSubmitError(message);
      throw new Error(message);
    }

    const expectedDays = getInclusiveDayDifference(startDate, endDate);
    if (expectedDays !== requestedDays) {
      const message = `Total working days must match the date range. Expected ${expectedDays} days for the selected dates.`;
      setSubmitError(message);
      throw new Error(message);
    }

    const response = await apiCall('/api/internships/apply', 'POST', session.token, {
      companyId: Number(selectedCompanyId),
      academicPeriodId: Number(selectedPeriodId),
      startDate,
      endDate,
      totalWorkingDays: Number(totalWorkingDays),
      lectureCode,
    });

    await loadUc4Data();
    setSubmitMessage('Internship application submitted successfully. Your request is now waiting for company approval.');
    return response;
  }

  return (
    <div className="workspace-stack">
      <WorkspaceHero
        tone="student"
        eyebrow="Student cockpit"
        title="Apply to an approved company, track the request, and stay aligned with the internship workflow."
        body="This UC-004 workspace pulls approved companies, active academic periods and your live application history into one student-facing flow."
      />

      <MetricsRow
        items={[
          { label: 'Approved companies', value: String(approvedCompanies.length), detail: 'Only approved organizations can be selected for internship applications.' },
          { label: 'Active periods', value: String(activePeriods.length), detail: 'Students can only submit against active academic periods.' },
          { label: 'My applications', value: String(internships.length), detail: 'Track pending, approved and rejected applications from one place.' },
        ]}
      />

      <section className="data-grid two-up">
        <article className="form-card">
          <div className="form-card-header">
            <div>
              <p className="eyebrow">Application form</p>
              <h3>Submit internship application</h3>
              <p className="meta">If the company list is empty, ask the admin to approve a company and create an active period first.</p>
              {dataWarning ? <p className="auth-error left-align">{dataWarning}</p> : null}
              {submitError ? <p className="auth-error left-align">{submitError}</p> : null}
              {submitMessage ? <p className="success-note">{submitMessage}</p> : null}
            </div>
            <button className="ghost-button" disabled={loading} onClick={() => void runRequest('UC-004 data refreshed', loadUc4Data)}>
              Refresh data
            </button>
          </div>

          <div className="auth-form">
            <label className="field">
              <span>Approved company</span>
              <select value={selectedCompanyId} onChange={(e) => setSelectedCompanyId(e.target.value)} disabled={!approvedCompanies.length}>
                {approvedCompanies.length === 0 ? <option value="">No approved company available</option> : null}
                {approvedCompanies.map((company) => (
                  <option key={company.id} value={company.id}>
                    {company.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="field">
              <span>Academic period</span>
              <select value={selectedPeriodId} onChange={(e) => setSelectedPeriodId(e.target.value)} disabled={!activePeriods.length}>
                {activePeriods.length === 0 ? <option value="">No active period available</option> : null}
                {activePeriods.map((period) => (
                  <option key={period.id} value={period.id}>
                    {period.name} ({period.semesterType} {period.year})
                  </option>
                ))}
              </select>
            </label>

            <div className="two-column-grid">
              <DateField label="Start date" value={startDate} onChange={setStartDate} />
              <DateField label="End date" value={endDate} onChange={setEndDate} />
            </div>

            <div className="two-column-grid">
              <label className="field">
                <span>Total working days</span>
                <input type="number" min={1} value={totalWorkingDays} onChange={(e) => setTotalWorkingDays(e.target.value)} required />
              </label>

              <label className="field">
                <span>Lecture code</span>
                <input value={lectureCode} onChange={(e) => setLectureCode(e.target.value)} placeholder="BBM325" required />
              </label>
            </div>

            <button
              className="primary-button"
              disabled={loading || !selectedCompanyId || !selectedPeriodId || !startDate || !endDate || !totalWorkingDays || !lectureCode}
              onClick={() =>
                void runRequest('Internship application submitted', async () => {
                  try {
                    return await submitApplication();
                  } catch (error) {
                    setSubmitMessage('');
                    setSubmitError(error instanceof Error ? error.message : String(error));
                    throw error;
                  }
                })
              }
            >
              {loading ? 'Submitting...' : 'Submit application'}
            </button>
          </div>
        </article>

        <article className="form-card">
          <p className="eyebrow">Selected company</p>
          <h3>{selectedCompany ? selectedCompany.name : 'Choose a company'}</h3>
          <p className="meta">{selectedCompany ? selectedCompany.address : 'Pick an approved company to see supervisor information.'}</p>

          {selectedCompany?.supervisors?.[0] ? (
            <div className="detail-stack">
              <p><strong>Supervisor:</strong> {selectedCompany.supervisors[0].firstName} {selectedCompany.supervisors[0].lastName}</p>
              <p><strong>Title:</strong> {selectedCompany.supervisors[0].title}</p>
              <p><strong>Email:</strong> {selectedCompany.supervisors[0].companyEmail}</p>
              <p><strong>Engineer:</strong> {selectedCompany.supervisors[0].engineerType}</p>
            </div>
          ) : (
            <p className="meta">Supervisor details will appear here when the selected company has a registered supervisor record.</p>
          )}

          <hr className="section-divider" />

          <p className="eyebrow">Selected period</p>
          <h3>{selectedPeriod ? `${selectedPeriod.name} (${selectedPeriod.semesterType} ${selectedPeriod.year})` : 'Choose an active period'}</h3>
          <div className="detail-stack">
            <p><strong>Submission deadline:</strong> {selectedPeriod?.submissionDeadline ?? 'N/A'}</p>
            <p><strong>Late deadline:</strong> {selectedPeriod?.lateDeadline ?? 'N/A'}</p>
            <p><strong>Minimum internship days:</strong> {selectedPeriod?.minInternshipDays ?? 'N/A'}</p>
          </div>
        </article>
      </section>

      <section className="form-card">
        <div className="form-card-header">
          <div>
            <p className="eyebrow">Application tracker</p>
            <h3>My internship applications</h3>
          </div>
        </div>

        <div className="application-list">
          {internships.length === 0 ? (
            <p className="meta">No internship application has been submitted yet.</p>
          ) : (
            internships.map((internship) => (
              <article key={internship.id} className="application-item">
                <div className="application-item-head">
                  <div>
                    <h4>{internship.companyName}</h4>
                    <p className="meta">
                      {internship.startDate} - {internship.endDate}
                    </p>
                  </div>
                  <span className={`status-chip ${internship.status.toLowerCase().replace(/_/g, '-')}`}>{internship.status}</span>
                </div>
                <div className="application-item-body">
                  <p><strong>Working days:</strong> {internship.totalWorkingDays}</p>
                  <p><strong>Report submitted:</strong> {internship.hasReport ? 'Yes' : 'No'}</p>
                  <p><strong>Company evaluation:</strong> {internship.hasEvaluation ? 'Yes' : 'No'}</p>
                </div>
              </article>
            ))
          )}
        </div>
      </section>
    </div>
  );
}

export function ReportPanel({ session, loading, runRequest }: PanelProps) {
  const [internships, setInternships] = useState<InternshipRecord[]>([]);
  const [selectedInternshipId, setSelectedInternshipId] = useState('');
  const [reportTitle, setReportTitle] = useState('');
  const [introduction, setIntroduction] = useState('');
  const [companyOverview, setCompanyOverview] = useState('');
  const [workPerformed, setWorkPerformed] = useState('');
  const [technologiesUsed, setTechnologiesUsed] = useState('');
  const [outcomesAndLearning, setOutcomesAndLearning] = useState('');
  const [conclusion, setConclusion] = useState('');
  const [reportFile, setReportFile] = useState<File | null>(null);
  const [reportError, setReportError] = useState('');
  const [reportSuccess, setReportSuccess] = useState('');
  const [lastSubmissionMeta, setLastSubmissionMeta] = useState<{
    id: number | null;
    internshipId: number | null;
    fileName: string;
    submissionStatus: string;
    submittedAt: string;
  } | null>(null);

  async function loadStudentInternships() {
    const response = await apiCall('/api/internships/my', 'GET', session.token);
    const nextInternships = response?.data ?? [];
    setInternships(nextInternships);
    setSelectedInternshipId((current) => current || (nextInternships[0] ? String(nextInternships[0].id) : ''));
    return response;
  }

  useEffect(() => {
    void loadStudentInternships();
  }, [session.token]);

  async function submitStructuredReport() {
    setReportError('');
    setReportSuccess('');

    if (!selectedInternshipId) {
      throw new Error('Please select an internship before submitting the report.');
    }

    if (!reportTitle || !introduction || !companyOverview || !workPerformed || !technologiesUsed || !outcomesAndLearning || !conclusion) {
      throw new Error('All report text fields must be filled in.');
    }

    if (!reportFile) {
      throw new Error('Please upload a PDF report file.');
    }

    if (reportFile.type !== 'application/pdf' && !reportFile.name.toLowerCase().endsWith('.pdf')) {
      throw new Error('Only PDF files are accepted.');
    }

    const formData = new FormData();
    formData.append(
      'report',
      new Blob(
        [
          JSON.stringify({
            reportTitle,
            introduction,
            companyOverview,
            workPerformed,
            technologiesUsed,
            outcomesAndLearning,
            conclusion,
          }),
        ],
        { type: 'application/json' },
      ),
    );
    formData.append('file', reportFile);

    const response = await multipartApiCall(`/api/internships/${selectedInternshipId}/report`, 'POST', formData, session.token);
    await loadStudentInternships();
    setReportSuccess('Internship report submitted successfully.');
    setLastSubmissionMeta({
      id: response?.data?.id ?? null,
      internshipId: response?.data?.internshipId ?? Number(selectedInternshipId),
      fileName: response?.data?.fileName ?? reportFile.name,
      submissionStatus: response?.data?.submissionStatus ?? 'SUBMITTED',
      submittedAt: response?.data?.submittedAt ?? '',
    });
    setReportTitle('');
    setIntroduction('');
    setCompanyOverview('');
    setWorkPerformed('');
    setTechnologiesUsed('');
    setOutcomesAndLearning('');
    setConclusion('');
    setReportFile(null);
    return response;
  }

  return (
    <div className="workspace-stack">
      <section className="data-grid two-up">
        <article className="form-card">
          <div className="form-card-header">
            <div>
              <p className="eyebrow">Final report</p>
              <h3>Submit internship report</h3>
              {reportError ? <p className="auth-error left-align">{reportError}</p> : null}
              {reportSuccess ? <p className="success-note">{reportSuccess}</p> : null}
            </div>
            <button className="ghost-button" disabled={loading} onClick={() => void runRequest('Student internships refreshed', loadStudentInternships)}>
              Refresh internships
            </button>
          </div>

          <div className="auth-form">
            <label className="field">
              <span>Internship record</span>
              <select value={selectedInternshipId} onChange={(e) => setSelectedInternshipId(e.target.value)}>
                {internships.length === 0 ? <option value="">No internship record found</option> : null}
                {internships.map((internship) => (
                  <option key={internship.id} value={internship.id}>
                    #{internship.id} - {internship.companyName} ({internship.status})
                  </option>
                ))}
              </select>
            </label>

            <label className="field">
              <span>Report title</span>
              <input value={reportTitle} onChange={(e) => setReportTitle(e.target.value)} placeholder="Stored inside template_content" required />
            </label>

            <label className="field">
              <span>Introduction</span>
              <textarea rows={4} value={introduction} onChange={(e) => setIntroduction(e.target.value)} placeholder="Introduction section" required />
            </label>

            <label className="field">
              <span>Company overview</span>
              <textarea rows={4} value={companyOverview} onChange={(e) => setCompanyOverview(e.target.value)} placeholder="Company overview section" required />
            </label>

            <label className="field">
              <span>Work performed</span>
              <textarea rows={5} value={workPerformed} onChange={(e) => setWorkPerformed(e.target.value)} placeholder="Daily and weekly work details" required />
            </label>

            <label className="field">
              <span>Technologies used</span>
              <textarea rows={4} value={technologiesUsed} onChange={(e) => setTechnologiesUsed(e.target.value)} placeholder="Tools, frameworks and technologies" required />
            </label>

            <label className="field">
              <span>Outcomes and learning</span>
              <textarea rows={4} value={outcomesAndLearning} onChange={(e) => setOutcomesAndLearning(e.target.value)} placeholder="What you achieved and learned" required />
            </label>

            <label className="field">
              <span>Conclusion</span>
              <textarea rows={4} value={conclusion} onChange={(e) => setConclusion(e.target.value)} placeholder="Conclusion section" required />
            </label>

            <label className="field">
              <span>PDF report file</span>
              <input
                type="file"
                accept="application/pdf,.pdf"
                onChange={(e) => {
                  setReportFile(e.target.files?.[0] ?? null);
                  setReportError('');
                }}
                required
              />
            </label>

            <button
              className="primary-button"
              disabled={loading || !selectedInternshipId}
              onClick={() =>
                void runRequest('Internship report submitted', async () => {
                  try {
                    return await submitStructuredReport();
                  } catch (error) {
                    setReportSuccess('');
                    setReportError(error instanceof Error ? error.message : String(error));
                    throw error;
                  }
                })
              }
            >
              {loading ? 'Submitting...' : 'Submit final report'}
            </button>
          </div>
        </article>

        <article className="form-card">
          <p className="eyebrow">Database mapping</p>
          <h3>Values written on final submit</h3>
          <div className="detail-stack">
            <p><strong>internship_report.internship_id:</strong> selected internship record</p>
            <p><strong>internship_report.template_content:</strong> report title + six text sections</p>
            <p><strong>internship_report.file_name:</strong> uploaded PDF file name</p>
            <p><strong>internship_report.file_path:</strong> stored server path for the uploaded PDF</p>
            <p><strong>internship_report.submission_status:</strong> `SUBMITTED`</p>
            <p><strong>internship_report.is_draft:</strong> `false`</p>
            <p><strong>internship_report.submitted_at:</strong> current server timestamp</p>
            <p><strong>internship.status:</strong> `PENDING_COORDINATOR_REVIEW` after successful submit</p>
          </div>

          <hr className="section-divider" />

          <p className="eyebrow">Last submit</p>
          {lastSubmissionMeta ? (
            <div className="detail-stack">
              <p><strong>Report ID:</strong> {lastSubmissionMeta.id ?? 'N/A'}</p>
              <p><strong>Internship ID:</strong> {lastSubmissionMeta.internshipId ?? 'N/A'}</p>
              <p><strong>File name:</strong> {lastSubmissionMeta.fileName || 'N/A'}</p>
              <p><strong>Status:</strong> {lastSubmissionMeta.submissionStatus || 'N/A'}</p>
              <p><strong>Submitted at:</strong> {lastSubmissionMeta.submittedAt || 'N/A'}</p>
            </div>
          ) : (
            <p className="meta">Submit the final report once to see the returned `internship_report` metadata here.</p>
          )}
        </article>
      </section>
    </div>
  );
}

export function CompaniesPanel({ session, loading, runRequest }: PanelProps) {
  const [pendingCompanies, setPendingCompanies] = useState<any[]>([]);
  const [approvedCompanies, setApprovedCompanies] = useState<ApprovedCompany[]>([]);
  const [rejectReasons, setRejectReasons] = useState<Record<number, string>>({});

  async function loadApprovedCompanies() {
    const response = await apiCall('/api/companies/approved', 'GET', session.token);
    setApprovedCompanies(response?.data ?? []);
    return response;
  }

  async function refreshCompanies() {
    const [pendingResponse, approvedResponse] = await Promise.all([
      apiCall('/api/companies/pending', 'GET', session.token),
      apiCall('/api/companies/approved', 'GET', session.token),
    ]);

    setPendingCompanies(pendingResponse?.data ?? []);
    setApprovedCompanies(approvedResponse?.data ?? []);

    return {
      pendingCompanies: (pendingResponse?.data ?? []).length,
      approvedCompanies: (approvedResponse?.data ?? []).length,
    };
  }

  async function approveCompany(companyId: number) {
    const response = await apiCall(`/api/companies/${companyId}/approve`, 'PUT', session.token);
    await refreshCompanies();
    return response;
  }

  async function rejectCompany(companyId: number) {
    const reason = (rejectReasons[companyId] ?? '').trim() || 'Rejected by admin review';
    const response = await apiCall(`/api/companies/${companyId}/reject?reason=${encodeURIComponent(reason)}`, 'PUT', session.token);
    await refreshCompanies();
    return response;
  }

  useEffect(() => {
    void refreshCompanies();
  }, [session.token]);

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
          { label: 'Pending queue', value: String(pendingCompanies.length), detail: 'Every new company application lands here first.' },
          { label: 'Approved companies', value: String(approvedCompanies.length), detail: 'Students can only see companies that appear in this approved list.' },
          { label: 'Traceability', value: 'Audited', detail: 'Every action can be cross-checked through the audit stream.' },
        ]}
      />

      <ActionGrid>
        <ActionCard
          title="Refresh company directories"
          body="Reload both pending and approved company lists to verify what students can currently access."
          actionLabel="Refresh all"
          disabled={loading}
          onAction={() => runRequest('Company directories refreshed', refreshCompanies)}
        />
        <InsightList
          title="Operations focus"
          items={[
            'Use pending queue visibility to reduce company onboarding delays.',
            'Pair approval actions with audit log review in the command room.',
            'Students only see the records that move from pending into the approved directory.',
          ]}
        />
      </ActionGrid>

      <section className="company-request-list">
        <article className="form-card">
          <div className="form-card-header">
            <div>
              <p className="eyebrow">Approved directory</p>
              <h3>Registered and approved companies</h3>
            </div>
            <button className="ghost-button" disabled={loading} onClick={() => void runRequest('Approved companies loaded', loadApprovedCompanies)}>
              Refresh approved
            </button>
          </div>

          <div className="application-list">
            {approvedCompanies.length === 0 ? (
              <p className="meta">There is no approved company yet. Students will see an empty selection list until at least one company is approved.</p>
            ) : (
              approvedCompanies.map((company) => (
                <article key={`approved-${company.id}`} className="application-item">
                  <div className="application-item-head">
                    <div>
                      <h4>{company.name}</h4>
                      <p className="meta">{company.address}</p>
                    </div>
                    <span className="status-chip approved">{company.approvalStatus}</span>
                  </div>
                  <div className="application-item-body">
                    <p><strong>Supervisor:</strong> {company.supervisors?.[0]?.firstName} {company.supervisors?.[0]?.lastName}</p>
                    <p><strong>Email:</strong> {company.supervisors?.[0]?.companyEmail ?? 'N/A'}</p>
                    <p><strong>Engineer:</strong> {company.supervisors?.[0]?.engineerType ?? 'N/A'}</p>
                  </div>
                </article>
              ))
            )}
          </div>
        </article>
      </section>

      <section className="company-request-list">
        {pendingCompanies.length === 0 ? (
          <article className="request-card">
            <h3>Pending requests cleared</h3>
            <p className="meta">New company applications will appear here as soon as they are submitted from the public registration screen.</p>
          </article>
        ) : (
          pendingCompanies.map((company) => (
            <article key={company.id} className="request-card">
              <div className="request-card-header">
                <div>
                  <h3>{company.name}</h3>
                  <p className="meta">{company.address}</p>
                </div>
                <span className="request-status">{company.approvalStatus}</span>
              </div>

              <div className="request-details">
                <p><strong>Supervisor:</strong> {company.supervisors?.[0]?.firstName} {company.supervisors?.[0]?.lastName}</p>
                <p><strong>Title:</strong> {company.supervisors?.[0]?.title}</p>
                <p><strong>Email:</strong> {company.supervisors?.[0]?.companyEmail}</p>
                <p><strong>Engineer:</strong> {company.supervisors?.[0]?.engineerType}</p>
              </div>

              <label className="field">
                <span>Rejection reason</span>
                <textarea
                  rows={3}
                  value={rejectReasons[company.id] ?? ''}
                  onChange={(e) => setRejectReasons((current) => ({ ...current, [company.id]: e.target.value }))}
                  placeholder="Add an optional reason before rejecting"
                />
              </label>

              <div className="request-actions">
                <button disabled={loading} className="primary-button" onClick={() => void runRequest(`Company ${company.name} approved`, () => approveCompany(company.id))}>
                  Approve
                </button>
                <button disabled={loading} className="ghost-button danger-button" onClick={() => void runRequest(`Company ${company.name} rejected`, () => rejectCompany(company.id))}>
                  Reject
                </button>
              </div>
            </article>
          ))
        )}
      </section>
    </div>
  );
}

export function PeriodsPanel({ session, loading, runRequest }: PanelProps) {
  const isAdmin = session.role === 'ADMIN';
  const [periods, setPeriods] = useState<AcademicPeriod[]>([]);
  const [periodError, setPeriodError] = useState('');
  const [periodSuccess, setPeriodSuccess] = useState('');
  const [periodName, setPeriodName] = useState('');
  const [semesterType, setSemesterType] = useState('SUMMER');
  const [year, setYear] = useState(String(new Date().getFullYear()));
  const [submissionDeadline, setSubmissionDeadline] = useState('');
  const [lateDeadline, setLateDeadline] = useState('');
  const [minInternshipDays, setMinInternshipDays] = useState('20');
  const [maxOrgsPerPeriod, setMaxOrgsPerPeriod] = useState('1');
  const [active, setActive] = useState(true);

  async function loadPeriods() {
    const response = await apiCall('/api/periods', 'GET', session.token);
    setPeriods(response?.data ?? []);
    return response;
  }

  async function createPeriod() {
    setPeriodError('');
    setPeriodSuccess('');

    if (!periodName.trim()) {
      throw new Error('Period name is required.');
    }

    if (lateDeadline < submissionDeadline) {
      throw new Error('Late deadline cannot be before submission deadline.');
    }

    const response = await apiCall('/api/periods', 'POST', session.token, {
      name: periodName.trim(),
      semesterType,
      year: Number(year),
      submissionDeadline,
      lateDeadline,
      minInternshipDays: Number(minInternshipDays),
      maxOrgsPerPeriod: Number(maxOrgsPerPeriod),
      active,
    });

    await loadPeriods();
    setPeriodSuccess('Academic period created successfully.');
    setPeriodName('');
    setSemesterType('SUMMER');
    setYear(String(new Date().getFullYear()));
    setSubmissionDeadline('');
    setLateDeadline('');
    setMinInternshipDays('20');
    setMaxOrgsPerPeriod('1');
    setActive(true);
    return response;
  }

  useEffect(() => {
    void loadPeriods();
  }, [session.token]);

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
          { label: 'Configured periods', value: String(periods.length), detail: 'All academic periods currently stored in the system.' },
          { label: 'Rule engine', value: 'Mutable', detail: 'System rules can be listed and updated on demand.' },
          { label: 'Risk level', value: 'Controlled', detail: 'One workspace for governance instead of scattered admin forms.' },
        ]}
      />

      <section className="data-grid two-up">
        <article className="form-card">
          <div className="form-card-header">
            <div>
              <p className="eyebrow">Active setup</p>
              <h3>Create academic period</h3>
              {periodError ? <p className="auth-error left-align">{periodError}</p> : null}
              {periodSuccess ? <p className="success-note">{periodSuccess}</p> : null}
            </div>
            <button className="ghost-button" disabled={loading} onClick={() => void runRequest('Periods listed', loadPeriods)}>
              Refresh periods
            </button>
          </div>

          <div className="auth-form">
            <label className="field">
              <span>Period name</span>
              <input value={periodName} onChange={(e) => setPeriodName(e.target.value)} placeholder="2026 Summer Internship" required />
            </label>

            <div className="two-column-grid">
              <label className="field">
                <span>Semester</span>
                <select value={semesterType} onChange={(e) => setSemesterType(e.target.value)}>
                  <option value="FALL">Fall</option>
                  <option value="SPRING">Spring</option>
                  <option value="SUMMER">Summer</option>
                </select>
              </label>

              <label className="field">
                <span>Year</span>
                <input type="number" value={year} onChange={(e) => setYear(e.target.value)} required />
              </label>
            </div>

            <div className="two-column-grid">
              <DateField label="Submission deadline" value={submissionDeadline} onChange={setSubmissionDeadline} />
              <DateField label="Late deadline" value={lateDeadline} onChange={setLateDeadline} />
            </div>

            <div className="two-column-grid">
              <label className="field">
                <span>Minimum internship days</span>
                <input type="number" min={1} value={minInternshipDays} onChange={(e) => setMinInternshipDays(e.target.value)} required />
              </label>

              <label className="field">
                <span>Max organizations per period</span>
                <input type="number" min={1} value={maxOrgsPerPeriod} onChange={(e) => setMaxOrgsPerPeriod(e.target.value)} required />
              </label>
            </div>

            <label className="checkbox-row">
              <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} />
              <span>Set this period as active</span>
            </label>

            <button
              className="primary-button"
              disabled={loading || !periodName || !submissionDeadline || !lateDeadline || !year || !minInternshipDays || !maxOrgsPerPeriod}
              onClick={() =>
                void runRequest('Academic period created', async () => {
                  try {
                    return await createPeriod();
                  } catch (error) {
                    setPeriodSuccess('');
                    setPeriodError(error instanceof Error ? error.message : String(error));
                    throw error;
                  }
                })
              }
            >
              {loading ? 'Saving...' : 'Create active period'}
            </button>
          </div>
        </article>

        <article className="form-card">
          <p className="eyebrow">Configured periods</p>
          <h3>Current academic periods</h3>
          <div className="application-list">
            {periods.length === 0 ? (
              <p className="meta">No academic period is configured yet.</p>
            ) : (
              periods.map((period) => (
                <article key={period.id} className="application-item">
                  <div className="application-item-head">
                    <div>
                      <h4>{period.name}</h4>
                      <p className="meta">
                        {period.semesterType} {period.year}
                      </p>
                    </div>
                    <span className={`status-chip ${period.active ? 'approved' : ''}`}>{period.active ? 'ACTIVE' : 'INACTIVE'}</span>
                  </div>
                  <div className="application-item-body">
                    <p><strong>Submission:</strong> {period.submissionDeadline}</p>
                    <p><strong>Late:</strong> {period.lateDeadline}</p>
                    <p><strong>Min days:</strong> {period.minInternshipDays}</p>
                  </div>
                </article>
              ))
            )}
          </div>
        </article>
      </section>

      <ActionGrid>
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
            'Students can only apply when at least one active period exists.',
            'Approved company data and active period data together unlock the UC-004 student form.',
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

export function SupervisorApprovalPage({
  token,
  loading,
  runRequest,
  onBackHome,
}: {
  token: string;
  loading: boolean;
  runRequest: ApiRunner;
  onBackHome: () => void;
}) {
  const [verificationCode, setVerificationCode] = useState('');
  const [internship, setInternship] = useState<InternshipRecord | null>(null);
  const [loadError, setLoadError] = useState('');

  async function loadTokenData() {
    const response = await apiCall(`/api/internships/token/${token}`, 'GET');
    setInternship(response?.data ?? null);
    setLoadError('');
    return response;
  }

  useEffect(() => {
    loadTokenData().catch((error) => {
      setLoadError(error instanceof Error ? error.message : String(error));
    });
  }, [token]);

  async function handleDecision(action: 'approve' | 'reject') {
    const response = await apiCall(`/api/internships/token/${token}/${action}?code=${encodeURIComponent(verificationCode)}`, 'PUT');
    await loadTokenData();
    return response;
  }

  return (
    <section className="auth-screen">
      <div className="auth-screen-inner">
        <header className="auth-title">
          <h1>Supervisor approval workspace</h1>
        </header>

        <div className="login-card minimal company-register-card supervisor-card">
          <div className="detail-stack">
            <p className="eyebrow">Public approval link</p>
            <h3>{internship ? internship.companyName : 'Loading internship details'}</h3>
            <p className="meta">
              {internship
                ? `Review the student internship request and apply your decision using the verification code shared with you.`
                : 'Fetching internship details from the secure token link.'}
            </p>
          </div>

          {loadError ? <p className="auth-error">{loadError}</p> : null}

          {internship ? (
            <div className="detail-stack">
              <p><strong>Student:</strong> {internship.studentName}</p>
              <p><strong>Company:</strong> {internship.companyName}</p>
              <p><strong>Dates:</strong> {internship.startDate} - {internship.endDate}</p>
              <p><strong>Total working days:</strong> {internship.totalWorkingDays}</p>
              <p><strong>Status:</strong> {internship.status}</p>
            </div>
          ) : null}

          <label className="field">
            <span>Verification code</span>
            <input value={verificationCode} onChange={(e) => setVerificationCode(e.target.value)} placeholder="Enter 6-digit code" required />
          </label>

          <div className="request-actions">
            <button className="primary-button" disabled={loading || !verificationCode.trim()} onClick={() => void runRequest('Internship approved by supervisor', () => handleDecision('approve'))}>
              {loading ? 'Processing...' : 'Approve internship'}
            </button>
            <button className="ghost-button danger-button" disabled={loading || !verificationCode.trim()} onClick={() => void runRequest('Internship rejected by supervisor', () => handleDecision('reject'))}>
              Reject internship
            </button>
          </div>

          <button type="button" className="ghost-button" onClick={onBackHome}>
            Back to login
          </button>
        </div>
      </div>
    </section>
  );
}
