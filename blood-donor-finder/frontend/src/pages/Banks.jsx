import { useEffect, useState } from 'react';
import { api, GROUPS } from '../api.js';

export default function Banks({ isAdmin }) {
  const [banks, setBanks] = useState([]);
  const [err, setErr] = useState('');
  useEffect(() => { api('/banks').then(setBanks).catch((e) => setErr(e.message)); }, []);

  const adjust = (id, g, delta) =>
    api(`/banks/${id}/stock/${encodeURIComponent(g)}?delta=${delta}`, { method: 'PUT' })
      .then((b) => setBanks((all) => all.map((x) => (x.id === b.id ? b : x)))).catch((e) => setErr(e.message));

  return (
    <>
      <h2>Blood banks &amp; hospitals</h2>
      {err && <p className="err">{err}</p>}
      {banks.map((b) => (
        <div className="card" key={b.id}>
          <b>{b.name}</b><div className="mu">{b.city} · 📞 {b.phone}</div>
          <div className="grid stock">
            {GROUPS.map((g) => (
              <div className="cell" key={g}>
                <b className={b.stock[g] === 0 ? 'warn' : 'bgc'}>{g}</b>
                <div>{b.stock[g] ?? 0} units</div>
                {isAdmin && <div><button className="btn s sm" onClick={() => adjust(b.id, g, 1)}>+</button> <button className="btn s sm" onClick={() => adjust(b.id, g, -1)}>−</button></div>}
              </div>
            ))}
          </div>
        </div>
      ))}
      {!isAdmin && <p className="mu">Blood bank staff (admin) can update stock after signing in.</p>}
    </>
  );
}
