import { useSelector } from 'react-redux';
import DonorDashboard from './DonorDashboard';
import RequesterDashboard from './RequesterDashboard';
import AdminDashboard from './AdminDashboard';

export default function Dashboard() {
  const role = useSelector((s) => s.auth.user.role);
  if (role === 'DONOR') return <DonorDashboard />;
  if (role === 'ADMIN') return <AdminDashboard />;
  return <RequesterDashboard />;
}
