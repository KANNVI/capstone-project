import { useCallback, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import api, { errMsg } from '../api/client';
import usePolling from '../utils/usePolling';
import DistanceBar from '../components/DistanceBar';
import { GroupTag, StatusBadge, UrgencyBadge } from '../components/Badges';
import { STATUS_STEPS, STATUS_LABEL, fmtDate } from '../utils/constants';

export default function RequestDetails() {
  const { id } = useParams();
  const role = useSelector((s) => s.auth.user.role);
  const [req, setReq] = useState(null);
  const [donors, setDonors] = useState([]);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      const [r, d] = await Promise.all([api.get(`/requests/${id}`), api.get(`/requests/${id}/donors`)]);
      setReq(r.data);
      setDonors(d.data);
      setError('');
    } catch (e) {
      setError(errMsg(e));
    }
  }, [id]);

  usePolling(load, 10000);

  const notifyMore = async () => {
    setBusy(true);
    setInfo('');
    try {
      const { data } = await api.post(`/requests/${id}/notify-more?radiusKm=50`);
      setInfo(data.newDonorsNotified > 0
        ? `${data.newDonorsNotified} more donor(s) within 50 km were alerted.`
        : 'No new compatible donors found within 50 km.');
      await load();
    } catch (e) {
      setError(errMsg(e));
    }
    setBusy(false);
  };

  const cancel = async () => {
    if (!window.confirm('Cancel this request? Donors who accepted will be told.')) return;
    setBusy(true);
    try {
      await api.post(`/requests/${id}/cancel`);
      await load();
    } catch (e) {
      setError(errMsg(e));
    }
    setBusy(false);
  };

  if (error && !req) return <main className="page"><div className="alert alert-error">{error}</div><Link to="/dashboard">Back to dashboard</Link></main>;
  if (!req) return <main className="page"><p className="muted">Loading…</p></main>;

  const closed = req.status === 'COMPLETED' || req.status === 'CANCELLED';
  const stepIndex = STATUS_STEPS.indexOf(req.status);

  return (
    <main className="page">
      <Link to="/dashboard" className="back">Back to dashboard</Link>

      <section className={`panel req urg-${req.urgency}`}>
        <div className="req-top">
          <GroupTag group={req.bloodGroup} big />
          <div className="grow">
            <h1>{req.hospital}{req.city ? `, ${req.city}` : ''}</h1>
            <div className="muted small">
              {req.unitsRequired} unit{req.unitsRequired > 1 ? 's' : ''}{req.patientName ? ` for ${req.patientName}` : ''} · created {fmtDate(req.createdAt)}
            </div>
          </div>
          <UrgencyBadge urgency={req.urgency} />
        </div>
        {req.notes && <p>{req.notes}</p>}

        {req.status === 'CANCELLED' ? (
          <StatusBadge status="CANCELLED" />
        ) : (
          <ol className="steps" aria-label="Request progress">
            {STATUS_STEPS.map((s, i) => (
              <li key={s} className={i < stepIndex ? 'done' : i === stepIndex ? 'now' : ''}>{STATUS_LABEL[s]}</li>
            ))}
          </ol>
        )}

        <div className="units">
          <div className="units-track"><div className="units-fill" style={{ width: `${(req.unitsFulfilled / req.unitsRequired) * 100}%` }} /></div>
          <span className="small">{req.unitsFulfilled} of {req.unitsRequired} units donated</span>
        </div>
      </section>

      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {info && <div className="alert alert-ok" role="status">{info}</div>}

      <div className="page-head">
        <h2>Donors alerted ({donors.length})</h2>
        {role === 'REQUESTER' && !closed && (
          <div className="row">
            <button className="btn btn-ghost" onClick={notifyMore} disabled={busy}>Alert more donors (50 km)</button>
            <button className="btn btn-quiet danger" onClick={cancel} disabled={busy}>Cancel request</button>
          </div>
        )}
      </div>

      {donors.length === 0 ? (
        <div className="empty">
          <p>No compatible, available donors were found within 25 km.</p>
          {role === 'REQUESTER' && !closed && <p className="muted small">Try “Alert more donors” to search within 50 km.</p>}
        </div>
      ) : (
        <ul className="donor-list panel">
          {donors.map((d) => (
            <li key={d.donorRequestId}>
              <GroupTag group={d.bloodGroup} />
              <div className="grow">
                <div>{d.name}</div>
                <div className="muted small">{d.exactMatch ? 'Exact match' : 'Compatible group'}</div>
              </div>
              <DistanceBar km={d.distanceKm ?? 0} />
              <div className="donor-state">
                <StatusBadge status={d.status} />
                {d.phone && <a className="small" href={`tel:${d.phone}`}>{d.phone}</a>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
