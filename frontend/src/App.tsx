import { startTransition, useEffect, useMemo, useState } from 'react';
import { Routes, Route, useNavigate, useLocation, Navigate } from 'react-router-dom';
import './App.css';
import type { Role, Session } from './types';
import AuthPanel, { CompanyRegistrationPanel, UserRegistrationPanel } from './components/panels/AuthPanel';
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
  SupervisorApprovalPage,
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

function getDefaultPath(role: Role) {
  if (role === 'STUDENT') return '/application';
  if (role === 'SUPERVISOR') return '/company-eval';
  if (role === 'COORDINATOR') return '/coordinator';
  return '/companies';
}

function App() {
  const [session, setSession] = useState<Session>(null);
  const [feedback, setFeedback] = useState<ApiFeedback>(defaultFeedback);
  const [loading, setLoading] = useState(false);
  const [navOpen, setNavOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const panels = useMemo(() => getPanels(session), [session]);
  const supervisorToken = useMemo(() => {
    const prefix = '/supervisor/token/';
    if (!location.pathname.startsWith(prefix)) {
      return null;
    }
    return decodeURIComponent(location.pathname.slice(prefix.length));
  }, [location.pathname]);

  // Oturum açıldığında veya URL değiştiğinde yönlendirmeyi yöneten useEffect
  useEffect(() => {
    if (session && location.pathname === '/') {
      navigate(getDefaultPath(session.role), { replace: true });
    }
  }, [session, location.pathname, navigate]);

  async function runRequest(title: string, request: () => Promise<unknown>, onSuccess?: () => void) {
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
      if (onSuccess) {
        onSuccess();
      }
      return true;
    } catch (error) {
      startTransition(() => {
        setFeedback({
          title: 'Error: ' + title,
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

  const resultPanel = <ResultPanel result={feedback.body} title={feedback.title} isError={feedback.isError} lastUpdated={feedback.lastUpdated} />;

  if (!session) {
    return (
      <div className="auth-layout">
        <Routes>
          <Route path="/supervisor/token/:token" element={<SupervisorApprovalPage token={supervisorToken} loading={loading} runRequest={runRequest} onBackHome={() => navigate('/')} />} />
          <Route path="/company/register" element={<CompanyRegistrationPanel loading={loading} runRequest={runRequest} onBack={() => navigate('/')} />} />
          <Route path="/register" element={<UserRegistrationPanel loading={loading} runRequest={runRequest} onBack={() => navigate('/')} />} />
          <Route path="*" element={<AuthPanel loading={loading} runRequest={runRequest} onSession={setSession} onNavigateCompanyRegister={() => navigate('/company/register')} />} />
        </Routes>
        {resultPanel}
      </div>
    );
  }

  const activePanel = panels.find(p => p.key === location.pathname.substring(1))?.key || getDefaultPath(session.role).substring(1);

  return (
    <div className="layout">
      <Sidebar
        panels={panels}
        activePanel={activePanel}
        onSelectPanel={(key) => navigate(`/${key}`)}
        isOpen={navOpen}
        onClose={() => setNavOpen(false)}
        onLogout={() => {
          setNavOpen(false);
          setSession(null);
          setFeedback(defaultFeedback);
          navigate('/');
        }}
      />

      <main className="content">
        <div className="workspace-toolbar">
          <button type="button" className="menu-toggle" aria-label="Open navigation" onClick={() => setNavOpen(true)}>
            <span />
            <span />
            <span />
          </button>
          <span className="toolbar-label">{panels.find((panel) => panel.key === activePanel)?.label ?? session.role}</span>
        </div>

        <section className="main-shell">
          <Routes>
            <Route path="/application" element={<ApplicationPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/report" element={<ReportPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/companies" element={<CompaniesPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/periods" element={<PeriodsPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/company-eval" element={<CompanyEvaluationPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/coordinator" element={<CoordinatorPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/admin-ops" element={<AdminOpsPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/support" element={<SupportPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="/history" element={<HistoryPanel session={session} loading={loading} runRequest={runRequest} />} />
            <Route path="*" element={<Navigate to={getDefaultPath(session.role)} replace />} />
          </Routes>
        </section>

        {resultPanel}
      </main>
    </div>
  );
}

export default App;