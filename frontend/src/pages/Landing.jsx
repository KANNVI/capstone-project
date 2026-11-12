import { Link, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import DistanceBar from '../components/DistanceBar';
import { GroupTag } from '../components/Badges';

const SAMPLE = [
  { name: 'Amit S.', group: 'O+', km: 1.2, note: 'Exact match, available' },
  { name: 'Simran K.', group: 'O+', km: 2.8, note: 'Exact match, available' },
  { name: 'Pooja B.', group: 'O-', km: 4.1, note: 'Compatible, available' },
];

export default function Landing() {
  const token = useSelector((s) => s.auth.token);
  if (token) return <Navigate to="/dashboard" replace />;

  return (
    <main>
      <section className="hero">
        <div className="hero-text">
          <h1>When someone needs blood, the right donor is usually a few kilometres away.</h1>
          <p className="lead">
            BloodConnect matches an emergency request with donors who have a compatible blood group, are
            available right now, and are close to the hospital. The nearest ones are asked first.
          </p>
          <div className="row">
            <Link to="/register" className="btn btn-primary btn-lg">Register as donor or requester</Link>
            <Link to="/login" className="btn btn-ghost btn-lg">Log in</Link>
          </div>
        </div>

        <aside className="match-card" aria-label="Example of how matching works">
          <div className="match-head">
            <div>
              <strong>Request: B+ needed</strong>
              <div className="muted small">2 units, City Hospital, critical</div>
            </div>
            <span className="badge ug-CRITICAL">Critical</span>
          </div>
          <p className="muted small">Donors asked, in order:</p>
          <ul className="match-list">
            {SAMPLE.map((d) => (
              <li key={d.name}>
                <GroupTag group={d.group} />
                <div className="grow">
                  <div>{d.name}</div>
                  <div className="muted small">{d.note}</div>
                </div>
                <DistanceBar km={d.km} />
              </li>
            ))}
          </ul>
          <p className="muted small">Donors who gave blood in the last 90 days are skipped automatically.</p>
        </aside>
      </section>

      <section className="how">
        <h2>How it works</h2>
        <div className="how-grid">
          <div>
            <h3>Requesters</h3>
            <p>Enter blood group, units, hospital and urgency. Matching donors are alerted immediately and you can watch who accepts.</p>
          </div>
          <div>
            <h3>Donors</h3>
            <p>Share your blood group and location, switch availability on or off, and accept only the requests you can reach.</p>
          </div>
          <div>
            <h3>Admins</h3>
            <p>Verify users, watch emergency requests, remove fake ones, and track donations across the network.</p>
          </div>
        </div>
      </section>
    </main>
  );
}
