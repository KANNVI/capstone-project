import { useEffect } from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProfile } from './store/authSlice';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import CreateRequest from './pages/CreateRequest';
import RequestDetails from './pages/RequestDetails';
import Profile from './pages/Profile';
import History from './pages/History';
import Notifications from './pages/Notifications';

export default function App() {
  const dispatch = useDispatch();
  const { token, user } = useSelector((s) => s.auth);

  // page refresh: we have a token but not the user yet
  useEffect(() => { if (token && !user) dispatch(fetchProfile()); }, [token, user, dispatch]);

  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/requests/new" element={<ProtectedRoute roles={['REQUESTER']}><CreateRequest /></ProtectedRoute>} />
        <Route path="/requests/:id" element={<ProtectedRoute roles={['REQUESTER', 'ADMIN']}><RequestDetails /></ProtectedRoute>} />
        <Route path="/history" element={<ProtectedRoute roles={['DONOR']}><History /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute roles={['DONOR', 'REQUESTER']}><Profile /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
