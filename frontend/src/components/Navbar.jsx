import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../store/authSlice';
import { fetchUnread } from '../store/notificationSlice';
import usePolling from '../utils/usePolling';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((s) => s.auth.user);
  const unread = useSelector((s) => s.notifications.unread);

  usePolling(() => { if (user) dispatch(fetchUnread()); }, 20000);

  const onLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  return (
    <header className="nav">
      <div className="nav-inner">
        <Link to={user ? '/dashboard' : '/'} className="brand">
          <svg width="20" height="20" viewBox="0 0 32 32" aria-hidden="true">
            <path fill="currentColor" d="M16 2C16 2 6 14 6 20a10 10 0 0 0 20 0C26 14 16 2 16 2z" />
          </svg>
          BloodConnect
        </Link>

        <nav className="nav-links">
          {user ? (
            <>
              <NavLink to="/dashboard">Dashboard</NavLink>
              {user.role === 'REQUESTER' && <NavLink to="/requests/new">New request</NavLink>}
              {user.role === 'DONOR' && <NavLink to="/history">My donations</NavLink>}
              {user.role !== 'ADMIN' && <NavLink to="/profile">Profile</NavLink>}
              <NavLink to="/notifications" className="bell">
                Notifications{unread > 0 && <span className="count">{unread}</span>}
              </NavLink>
              <button className="btn btn-quiet" onClick={onLogout}>Log out</button>
            </>
          ) : (
            <>
              <NavLink to="/login">Log in</NavLink>
              <Link to="/register" className="btn btn-primary">Register</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
