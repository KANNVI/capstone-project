import { useState } from 'react';
import Find from './pages/Find.jsx';
import Requests from './pages/Requests.jsx';
import DonorProfile from './pages/DonorProfile.jsx';
import Banks from './pages/Banks.jsx';
import Admin from './pages/Admin.jsx';
import Awareness from './pages/Awareness.jsx';
import Login from './pages/Login.jsx';

const TABS = ['Find donors', 'Blood requests', 'Donor profile', 'Blood banks', 'Admin', 'Awareness'];

export default function App() {
  const [tab, setTab] = useState(0);
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
  });

  const onLogin = (u) => {
    localStorage.setItem('token', u.token);
    localStorage.setItem('user', JSON.stringify({ name: u.name, role: u.role }));
    setUser({ name: u.name, role: u.role });
  };
  const logout = () => {
    localStorage.removeItem('token'); localStorage.removeItem('user'); setUser(null);
  };

  const gated = (node) => (user ? node : <Login onLogin={onLogin} />);
  const pages = [
    <Find />,
    gated(<Requests />),
    gated(<DonorProfile />),
    <Banks isAdmin={user?.role === 'ADMIN'} />,
    gated(user?.role === 'ADMIN' ? <Admin /> : <div className="card">Admin access only. Sign in as admin@blood.com.</div>),
    <Awareness />,
  ];

  return (
    <>
      <header>
        <div><h1>🩸 Blood Donation &amp; Emergency Donor Finder</h1></div>
        {user && <div className="who">{user.name} · <button className="link" onClick={logout}>Sign out</button></div>}
      </header>
      <nav>
        {TABS.map((t, i) => (
          <button key={t} className={i === tab ? 'on' : ''} onClick={() => setTab(i)}>{t}</button>
        ))}
      </nav>
      <main>{pages[tab]}</main>
    </>
  );
}
