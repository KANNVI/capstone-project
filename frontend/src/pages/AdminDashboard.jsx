import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import usePolling from '../utils/usePolling';
import { GroupTag, StatusBadge, UrgencyBadge } from '../components/Badges';
import { fmtDate } from '../utils/constants';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [requests, setRequests] = useState([]);
  const [tab, setTab] = useState('requests');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const [s, u, r] = await Promise.all([api.get('/admin/stats'), api.get('/admin/users'), api.get('/admin/requests')]);
      setStats(s.data); setUsers(u.data); setRequests(r.data); setError('');
    } catch (e) {
      setError(errMsg(e));
    }
  }, []);
  usePolling(load, 20000);

  const run = async (fn) => {
    try { await fn(); await load(); } catch (e) { setError(errMsg(e)); }
  };

  const verify = (u) => run(() => api.put(`/admin/users/${u.id}/verify?value=${!u.verified}`));
  const block = (u) => run(() => api.put(`/admin/users/${u.id}/active?value=${!u.active}`));
  const remove = (r) => {
    if (!window.confirm(`Remove request #${r.id} (${r.bloodGroup} at ${r.hospital})?`)) return;
    run(async () => { const { data } = await api.delete(`/admin/requests/${r.id}`); setMsg(data.message); });
  };

  const cards = stats && [
    ['Users', stats.totalUsers], ['Donors', stats.donors], ['Requesters', stats.requesters],
    ['Open requests', stats.activeRequests], ['Completed', stats.completedRequests], ['Donations', stats.totalDonations],
  ];
  const maxGroup = stats ? Math.max(1, ...Object.values(stats.donorsByBloodGroup)) : 1;

  return (
    <main className="page">
      <h1>Admin dashboard</h1>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {msg && <div className="alert alert-ok" role="status">{msg}</div>}

      {stats && (
        <>
          <div className="stat-grid">
            {cards.map(([label, n]) => (
              <div className="panel stat" key={label}><strong>{n}</strong><span className="muted small">{label}</span></div>
            ))}
          </div>

          <section className="panel">
            <h2>Donors by blood group</h2>
            {Object.keys(stats.donorsByBloodGroup).length === 0 && <p className="muted">No donors yet.</p>}
            <ul className="bars">
              {Object.entries(stats.donorsByBloodGroup).map(([g, n]) => (
                <li key={g}>
                  <GroupTag group={g} />
                  <div className="units-track"><div className="units-fill" style={{ width: `${(n / maxGroup) * 100}%` }} /></div>
                  <span className="small">{n}</span>
                </li>
              ))}
            </ul>
          </section>
        </>
      )}

      <div className="seg" role="tablist">
        <button role="tab" aria-selected={tab === 'requests'} className={tab === 'requests' ? 'on' : ''} onClick={() => setTab('requests')}>Requests ({requests.length})</button>
        <button role="tab" aria-selected={tab === 'users'} className={tab === 'users' ? 'on' : ''} onClick={() => setTab('users')}>Users ({users.length})</button>
      </div>

      {tab === 'requests' ? (
        <div className="panel table-wrap">
          <table>
            <thead><tr><th>#</th><th>Group</th><th>Hospital</th><th>Requester</th><th>Urgency</th><th>Status</th><th>Units</th><th>Created</th><th></th></tr></thead>
            <tbody>
              {requests.map((r) => (
                <tr key={r.id}>
                  <td><Link to={`/requests/${r.id}`}>{r.id}</Link></td>
                  <td><GroupTag group={r.bloodGroup} /></td>
                  <td>{r.hospital}</td>
                  <td>{r.requesterName}</td>
                  <td><UrgencyBadge urgency={r.urgency} /></td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>{r.unitsFulfilled}/{r.unitsRequired}</td>
                  <td>{fmtDate(r.createdAt)}</td>
                  <td><button className="btn btn-quiet danger" onClick={() => remove(r)}>Remove</button></td>
                </tr>
              ))}
              {requests.length === 0 && <tr><td colSpan="9" className="muted">No requests yet.</td></tr>}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="panel table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Group</th><th>City</th><th>Verified</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>{u.role.toLowerCase()}</td>
                  <td>{u.bloodGroup ? <GroupTag group={u.bloodGroup} /> : '-'}</td>
                  <td>{u.city || '-'}</td>
                  <td>{u.verified ? 'Yes' : 'No'}</td>
                  <td>{u.active ? 'Active' : 'Blocked'}</td>
                  <td className="row">
                    <button className="btn btn-quiet" onClick={() => verify(u)}>{u.verified ? 'Unverify' : 'Verify'}</button>
                    {u.role !== 'ADMIN' && <button className="btn btn-quiet danger" onClick={() => block(u)}>{u.active ? 'Block' : 'Unblock'}</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}
