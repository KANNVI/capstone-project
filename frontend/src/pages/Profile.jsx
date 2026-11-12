import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { updateDonorProfile } from '../store/authSlice';
import LocationButton from '../components/LocationButton';
import { GroupTag } from '../components/Badges';
import { fmtDay } from '../utils/constants';

export default function Profile() {
  const dispatch = useDispatch();
  const user = useSelector((s) => s.auth.user);
  const isDonor = user.role === 'DONOR';
  const [form, setForm] = useState({
    city: user.city || '', phone: user.phone || '', lastDonationDate: user.lastDonationDate || '',
    latitude: user.latitude ?? '', longitude: user.longitude ?? '',
  });
  const [msg, setMsg] = useState({ type: '', text: '' });
  const [busy, setBusy] = useState(false);

  useEffect(() => { setMsg({ type: '', text: '' }); }, []);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    const res = await dispatch(updateDonorProfile({
      city: form.city, phone: form.phone,
      lastDonationDate: form.lastDonationDate || null,
      latitude: form.latitude === '' ? null : Number(form.latitude),
      longitude: form.longitude === '' ? null : Number(form.longitude),
    }));
    setMsg(res.error ? { type: 'error', text: res.payload } : { type: 'ok', text: 'Profile saved.' });
    setBusy(false);
  };

  return (
    <main className="page narrow">
      <h1>Profile</h1>
      <section className="panel stack">
        <div className="row">
          {isDonor && <GroupTag group={user.bloodGroup} big />}
          <div>
            <strong>{user.name}</strong>
            <div className="muted small">{user.email} · {user.role.toLowerCase()} · {user.verified ? 'verified by admin' : 'not verified yet'}</div>
          </div>
        </div>
      </section>

      {isDonor ? (
        <form className="panel stack" onSubmit={save}>
          <h2>Donor details</h2>
          {msg.text && <div className={`alert ${msg.type === 'ok' ? 'alert-ok' : 'alert-error'}`} role="status">{msg.text}</div>}
          <div className="grid2">
            <label>Phone <input value={form.phone} onChange={set('phone')} /></label>
            <label>City <input value={form.city} onChange={set('city')} /></label>
          </div>
          <label>Last donation date
            <input type="date" max={new Date().toISOString().slice(0, 10)} value={form.lastDonationDate} onChange={set('lastDonationDate')} />
            <span className="muted small">
              {user.eligibleNow ? 'You are eligible to donate.' : `Next eligible: ${fmtDay(user.nextEligibleDate)}`}
            </span>
          </label>
          <fieldset className="loc-box">
            <legend>Location</legend>
            <LocationButton label="Update from my current location" done={form.latitude !== ''}
                            onLocate={(c) => setForm((f) => ({ ...f, latitude: c.latitude, longitude: c.longitude }))} />
            <div className="grid2">
              <label>Latitude <input type="number" step="any" value={form.latitude} onChange={set('latitude')} /></label>
              <label>Longitude <input type="number" step="any" value={form.longitude} onChange={set('longitude')} /></label>
            </div>
          </fieldset>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button>
        </form>
      ) : (
        <section className="panel"><p className="muted">Phone: {user.phone}{user.city ? ` · City: ${user.city}` : ''}</p></section>
      )}
    </main>
  );
}
