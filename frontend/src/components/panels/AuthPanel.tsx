import { useState } from 'react';
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
              Register Company
            </button>

            {loginError ? <p className="auth-error">{loginError}</p> : null}
          </form>
        </div>
      </div>
    </section>
  );
}

export function CompanyRegistrationPanel({ loading, runRequest, onBack }: CompanyRegistrationProps) {
  const [companyName, setCompanyName] = useState('');
  const [address, setAddress] = useState('');
  const [engineerType, setEngineerType] = useState('COMPUTER');
  const [supervisorFirstName, setSupervisorFirstName] = useState('');
  const [supervisorLastName, setSupervisorLastName] = useState('');
  const [supervisorTitle, setSupervisorTitle] = useState('');
  const [supervisorEmail, setSupervisorEmail] = useState('');
  const backgroundStyle = { '--auth-forest-image': `url(${loginImage})` } as CSSProperties;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();

    const success = await runRequest('Company registration submitted', () =>
      apiCall('/api/companies/register', 'POST', undefined, {
        name: companyName,
        address,
        engineerType,
        supervisorFirstName,
        supervisorLastName,
        supervisorTitle,
        supervisorEmail,
      }),
    );

    if (success) {
      setCompanyName('');
      setAddress('');
      setEngineerType('COMPUTER');
      setSupervisorFirstName('');
      setSupervisorLastName('');
      setSupervisorTitle('');
      setSupervisorEmail('');
    }
  }

  return (
    <section className="auth-screen" style={backgroundStyle}>
      <div className="auth-screen-inner">
        <header className="auth-title">
          <h1>Company registration</h1>
        </header>

        <div className="login-card minimal company-register-card">
          <form onSubmit={onSubmit} className="grid auth-form">
            <label className="field">
              <span>Company name</span>
              <input value={companyName} onChange={(e) => setCompanyName(e.target.value)} placeholder="Enter company name" required />
            </label>

            <label className="field">
              <span>Address</span>
              <input value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Enter company address" required />
            </label>

            <label className="field">
              <span>Engineer type</span>
              <select value={engineerType} onChange={(e) => setEngineerType(e.target.value)}>
                <option value="COMPUTER">Computer Engineer</option>
                <option value="ELECTRICAL_ELECTRONIC">Electrical-Electronic Engineer</option>
                <option value="OTHER">Other</option>
              </select>
            </label>

            <div className="two-column-grid">
              <label className="field">
                <span>Supervisor first name</span>
                <input value={supervisorFirstName} onChange={(e) => setSupervisorFirstName(e.target.value)} placeholder="First name" required />
              </label>

              <label className="field">
                <span>Supervisor last name</span>
                <input value={supervisorLastName} onChange={(e) => setSupervisorLastName(e.target.value)} placeholder="Last name" required />
              </label>
            </div>

            <label className="field">
              <span>Supervisor title</span>
              <input value={supervisorTitle} onChange={(e) => setSupervisorTitle(e.target.value)} placeholder="Job title" required />
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

            <button disabled={loading} className="primary-button login-submit">
              {loading ? 'Submitting...' : 'Submit company request'}
            </button>

            <button type="button" disabled={loading} className="ghost-button" onClick={onBack}>
              Back to login
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}
