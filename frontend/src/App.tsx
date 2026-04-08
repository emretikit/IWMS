import { useMemo, useState } from 'react';
import './App.css';
import type { Session } from './types';
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

function App() {
  const [session, setSession] = useState<Session>(null);
  const [activePanel, setActivePanel] = useState('auth');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);

  const panels = useMemo(() => getPanels(session), [session]);

  async function runRequest(request: () => Promise<unknown>) {
    setLoading(true);
    try {
      const data = await request();
      setResult(JSON.stringify(data, null, 2));
    } catch (error) {
      setResult(String(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="layout">
      <Sidebar
        usernameLabel={session ? `${session.username} (${session.role})` : 'Not authenticated'}
        panels={panels}
        activePanel={activePanel}
        onSelectPanel={setActivePanel}
        showLogout={Boolean(session)}
        onLogout={() => {
          setSession(null);
          setActivePanel('auth');
        }}
      />

      <main className="content">
        <section className="card">
          {activePanel === 'auth' && (
            <AuthPanel loading={loading} runRequest={runRequest} onSession={setSession} onPanelChange={setActivePanel} />
          )}

          {activePanel === 'application' && session && (
            <ApplicationPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'report' && session && (
            <ReportPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'companies' && session && (
            <CompaniesPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'periods' && session && (
            <PeriodsPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'company-eval' && session && (
            <CompanyEvaluationPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'coordinator' && session && (
            <CoordinatorPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'admin-ops' && session && (
            <AdminOpsPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'support' && (
            <SupportPanel session={session} loading={loading} runRequest={runRequest} />
          )}

          {activePanel === 'history' && session && (
            <HistoryPanel session={session} loading={loading} runRequest={runRequest} />
          )}
        </section>

        <ResultPanel result={result} />
      </main>
    </div>
  );
}

export default App;
