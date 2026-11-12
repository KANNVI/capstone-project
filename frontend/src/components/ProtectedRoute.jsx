import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

export default function ProtectedRoute({ roles, children }) {
  const { token, user, loading } = useSelector((s) => s.auth);
  if (!token) return <Navigate to="/login" replace />;
  if (loading || !user) return <div className="page-center">Loading…</div>;
  if (roles && !roles.includes(user.role)) return <Navigate to="/dashboard" replace />;
  return children;
}
