import { useEffect, useState } from 'react';
import { api, GROUPS, CITIES } from '../api.js';

const blank = { name: '', bloodGroup: 'O+', age: 25, weight: 60, phone: '', city: 'Gurugram', lastDonation: '' };

export default function DonorProfile() {
  const [form, setForm] = useState(blank);
  const [mine, setMine] = useState([]);
  const [err, setErr] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const load = () => api('/donors/mine').then(setMine).catch((e) => setErr(e.message));
  useEffect(() => { load(); }, []);

  const submit = async (e) => {
    e.preventDefault(); setErr('');
    try {
      await api('/donors', { method: 'POST', body: { ...form, age: +form.age, weight: +form.weight, lastDonation: form.lastDonation || null } });
      setForm(blank); load();
    } catch (ex) { setErr(ex.message); }
  };
  const toggle = (id) => api(`/donors/${id}/availability`, { method: 'PATCH' }).then(load).catch((e) => setErr(e.message));

  return (
    <>
      <h2>Register as a donor</h2>
      <form className="card" onSubmit={submit}>
        <div className="grid">
          <div><label>Full name</label><input value={form.name} onChange={set('name')} required /></div>
          <div><label>Blood group</label><select value={form.bloodGroup} onChange={set('bloodGroup')}>{GROUPS.map((g) => <option key={g}>{g}</option>)}</select></div>
          <div><label>Age</label><input type="number" value={form.age} onChange={set('age')} /></div>
          <div><label>Weight (kg)</label><input type="number" value={form.weight} onChange={set('weight')} /></div>
          <div><label>Phone (10 digits)</label><input value={form.phone} onChange={set('phone')} pattern="\d{10}" required /></div>
          <div><label>City</label><select value={form.city} onChange={set('city')}>{CITIES.map((c) => <option key={c}>{c}</option>)}</select></div>
          <div><label>Last donation (optional)</label><input type="date" value={form.lastDonation} onChange={set('lastDonation')} /></div>
        </div>
        <p className="mu">You appear in searches only after an admin verifies your profile.</p>
        <button className="btn">Register</button>
      </form>
      {err && <p className="err">{err}</p>}
      <h2>My donor profiles</h2>
      {mine.length === 0 && <div className="card mu">No profile yet.</div>}
      {mine.map((d) => (
        <div className="card row" key={d.id}>
          <div className="row"><span className="bg">{d.bloodGroup}</span>
            <div><b>{d.name}</b><div className="mu">{d.city} · {d.age} yrs · {d.weight} kg</div>
              <div className={d.eligible ? 'ok' : 'warn'}>
                {d.eligible ? 'Eligible to donate' : !d.verified ? 'Awaiting admin verification' : !d.available ? 'Marked unavailable' : 'Not currently eligible (age, weight or donated within 90 days)'}
              </div></div></div>
          <button className="btn s sm" onClick={() => toggle(d.id)}>{d.available ? 'Set unavailable' : 'Set available'}</button>
        </div>
      ))}
    </>
  );
}
