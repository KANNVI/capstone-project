import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchInbox, respond } from '../store/donorSlice';
import { fetchProfile, setAvailability } from '../store/authSlice';
import usePolling from '../utils/usePolling';
import DistanceBar from '../components/DistanceBar';
import { GroupTag, StatusBadge, UrgencyBadge } from '../components/Badges';
import { fmtDate, fmtDay } from '../utils/constants';

function RequestCard({ item, busy, onAct }) {
  const r = item.request;
  return (
    <article className={`panel req urg-${r.urgency}`}>
      <div className="req-top">
        <GroupTag group={r.bloodGroup} />
        <div className="grow">
          <h3>{r.hospital}{r.city ? `, ${r.city}` : ''}</h3>
          <div className="muted small">
            {r.unitsRequired} unit{r.unitsRequired > 1 ? 's' : ''} for {r.patientName || 'a patient'} · asked by {r.requesterName} · {fmtDate(item.createdAt)}
          </div>
        </div>
        <UrgencyBadge urgency={r.urgency} />
      </div>

      {item.distanceKm != null && <DistanceBar km={item.distanceKm} />}
      {r.notes && <p className="small">{r.notes}</p>}

      {item.status === 'SENT' && (
        <div className="row">
          <button className="btn btn-primary" disabled={!!busy} onClick={() => onAct(item.id, 'accept')}>Accept request</button>
          <button className="btn btn-ghost" disabled={!!busy} onClick={() => onAct(item.id, 'reject')}>Decline</button>
        </div>
      )}

      {(item.status === 'ACCEPTED' || item.status === 'IN_PROGRESS') && (
        <>
          <div className="alert alert-ok">
            You accepted this request. Call the requester on{' '}
            <a href={`tel:${r.contactPhone}`}><strong>{r.contactPhone}</strong></a> and head to the hospital.
          </div>
          <div className="row">
            {item.status === 'ACCEPTED' && (
              <button className="btn btn-primary" disabled={!!busy} onClick={() => onAct(item.id, 'start')}>I am at the hospital, start donation</button>
            )}
            <button className="btn btn-ghost" disabled={!!busy} onClick={() => onAct(item.id, 'complete')}>Mark donation completed</button>
          </div>
        </>
      )}

      {!['SENT', 'ACCEPTED', 'IN_PROGRESS'].includes(item.status) && <StatusBadge status={item.status} />}
    </article>
  );
}

export default function DonorDashboard() {
  const dispatch = useDispatch();
  const user = useSelector((s) => s.auth.user);
  const { inbox, loaded, error } = useSelector((s) => s.donor);
  const [busy, setBusy] = useState('');

  usePolling(() => dispatch(fetchInbox()), 15000);

  const act = async (id, action) => {
    setBusy(`${id}-${action}`);
    await dispatch(respond({ id, action }));
    await dispatch(fetchProfile());
    await dispatch(fetchInbox());
    setBusy('');
  };

  const incoming = inbox.filter((x) => x.status === 'SENT');
  const active = inbox.filter((x) => x.status === 'ACCEPTED' || x.status === 'IN_PROGRESS');
  const earlier = inbox.filter((x) => !['SENT', 'ACCEPTED', 'IN_PROGRESS'].includes(x.status));

  return (
    <main className="page">
      <section className="panel donor-status">
        <GroupTag group={user.bloodGroup} big />
        <div className="grow">
          <h1>Hello, {user.name.split(' ')[0]}</h1>
          {user.eligibleNow ? (
            <p className="muted">You are eligible to donate. Requests within 25 km that match your group will appear below.</p>
          ) : (
            <p className="muted">
              You donated recently. You can donate again from <strong>{fmtDay(user.nextEligibleDate)}</strong>; until then no requests are sent to you.
            </p>
          )}
        </div>
        <button
          role="switch"
          aria-checked={user.available}
          className={`switch ${user.available ? 'on' : ''}`}
          onClick={() => dispatch(setAvailability(!user.available))}
        >
          <span className="knob" />
          <span className="switch-label">{user.available ? 'Available' : 'Unavailable'}</span>
        </button>
      </section>

      {error && <div className="alert alert-error" role="alert">{error}</div>}

      {active.length > 0 && (
        <section>
          <h2>Your active donations</h2>
          {active.map((i) => <RequestCard key={i.id} item={i} busy={busy} onAct={act} />)}
        </section>
      )}

      <section>
        <h2>Emergency requests near you</h2>
        {!loaded && <p className="muted">Loading…</p>}
        {loaded && incoming.length === 0 && (
          <div className="empty">
            <p>No open requests for you right now.</p>
            <p className="muted small">
              {user.available ? 'This page refreshes every 15 seconds.' : 'You are set to unavailable. Switch to available to receive requests.'}
            </p>
          </div>
        )}
        {incoming.map((i) => <RequestCard key={i.id} item={i} busy={busy} onAct={act} />)}
      </section>

      {earlier.length > 0 && (
        <section>
          <h2>Earlier requests</h2>
          {earlier.map((i) => <RequestCard key={i.id} item={i} busy={busy} onAct={act} />)}
        </section>
      )}
    </main>
  );
}
