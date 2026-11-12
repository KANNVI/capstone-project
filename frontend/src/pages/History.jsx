import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchHistory } from '../store/donorSlice';
import { GroupTag } from '../components/Badges';
import { fmtDay } from '../utils/constants';

export default function History() {
  const dispatch = useDispatch();
  const history = useSelector((s) => s.donor.history);
  useEffect(() => { dispatch(fetchHistory()); }, [dispatch]);

  const total = history.reduce((n, h) => n + h.units, 0);

  return (
    <main className="page narrow">
      <h1>My donations</h1>
      <p className="muted">{total} unit{total === 1 ? '' : 's'} donated through BloodConnect.</p>
      {history.length === 0 ? (
        <div className="empty"><p>No donations recorded yet. Completed donations appear here.</p></div>
      ) : (
        <div className="panel table-wrap">
          <table>
            <thead><tr><th>Date</th><th>Hospital</th><th>Patient group</th><th>Units</th></tr></thead>
            <tbody>
              {history.map((h) => (
                <tr key={h.id}>
                  <td>{fmtDay(h.donationDate)}</td>
                  <td>{h.hospital || '-'}</td>
                  <td>{h.patientBloodGroup ? <GroupTag group={h.patientBloodGroup} /> : '-'}</td>
                  <td>{h.units}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}
