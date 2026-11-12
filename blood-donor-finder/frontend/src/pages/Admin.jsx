import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function Admin() {
  const [stats, setStats] = useState({});
  const [donors, setDonors] = useState([]);
  const [reqs, setReqs] = useState([]);
  const [err, setErr] = useState('');

  const load = () => Promise.all([api('/admin/stats'), api('/admin/donors'), api('/admin/requests')])
    .then(([s, d, r]) => { setStats(s); setDonors(d); setReqs(r); }).catch((e) => setErr(e.message));
  useEffect(() => { load(); }, []);

  const verify = (kind, id, value) =>
    api(`/admin/${kind}/${id}/verify?value=${value}`, { method: 'POST' }).then(load).catch((e) => setErr(e.message));

  const report = () => {
    const rows = [['Type', 'Name', 'Group', 'City', 'Status'],
      ...donors.map((d) => ['Donor', d.name, d.bloodGroup, d.city, d.verified ? 'Verified' : 'Pending']),
      ...reqs.map((r) => ['Request', r.patient, r.bloodGroup, r.city, r.status])];
    const csv = rows.map((r) => r.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n');
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    a.download = 'report.csv'; a.click();
  };

  return (
    <>
      <div className="row"><h2>Admin dashboard</h2><button className="btn s sm" onClick={report}>Download report (CSV)</button></div>
      {err && <p className="err">{err}</p>}
      <div className="grid">
        {[['Donors', stats.donors], ['Eligible now', stats.eligibleDonors], ['Open requests', stats.openRequests], ['Fulfilled', stats.fulfilledRequests]]
          .map(([l, v]) => <div className="card stat" key={l}><b>{v ?? '–'}</b>{l}</div>)}
      </div>
      <div className="card scroll"><h3>Donors</h3>
        <table><thead><tr><th>Name</th><th>Group</th><th>City</th><th>Status</th><th></th></tr></thead>
          <tbody>{donors.map((d) => (
            <tr key={d.id}><td>{d.name}</td><td>{d.bloodGroup}</td><td>{d.city}</td>
              <td className={d.verified ? 'ok' : 'warn'}>{d.verified ? 'Verified' : 'Pending'}</td>
              <td><button className="btn sm" onClick={() => verify('donors', d.id, !d.verified)}>{d.verified ? 'Block' : 'Verify'}</button></td></tr>
          ))}</tbody></table></div>
      <div className="card scroll"><h3>Requests</h3>
        <table><thead><tr><th>Patient</th><th>Group</th><th>Hospital</th><th>Status</th><th></th></tr></thead>
          <tbody>{reqs.map((r) => (
            <tr key={r.id}><td>{r.patient}</td><td>{r.bloodGroup}</td><td>{r.hospital}</td>
              <td className={r.verified ? 'ok' : 'warn'}>{r.status}{r.verified ? '' : ' · unverified'}</td>
              <td><button className="btn sm" onClick={() => verify('requests', r.id, !r.verified)}>{r.verified ? 'Unverify' : 'Verify'}</button></td></tr>
          ))}</tbody></table></div>
    </>
  );
}
