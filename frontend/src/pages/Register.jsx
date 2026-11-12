import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { register, clearError } from '../store/authSlice';
import LocationButton from '../components/LocationButton';
import { BLOOD_GROUPS } from '../utils/constants';

export default function Register() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { token, user, error } = useSelector((s) => s.auth);
  const [busy, setBusy] = useState(false);
  const [localError, setLocalError] = useState('');
  const [form, setForm] = useState({
    name: '', email: '', password: '', phone: '', role: 'DONOR',
    bloodGroup: '', city: '', latitude: '', longitude: '', lastDonationDate: '',
  });

  useEffect(() => { dispatch(clearError()); }, [dispatch]);
  useEffect(() => { if (token && user) navigate('/dashboard', { replace: true }); }, [token, user, navigate]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const isDonor = form.role === 'DONOR';
  const hasLocation = form.latitude !== '' && form.longitude !== '';

  const submit = async (e) => {
    e.preventDefault();
    setLocalError('');
    if (isDonor && !form.bloodGroup) return setLocalError('Select your blood group.');
    if (isDonor && !hasLocation) return setLocalError('Donors must share a location so nearby requests can reach them.');

    const body = {
      name: form.name, email: form.email, password: form.password, phone: form.phone, role: form.role,
      city: form.city || null,
      bloodGroup: isDonor ? form.bloodGroup : null,
      latitude: isDonor && hasLocation ? Number(form.latitude) : null,
      longitude: isDonor && hasLocation ? Number(form.longitude) : null,
      lastDonationDate: isDonor && form.lastDonationDate ? form.lastDonationDate : null,
    };
    setBusy(true);
    await dispatch(register(body));
    setBusy(false);
  };

  return (
    <main className="auth">
      <form className="panel auth-card wide" onSubmit={submit}>
        <h1>Create your account</h1>

        <div className="seg" role="radiogroup" aria-label="I want to">
          <button type="button" role="radio" aria-checked={isDonor}
                  className={isDonor ? 'on' : ''} onClick={() => setForm({ ...form, role: 'DONOR' })}>
            I want to donate
          </button>
          <button type="button" role="radio" aria-checked={!isDonor}
                  className={!isDonor ? 'on' : ''} onClick={() => setForm({ ...form, role: 'REQUESTER' })}>
            I need blood
          </button>
        </div>

        {(error || localError) && <div className="alert alert-error" role="alert">{localError || error}</div>}

        <div className="grid2">
          <label>Full name
            <input required value={form.name} onChange={set('name')} autoComplete="name" />
          </label>
          <label>Phone
            <input required value={form.phone} onChange={set('phone')} inputMode="tel" autoComplete="tel"
                   placeholder="9876543210" />
          </label>
          <label>Email
            <input type="email" required value={form.email} onChange={set('email')} autoComplete="email" />
          </label>
          <label>Password
            <input type="password" required minLength={6} value={form.password} onChange={set('password')}
                   autoComplete="new-password" />
          </label>
          <label>City
            <input value={form.city} onChange={set('city')} placeholder="Mohali" />
          </label>
          {isDonor && (
            <label>Blood group
              <select value={form.bloodGroup} onChange={set('bloodGroup')} required>
                <option value="">Select</option>
                {BLOOD_GROUPS.map((g) => <option key={g}>{g}</option>)}
              </select>
            </label>
          )}
        </div>

        {isDonor && (
          <>
            <label>Last donation date <span className="muted">(leave empty if you have never donated)</span>
              <input type="date" max={new Date().toISOString().slice(0, 10)}
                     value={form.lastDonationDate} onChange={set('lastDonationDate')} />
            </label>

            <fieldset className="loc-box">
              <legend>Your location</legend>
              <LocationButton done={hasLocation}
                              onLocate={(c) => setForm((f) => ({ ...f, latitude: c.latitude, longitude: c.longitude }))} />
              <div className="grid2">
                <label>Latitude
                  <input type="number" step="any" value={form.latitude} onChange={set('latitude')} />
                </label>
                <label>Longitude
                  <input type="number" step="any" value={form.longitude} onChange={set('longitude')} />
                </label>
              </div>
              <p className="muted small">Only used to calculate distance to hospitals. Other users never see your coordinates.</p>
            </fieldset>
          </>
        )}

        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="muted small">Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </main>
  );
}
