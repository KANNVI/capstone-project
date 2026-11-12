import { useEffect, useState } from 'react';
import { api, GROUPS, CITIES } from '../api.js';

const blank = { patient: '', bloodGroup: 'A+', units: 1, hospital: '', city: 'Delhi', urgency: 'Critical', requiredBy: '' };

export default function Requests() {
  const [form, setForm] = useState(blank);
  const [reqs, setReqs] = useState([]);
  const [matches, setMatches] = useState({});
  const [err, setErr] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const load = () => api('/requests/mine').then(setReqs).catch((e) => setErr(e.message));
  useEffect(() => { load(); }, []);

  const run = async (fn) => { try { setErr(''); await fn(); } catch (e) { setErr(e.message); } };
  const loadMatches = (id) => run(async () => { const r = await api(`/requests/${id}/matches`); setMatches((m) => ({ ...m, [id]: r })); });
  const act = (id, path) => run(async () => { await api(`/requests/${id}/${path}`, { method: 'POST' }); await loadMatches(id); await load(); });
  const close = (id, v) => run(async () => { await api(`/requests/${id}/status?value=${v}`, { method: 'POST' }); await load(); });
  const create = (e) => { e.preventDefault(); run(async () => { await api('/requests', { method: 'POST', body: { ...form, units: +form.units } }); setForm(blank); await load(); }); };

  return (
    <>
      <h2>Post an emergency blood request</h2>
      <form className="card" onSubmit={create}>
        <div className="grid">
          <div><label>Patient name</label><input value={form.patient} onChange={set('patient')} required /></div>
          <div><label>Blood group</label><select value={form.bloodGroup} onChange={set('bloodGroup')}>{GROUPS.map((g) => <option key={g}>{g}</option>)}</select></div>
          <div><label>Units</label><input type="number" min="1" value={form.units} onChange={set('units')} /></div>
          <div><label>Hospital</label><input value={form.hospital} onChange={set('hospital')} required /></div>
          <div><label>City</label><select value={form.city} onChange={set('city')}>{CITIES.map((c) => <option key={c}>{c}</option>)}</select></div>
          <div><label>Urgency</label><select value={form.urgency} onChange={set('urgency')}>{['Critical', 'Urgent', 'Planned'].map((u) => <option key={u}>{u}</option>)}</select></div>
          <div><label>Required by</label><input type="datetime-local" value={form.requiredBy} onChange={set('requiredBy')} /></div>
        </div>
        <p><button className="btn">Post request</button></p>
      </form>
      {err && <p className="err">{err}</p>}

      <h2>My requests</h2>
      {reqs.length === 0 && <div className="card mu">No requests yet.</div>}
      {reqs.map((r) => (
        <div className="card" key={r.id}>
          <div className="row">
            <div className="row"><span className="bg">{r.bloodGroup}</span>
              <div><b>{r.patient}</b> · {r.units} unit(s)<div className="mu">{r.hospital}, {r.city}</div></div></div>
            <div><span className="tag">{r.urgency}</span> <span className="tag">{r.status}</span> {!r.verified && <span className="tag warn">awaiting verification</span>}</div>
          </div>
          {r.status === 'OPEN' && (
            <div className="sub">
              <button className="btn s sm" onClick={() => loadMatches(r.id)}>Show matching donors</button>{' '}
              <button className="btn s sm" onClick={() => close(r.id, 'FULFILLED')}>Mark fulfilled</button>{' '}
              <button className="btn s sm" onClick={() => close(r.id, 'CLOSED')}>Close</button>
              {matches[r.id] && (
                <>
                  <p className="row"><b>{matches[r.id].length} matching donor(s) within 100 km</b>
                    {matches[r.id].length > 0 && <button className="btn sm" onClick={() => act(r.id, 'alert-all')}>Alert all</button>}</p>
                  {matches[r.id].map((d) => (
                    <div className="row line" key={d.id}>
                      <span><b>{d.bloodGroup}</b> {d.name} <span className="mu">· {d.distanceKm} km</span></span>
                      {d.accepted ? <span className="ok">✔ Accepted · 📞 {d.phone}</span>
                        : d.alerted ? <button className="btn s sm" onClick={() => act(r.id, `accept/${d.id}`)}>Simulate donor accepting</button>
                        : <button className="btn s sm" onClick={() => act(r.id, `alert/${d.id}`)}>Send alert</button>}
                    </div>
                  ))}
                </>
              )}
            </div>
          )}
        </div>
      ))}
    </>
  );
}
