import { useDispatch, useSelector } from 'react-redux';
import { fetchNotifications, fetchUnread, markAllRead } from '../store/notificationSlice';
import usePolling from '../utils/usePolling';
import { fmtDate } from '../utils/constants';

export default function Notifications() {
  const dispatch = useDispatch();
  const { items, unread } = useSelector((s) => s.notifications);

  usePolling(() => { dispatch(fetchNotifications()); dispatch(fetchUnread()); }, 15000);

  return (
    <main className="page narrow">
      <div className="page-head">
        <h1>Notifications</h1>
        {unread > 0 && <button className="btn btn-ghost" onClick={() => dispatch(markAllRead())}>Mark all as read</button>}
      </div>
      {items.length === 0 ? (
        <div className="empty"><p>Nothing yet. New blood requests and updates show up here.</p></div>
      ) : (
        <ul className="panel notif">
          {items.map((n) => (
            <li key={n.id} className={n.seen ? '' : 'unseen'}>
              <span>{n.message}</span>
              <span className="muted small">{fmtDate(n.createdAt)}</span>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
