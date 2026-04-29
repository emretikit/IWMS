import { useEffect, useState } from 'react';
import type { CSSProperties, FormEvent } from 'react';
import { apiCall } from '../../services/api';
import loginImage from '../../assets/login.png';
import logoImage from '../../assets/logo.png';
import type { ApiRunner, Session } from '../../types';

type Props = {
  loading: boolean;
  runRequest: ApiRunner;
  onSession: (session: NonNullable<Session>) => void;
  onNavigateCompanyRegister: () => void;
};

type CompanyRegistrationProps = {
  loading: boolean;
  runRequest: ApiRunner;
  onBack: () => void;
};

type ApprovedCompany = {
  id: number;
  name: string;
  address: string;
  supervisors?: Array<{
    companyEmail: string;
  }>;
};

export default function AuthPanel({ loading, runRequest, onSession, onNavigateCompanyRegister }: Props) {
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loginError, setLoginError] = useState('');
  const backgroundStyle = { '--auth-forest-image': `url(${loginImage})` } as CSSProperties;

  async function onLogin(e: FormEvent) {
    e.preventDefault();
    setLoginError('');
    const success = await runRequest('Authentication completed', async () => {
      const data = await apiCall('/api/auth/login', 'POST', undefined, {
        username: loginUsername,
        password: loginPassword,
      });
      const payload = data?.data;
      onSession({ token: payload.token, username: payload.username, role: payload.role });
      return data;
    });

    if (!success) {
      setLoginError('Invalid username or password');
    }
  }

  return (
    <section className="auth-screen" style={backgroundStyle}>
      <div className="auth-screen-inner">
        <header className="auth-title">
          <h1>Internship Workflow Management System</h1>
        </header>

        <div className="login-card minimal">
          <div className="login-logo-wrap">
            <img src={logoImage} alt="Internship Workflow Management System logo" className="login-logo" />
          </div>

          <form onSubmit={onLogin} className="grid auth-form">
            <label className="field">
              <span>Username</span>
              <input
                value={loginUsername}
                onChange={(e) => setLoginUsername(e.target.value)}
                placeholder="Enter username"
                autoComplete="username"
                required
              />
            </label>

            <label className="field">
              <span>Password</span>
              <div className="password-input-wrap">
                <input
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter password"
                  autoComplete="current-password"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  aria-pressed={showPassword}
                  onClick={() => setShowPassword((current) => !current)}
                >
                  {showPassword ? (
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path d="M3 5.27 4.28 4 20 19.72 18.73 21l-2.47-2.47A12.8 12.8 0 0 1 12 19c-5.5 0-9.59-3.44-11-7 1-2.52 3.18-4.97 6.12-6.3L3 5.27Zm6.04 6.04A3 3 0 0 0 12.7 14.96l-3.66-3.65ZM12 5c5.5 0 9.59 3.44 11 7a12.66 12.66 0 0 1-3.62 4.95l-2.17-2.17A8.84 8.84 0 0 0 19.92 12c-1.35-2.26-4.13-5-7.92-5-.92 0-1.8.16-2.62.43L7.72 5.77A12.2 12.2 0 0 1 12 5Zm-.14 3a4 4 0 0 1 4 4c0 .55-.11 1.08-.31 1.56l-5.25-5.25c.48-.2 1.01-.31 1.56-.31Z" />
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path d="M12 5c5.5 0 9.59 3.44 11 7-1.41 3.56-5.5 7-11 7S2.41 15.56 1 12c1.41-3.56 5.5-7 11-7Zm0 2C8.21 7 5.43 9.74 4.08 12 5.43 14.26 8.21 17 12 17s6.57-2.74 7.92-5C18.57 9.74 15.79 7 12 7Zm0 2.5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5Z" />
                    </svg>
                  )}
                </button>
              </div>
            </label>

            <button disabled={loading} className="primary-button login-submit">
              {loading ? 'Signing in...' : 'Login'}
            </button>

            <button type="button" disabled={loading} className="ghost-button" onClick={onNavigateCompanyRegister}>
              Sign up
            </button>

            {loginError ? <p className="auth-error">{loginError}</p> : null}
          </form>
        </div>
      </div>
    </section>
  );
}

export function CompanyRegistrationPanel({ loading, runRequest, onBack }: CompanyRegistrationProps) {
  const [mode, setMode] = useState<'company' | 'supervisor'>('company');
  const [approvedCompanies, setApprovedCompanies] = useState<ApprovedCompany[]>([]);
  const [approvedError, setApprovedError] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [address, setAddress] = useState('');
  const [supervisorEmail, setSupervisorEmail] = useState('');
  const [companySuccess, setCompanySuccess] = useState('');
  const [companyError, setCompanyError] = useState('');
  const [selectedCompanyId, setSelectedCompanyId] = useState('');
  const [signupUsername, setSignupUsername] = useState('');
  const [signupName, setSignupName] = useState('');
  const [signupSurname, setSignupSurname] = useState('');
  const [signupTitle, setSignupTitle] = useState('');
  const [signupEngineerType, setSignupEngineerType] = useState('COMPUTER');
  const [signupEmail, setSignupEmail] = useState('');
  const [signupPassword, setSignupPassword] = useState('');
  const [supervisorError, setSupervisorError] = useState('');
  const [supervisorSuccess, setSupervisorSuccess] = useState('');
  const backgroundStyle = { '--auth-forest-image': `url(${loginImage})` } as CSSProperties;

  useEffect(() => {
    void loadApprovedCompanies();
  }, []);

  async function loadApprovedCompanies() {
    try {
      const response = await apiCall('/api/companies/approved', 'GET');
      const companies = response?.data ?? [];
      setApprovedCompanies(companies);
      setSelectedCompanyId((current) => current || (companies[0] ? String(companies[0].id) : ''));
      setApprovedError('');
      return response;
    } catch (error) {
      setApprovedError(error instanceof Error ? error.message : String(error));
      return null;
    }
  }

  const selectedCompany = approvedCompanies.find((company) => String(company.id) === selectedCompanyId);
  const selectedCompanyEmail = selectedCompany?.supervisors?.[0]?.companyEmail?.trim().toLowerCase() ?? '';
  const normalizedSignupEmail = signupEmail.trim().toLowerCase();
  const emailMismatch = mode === 'supervisor' && !!selectedCompanyId && !!normalizedSignupEmail && normalizedSignupEmail !== selectedCompanyEmail;

  async function onCompanySubmit(e: FormEvent) {
    e.preventDefault();
    setCompanyError('');
    setCompanySuccess('');

    const success = await runRequest('Company registration submitted', () =>
      apiCall('/api/companies/register', 'POST', undefined, {
        name: companyName,
        address,
        supervisorEmail,
      }),
    );

    if (success) {
      setCompanyName('');
      setAddress('');
      setSupervisorEmail('');
      setCompanySuccess('Company signup submitted.');
    } else {
      setCompanyError('Company signup failed.');
    }
  }

  async function onSupervisorSubmit(e: FormEvent) {
    e.preventDefault();
    setSupervisorError('');
    setSupervisorSuccess('');

    if (!selectedCompanyId) {
      setSupervisorError('Please select a company.');
      return;
    }

    if (!selectedCompanyEmail) {
      setSupervisorError('Selected company does not have a supervisor email.');
      return;
    }

    if (emailMismatch) {
      setSupervisorError('Email must exactly match the supervisor email registered by the company.');
      return;
    }

    const success = await runRequest('Supervisor signup completed', () =>
      apiCall('/api/auth/register', 'POST', undefined, {
        username: signupUsername,
        email: signupEmail,
        password: signupPassword,
        role: 'SUPERVISOR',
        name: signupName,
        surname: signupSurname,
        title: signupTitle,
        engineerType: signupEngineerType,
      }),
    );

    if (success) {
      setSignupUsername('');
      setSignupName('');
      setSignupSurname('');
      setSignupTitle('');
      setSignupEngineerType('COMPUTER');
      setSignupEmail('');
      setSignupPassword('');
      setSupervisorSuccess('Supervisor account created.');
    } else {
      setSupervisorError('Supervisor signup failed.');
    }
  }

  return (
    <section className="auth-screen" style={backgroundStyle}>
      <div className="auth-screen-inner auth-screen-centered">
        <header className="auth-title">
          <h1>Sign up</h1>
        </header>

        <div className="login-card minimal company-register-card signup-card">
          <div className="signup-switcher">
            <button type="button" className={mode === 'company' ? 'signup-chip active' : 'signup-chip'} onClick={() => setMode('company')}>
              Company
            </button>
            <button type="button" className={mode === 'supervisor' ? 'signup-chip active' : 'signup-chip'} onClick={() => setMode('supervisor')}>
              Supervisor
            </button>
          </div>

          {mode === 'company' ? (
            <form onSubmit={onCompanySubmit} className="grid auth-form">
              <label className="field">
                <span>Company name</span>
                <input value={companyName} onChange={(e) => setCompanyName(e.target.value)} placeholder="Enter company name" required />
              </label>

              <label className="field">
                <span>Address</span>
                <input value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Enter company address" required />
              </label>

              <label className="field">
                <span>Supervisor email</span>
                <input
                  value={supervisorEmail}
                  onChange={(e) => setSupervisorEmail(e.target.value)}
                  type="email"
                  placeholder="name@company.com"
                  required
                />
              </label>

              {companyError ? <p className="auth-error">{companyError}</p> : null}
              {companySuccess ? <p className="success-note center-note">{companySuccess}</p> : null}

              <button disabled={loading} className="primary-button login-submit">
                {loading ? 'Submitting...' : 'Create company'}
              </button>

              <button type="button" disabled={loading} className="ghost-button" onClick={onBack}>
                Back to login
              </button>
            </form>
          ) : (
            <form onSubmit={onSupervisorSubmit} className="grid auth-form">
              <label className="field">
                <span>Approved company</span>
                <select value={selectedCompanyId} onChange={(e) => setSelectedCompanyId(e.target.value)} disabled={!approvedCompanies.length}>
                  {approvedCompanies.length === 0 ? <option value="">No approved company</option> : null}
                  {approvedCompanies.map((company) => (
                    <option key={company.id} value={company.id}>
                      {company.name}
                    </option>
                  ))}
                </select>
              </label>

              <div className="two-column-grid">
                <label className="field">
                  <span>First name</span>
                  <input value={signupName} onChange={(e) => setSignupName(e.target.value)} placeholder="First name" required />
                </label>

                <label className="field">
                  <span>Last name</span>
                  <input value={signupSurname} onChange={(e) => setSignupSurname(e.target.value)} placeholder="Last name" required />
                </label>
              </div>

              <label className="field">
                <span>Username</span>
                <input value={signupUsername} onChange={(e) => setSignupUsername(e.target.value)} placeholder="Username" required />
              </label>

              <label className="field">
                <span>Title</span>
                <input value={signupTitle} onChange={(e) => setSignupTitle(e.target.value)} placeholder="Supervisor title" required />
              </label>

              <label className="field">
                <span>Engineer type</span>
                <select value={signupEngineerType} onChange={(e) => setSignupEngineerType(e.target.value)}>
                  <option value="COMPUTER">Computer Engineer</option>
                  <option value="ELECTRICAL_ELECTRONIC">Electrical-Electronic Engineer</option>
                  <option value="OTHER">Other</option>
                </select>
              </label>

              <label className="field">
                <span>Email</span>
                <input value={signupEmail} onChange={(e) => setSignupEmail(e.target.value)} type="email" placeholder="Supervisor email" required />
              </label>

              {selectedCompanyEmail ? <p className="signup-hint">Registered company email: {selectedCompanyEmail}</p> : null}
              {approvedError ? <p className="auth-error">{approvedError}</p> : null}
              {emailMismatch ? <p className="auth-error">Email must exactly match the company email.</p> : null}
              {supervisorError ? <p className="auth-error">{supervisorError}</p> : null}
              {supervisorSuccess ? <p className="success-note center-note">{supervisorSuccess}</p> : null}

              <label className="field">
                <span>Password</span>
                <input value={signupPassword} onChange={(e) => setSignupPassword(e.target.value)} type="password" placeholder="Password" required />
              </label>

              <button disabled={loading || emailMismatch || !selectedCompanyId} className="primary-button login-submit">
                {loading ? 'Creating...' : 'Create supervisor'}
              </button>

              <button type="button" disabled={loading} className="ghost-button" onClick={onBack}>
                Back to login
              </button>
            </form>
          )}
        </div>
      </div>
    </section>
  );
}
