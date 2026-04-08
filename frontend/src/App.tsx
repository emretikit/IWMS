import { startTransition, useEffect, useMemo, useState } from 'react';
import './App.css';
import type { Role, Session } from './types';
import AuthPanel from './components/panels/AuthPanel';
import Sidebar from './components/layout/Sidebar';
import ResultPanel from './components/layout/ResultPanel';
import { getPanels } from './config/panels';
import {
  AdminOpsPanel,
  ApplicationPanel,
  CompaniesPanel,
  CompanyEvaluationPanel,
  CoordinatorPanel,
  HistoryPanel,
  PeriodsPanel,
  ReportPanel,
  SupportPanel,
} from './components/panels/ActionPanels';

type ApiFeedback = {
  title: string;
  body: string;
  isError: boolean;
  lastUpdated: string;
};

const defaultFeedback: ApiFeedback = {
  title: 'Awaiting action',
  body: '',
  isError: false,
  lastUpdated: 'just now',
};

function getDefaultPanel(role: Role) {
  if (role === 'STUDENT') return 'application';
  if (role === 'SUPERVISOR') return 'company-eval';
  if (role === 'COORDINATOR') return 'coordinator';
  return 'companies';
}

function App() {
  const [session, setSession] = useState<Session>(null);
  const [activePanel, setActivePanel] = useState('auth');
  const [feedback, setFeedback] = useState<ApiFeedback>(defaultFeedback);
  const [loading, setLoading] = useState(false);

  const panels = useMemo(() => getPanels(session), [session]);

  useEffect(() => {
    if (!session) {
      setActivePanel('auth');
      return;
    }

    setActivePanel(getDefaultPanel(session.role));
  }, [session]);

  async function runRequest(title: string, request: () => Promise<unknown>) {
    setLoading(true);
    try {
      const data = await request();
      startTransition(() => {
        setFeedback({
          title,
          body: JSON.stringify(data, null, 2),
          isError: false,
          lastUpdated: new Date().toLocaleTimeString(),
        });
      });
      return true;
    } catch (error) {
      startTransition(() => {
        setFeedback({
          title: 'Request failed',
          body: error instanceof Error ? error.message : String(error),
          isError: true,
          lastUpdated: new Date().toLocaleTimeString(),
        });
      });
      return false;
    } finally {
      setLoading(false);
    }
  }

  if (!session) {
    return <AuthPanel loading={loading} runRequest={runRequest} onSession={setSession} />;
  }

  return (
    <div className="layout">
      <Sidebar
        usernameLabel={`${session.username} (${session.role})`}
        role={session.role}
        panels={panels}
        activePanel={activePanel}
        onSelectPanel={setActivePanel}
        showLogout
        onLogout={() => {
          setSession(null);
          setFeedback(defaultFeedback);
        }}
      />

      <main className="content">
        <section className="main-shell">
          <header className="page-header">
            <div>
              <p className="eyebrow">Adaptive experience</p>
              <h2>{`${session.role.toLowerCase()} workspace`}</h2>
              <p className="meta">The interface, hierarchy and primary action blocks are tailored to the authenticated role.</p>
            </div>
            <div className="page-badge">
              <span>{session.role}</span>
            </div>
          </header>

          {activePanel === 'application' && session && <ApplicationPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'report' && session && <ReportPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'companies' && session && <CompaniesPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'periods' && session && <PeriodsPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'company-eval' && session && <CompanyEvaluationPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'coordinator' && session && <CoordinatorPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'admin-ops' && session && <AdminOpsPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'support' && <SupportPanel session={session} loading={loading} runRequest={runRequest} />}
          {activePanel === 'history' && session && <HistoryPanel session={session} loading={loading} runRequest={runRequest} />}
        </section>

        <ResultPanel result={feedback.body} title={feedback.title} isError={feedback.isError} lastUpdated={feedback.lastUpdated} />
      </main>
    </div>
  );
}

export default App;
