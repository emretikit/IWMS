import { startTransition, useEffect, useMemo, useState } from 'react';
import './App.css';
import type { Role, Session } from './types';
import AuthPanel, { CompanyRegistrationPanel } from './components/panels/AuthPanel';
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

function getDefaultPanel(role: Role) {
  if (role === 'STUDENT') return 'application';
  if (role === 'SUPERVISOR') return 'company-eval';
  if (role === 'COORDINATOR') return 'coordinator';
  return 'companies';
}

function getSupervisorTokenFromPath(pathname: string) {
  const prefix = '/supervisor/token/';
  if (!pathname.startsWith(prefix)) {
    return null;
  }

  return decodeURIComponent(pathname.slice(prefix.length));
}

function App() {
  const [session, setSession] = useState<Session>(null);
  const [activePanel, setActivePanel] = useState('auth');
  const [feedback, setFeedback] = useState<ApiFeedback>(defaultFeedback);
  const [loading, setLoading] = useState(false);
  const [pathname, setPathname] = useState(window.location.pathname);
  const [navOpen, setNavOpen] = useState(false);

  const panels = useMemo(() => getPanels(session), [session]);
  const supervisorToken = useMemo(() => getSupervisorTokenFromPath(pathname), [pathname]);

  function navigateTo(path: string) {
    window.history.pushState({}, '', path);
    setPathname(window.location.pathname);
  }

  useEffect(() => {
    if (!session) {
      setActivePanel('auth');
      return;
    }

    setActivePanel(getDefaultPanel(session.role));
  }, [session]);

  useEffect(() => {
    const onPopState = () => {
      setPathname(window.location.pathname);
    };

    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

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
    if (supervisorToken) {
      return <SupervisorApprovalPage token={supervisorToken} loading={loading} runRequest={runRequest} onBackHome={() => navigateTo('/')} />;
    }

    if (activePanel === 'signup' || pathname === '/signup' || pathname === '/company/register') {
      return (
        <CompanyRegistrationPanel
          loading={loading}
          runRequest={runRequest}
          onBack={() => {
            setActivePanel('auth');
            navigateTo('/');
          }}
        />
      );
    }

    return (
      <AuthPanel
        loading={loading}
        runRequest={runRequest}
        onSession={(nextSession) => {
          setSession(nextSession);
          navigateTo('/');
        }}
        onNavigateCompanyRegister={() => {
          setActivePanel('signup');
          navigateTo('/signup');
        }}
      />
    );
  }

  return (
    <div className="layout">
      <Sidebar
        panels={panels}
        activePanel={activePanel}
        onSelectPanel={setActivePanel}
        isOpen={navOpen}
        onClose={() => setNavOpen(false)}
        onLogout={() => {
          setNavOpen(false);
          setSession(null);
          setFeedback(defaultFeedback);
          navigateTo('/');
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

        <div className="activity-feed-row">
          <ResultPanel result={feedback.body} title={feedback.title} isError={feedback.isError} lastUpdated={feedback.lastUpdated} />
        </div>
      </main>
    </div>
  );
}

export default App;
