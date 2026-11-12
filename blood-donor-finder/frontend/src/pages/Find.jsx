import { useEffect, useState } from 'react';
import { api, GROUPS, CITIES } from '../api.js';

export default function Find() {
  const [q, setQ] = useState({ group: 'A+', city: 'Delhi', radius: 50 });
  const [list, setList] = useState([]);
  const [err, setErr] = useState('');

  useEffect(() => {
    api(`/donors/search?group=${encodeURIComponent(q.group)}&city=${q.city}&radius=${q.radius}`)
      .then((d) => { setList(d); setErr(''); }).catch((e) => setErr(e.message));
  }, [q]);

  const set = (k, num) => (e) => setQ({ ...q, [k]: num ? +e.target.value : e.target.value });

  return (
    <>
      <h2>Find compatible donors</h2>
      <div className="card grid">
        <div><label>Blood group needed</label>
          <select value={q.group} onChange={set('group')}>{GROUPS.map((g) => <option key={g}>{g}</option>)}</select></div>
        <div><label>City</label>
          <select value={q.city} onChange={set('city')}>{CITIES.map((c) => <option key={c}>{c}</option>)}</select></div>
        <div><label>Radius (km)</label>
          <select value={q.radius} onChange={set('radius', true)}>{[10, 25, 50, 100, 300].map((r) => <option key={r}>{r}</option>)}</select></div>
      </div>
      <p className="mu">Only verified, available, eligible donors are shown, nearest first. Phone numbers stay masked until a donor accepts your request.</p>
      {err && <p className="err">{err}</p>}
      {list.map((d) => (
        <div className="card row" key={d.id}>
          <div className="row"><span className="bg">{d.bloodGroup}</span>
            <div><b>{d.name}</b><div className="mu">{d.city} · {d.distanceKm} km away</div></div></div>
          <span className="mu">📞 {d.phone}</span>
        </div>
      ))}
      {!err && list.length === 0 && <div className="card mu">No eligible donors found. Try a larger radius or post a blood request.</div>}
    </>
  );
}
