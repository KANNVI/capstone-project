import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { login, clearError } from '../store/authSlice';

export default function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { token, user, error } = useSelector((s) => s.auth);
  const [form, setForm] = useState({ email: '', password: '' });
  const [busy, setBusy] = useState(false);

  useEffect(() => { dispatch(clearError()); }, [dispatch]);
  useEffect(() => { if (token && user) navigate('/dashboard', { replace: true }); }, [token, user, navigate]);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    await dispatch(login(form));
    setBusy(false);
  };

  return (
    <main className="auth">
      <form className="panel auth-card" onSubmit={submit}>
        <h1>Log in</h1>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <label>Email
          <input type="email" required autoComplete="email" value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <label>Password
          <input type="password" required autoComplete="current-password" value={form.password}
                 onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
        <p className="muted small">No account? <Link to="/register">Register</Link></p>
        <p className="muted small demo-hint">
          Demo accounts: donor <code>amit@demo.com</code>, requester <code>rahul@demo.com</code> (password{' '}
          <code>password123</code>), admin <code>admin@bloodconnect.com</code> (<code>admin123</code>).
        </p>
      </form>
    </main>
  );
}
