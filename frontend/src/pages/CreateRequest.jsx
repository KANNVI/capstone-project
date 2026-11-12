import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { createRequest } from '../store/requestSlice';
import LocationButton from '../components/LocationButton';
import { BLOOD_GROUPS, URGENCIES } from '../utils/constants';

export default function CreateRequest() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((s) => s.auth.user);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    patientName: '', bloodGroup: '', unitsRequired: 1, hospital: '', city: user.city || '',
    latitude: '', longitude: '', urgency: 'HIGH', contactPhone: user.phone || '', notes: '',
  });
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const hasLocation = form.latitude !== '' && form.longitude !== '';

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!hasLocation) return setError('Add the hospital location so we can find nearby donors.');
    setBusy(true);
    try {
      const created = await dispatch(createRequest({
        ...form,
        unitsRequired: Number(form.unitsRequired),
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
      })).unwrap();
      navigate(`/requests/${created.id}`);
    } catch (msg) {
      setError(typeof msg === 'string' ? msg : 'Could not create the request');
      setBusy(false);
    }
  };

  return (
    <main className="page narrow">
      <h1>Create blood request</h1>
      <form className="panel stack" onSubmit={submit}>
        {error && <div className="alert alert-error" role="alert">{error}</div>}

        <div className="grid2">
          <label>Blood group needed
            <select required value={form.bloodGroup} onChange={set('bloodGroup')}>
              <option value="">Select</option>
              {BLOOD_GROUPS.map((g) => <option key={g}>{g}</option>)}
            </select>
          </label>
          <label>Units required
            <input type="number" min="1" max="10" required value={form.unitsRequired} onChange={set('unitsRequired')} />
          </label>
          <label>Urgency
            <select value={form.urgency} onChange={set('urgency')}>
              {URGENCIES.map((u) => <option key={u} value={u}>{u.charAt(0) + u.slice(1).toLowerCase()}</option>)}
            </select>
          </label>
          <label>Patient name <span className="muted">(optional)</span>
            <input value={form.patientName} onChange={set('patientName')} />
          </label>
          <label>Hospital
            <input required value={form.hospital} onChange={set('hospital')} placeholder="City Hospital, Phase 6" />
          </label>
          <label>City
            <input value={form.city} onChange={set('city')} />
          </label>
        </div>

        <label>Contact phone <span className="muted">(shown to a donor only after they accept)</span>
          <input required value={form.contactPhone} onChange={set('contactPhone')} inputMode="tel" />
        </label>

        <fieldset className="loc-box">
          <legend>Hospital location</legend>
          <LocationButton label="Use my current location (I am at the hospital)" done={hasLocation}
                          onLocate={(c) => setForm((f) => ({ ...f, latitude: c.latitude, longitude: c.longitude }))} />
          <div className="grid2">
            <label>Latitude
              <input type="number" step="any" value={form.latitude} onChange={set('latitude')} />
            </label>
            <label>Longitude
              <input type="number" step="any" value={form.longitude} onChange={set('longitude')} />
            </label>
          </div>
          <p className="muted small">Not at the hospital? Copy the coordinates from Google Maps (right-click the hospital, click the numbers).</p>
        </fieldset>

        <label>Notes <span className="muted">(optional)</span>
          <textarea rows="3" maxLength="500" value={form.notes} onChange={set('notes')}
                    placeholder="Ward, bed number, doctor's name" />
        </label>

        <button className="btn btn-primary btn-lg" disabled={busy}>{busy ? 'Finding donors…' : 'Send request to nearby donors'}</button>
      </form>
    </main>
  );
}
