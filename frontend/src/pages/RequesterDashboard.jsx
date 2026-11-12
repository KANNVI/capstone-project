import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMyRequests } from '../store/requestSlice';
import usePolling from '../utils/usePolling';
import { GroupTag, StatusBadge, UrgencyBadge } from '../components/Badges';
import { fmtDate } from '../utils/constants';

export default function RequesterDashboard() {
  const dispatch = useDispatch();
  const user = useSelector((s) => s.auth.user);
  const { items, loaded, error } = useSelector((s) => s.requests);

  usePolling(() => dispatch(fetchMyRequests()), 15000);

  return (
    <main className="page">
      <div className="page-head">
        <div>
          <h1>Hello, {user.name.split(' ')[0]}</h1>
          <p className="muted">Create a request and nearby compatible donors are alerted straight away.</p>
        </div>
        <Link to="/requests/new" className="btn btn-primary btn-lg">Create blood request</Link>
      </div>

      {error && <div className="alert alert-error" role="alert">{error}</div>}

      <h2>Your requests</h2>
      {!loaded && <p className="muted">Loading…</p>}
      {loaded && items.length === 0 && (
        <div className="empty">
          <p>You have not made any requests yet.</p>
          <Link to="/requests/new" className="btn btn-ghost">Create your first request</Link>
        </div>
      )}

      {items.map((r) => (
        <Link key={r.id} to={`/requests/${r.id}`} className={`panel req req-link urg-${r.urgency}`}>
          <div className="req-top">
            <GroupTag group={r.bloodGroup} />
            <div className="grow">
              <h3>{r.hospital}{r.city ? `, ${r.city}` : ''}</h3>
              <div className="muted small">{fmtDate(r.createdAt)} · {r.donorsNotified} donors alerted · {r.donorsAccepted} accepted</div>
            </div>
            <UrgencyBadge urgency={r.urgency} />
          </div>
          <div className="row between">
            <StatusBadge status={r.status} />
            <div className="units" aria-label={`${r.unitsFulfilled} of ${r.unitsRequired} units received`}>
              <div className="units-track"><div className="units-fill" style={{ width: `${(r.unitsFulfilled / r.unitsRequired) * 100}%` }} /></div>
              <span className="small">{r.unitsFulfilled}/{r.unitsRequired} units</span>
            </div>
          </div>
        </Link>
      ))}
    </main>
  );
}
