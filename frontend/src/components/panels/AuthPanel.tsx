import { useState } from 'react';
import type { FormEvent } from 'react';
import { apiCall } from '../../services/api';
import type { ApiRunner, Session } from '../../types';

type Props = {
  loading: boolean;
  runRequest: ApiRunner;
  onSession: (session: NonNullable<Session>) => void;
  onPanelChange: (panel: string) => void;
};

export default function AuthPanel({ loading, runRequest, onSession, onPanelChange }: Props) {
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  async function onLogin(e: FormEvent) {
    e.preventDefault();
    await runRequest(async () => {
      const data = await apiCall('/api/auth/login', 'POST', undefined, {
        username: loginUsername,
        password: loginPassword,
      });
      const payload = data?.data;
      onSession({ token: payload.token, username: payload.username, role: payload.role });
      onPanelChange('auth');
      return data;
    });
  }

  return (
    <>
      <h2>Authenticate (UC-001)</h2>
      <form onSubmit={onLogin} className="grid">
        <input value={loginUsername} onChange={(e) => setLoginUsername(e.target.value)} placeholder="Username" required />
        <input value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} type="password" placeholder="Password" required />
        <button disabled={loading}>Login</button>
      </form>
    </>
  );
}
