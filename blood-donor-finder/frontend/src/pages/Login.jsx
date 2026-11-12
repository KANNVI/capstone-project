import { useState } from 'react';
import { api } from '../api.js';

export default function Login({ onLogin }) {
  const [mode, setMode] = useState('login');
  const [f, setF] = useState({ name: '', email: '', password: '' });
  const [err, setErr] = useState('');
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault(); setErr('');
    try { onLogin(await api('/auth/' + mode, { method: 'POST', body: f })); }
    catch (ex) { setErr(ex.message); }
  };

  return (
    <form className="card narrow" onSubmit={submit}>
      <h2>{mode === 'login' ? 'Sign in' : 'Create account'}</h2>
      {mode === 'register' && <><label>Name</label><input value={f.name} onChange={set('name')} required /></>}
      <label>Email</label><input type="email" value={f.email} onChange={set('email')} required />
      <label>Password (min 6 characters)</label><input type="password" value={f.password} onChange={set('password')} required />
      {err && <p className="err">{err}</p>}
      <p><button className="btn">{mode === 'login' ? 'Sign in' : 'Register'}</button>{' '}
        <button type="button" className="link" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
          {mode === 'login' ? 'New here? Register' : 'Have an account? Sign in'}
        </button></p>
      <p className="mu">Demo admin: admin@blood.com / admin123</p>
    </form>
  );
}
