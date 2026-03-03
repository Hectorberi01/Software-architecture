
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import { AppLayout } from './components/layout/AppLayout';
import { ProtectedRoute } from './components/guards/ProtectedRoute';

import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ParkingPage } from './pages/ParkingPage';
import { ReservationsPage } from './pages/ReservationsPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { AdminPage } from './pages/AdminPage';
import { CheckInPage } from './pages/CheckInPage';

function App() {
    return (
        <BrowserRouter>
            <ToastProvider>
                <AuthProvider>
                    <Routes>
                        {/* Routes publiques */}
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/checkin/qr" element={<CheckInPage />} />

                        <Route path="/" element={<Navigate to="/parking" replace />} />

                        {/* Routes privées avec Sidebar */}
                        <Route element={<ProtectedRoute />}>
                            <Route element={<AppLayout />}>
                                <Route path="/parking" element={<ParkingPage />} />
                                <Route path="/reservations" element={<ReservationsPage />} />

                                {/* Roles: Manager & Admin */}
                                <Route element={<ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']} />}>
                                    <Route path="/analytics" element={<AnalyticsPage />} />
                                </Route>

                                {/* Role: Secrétaire (Admin) */}
                                <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                                    <Route path="/admin" element={<AdminPage />} />
                                </Route>
                            </Route>
                        </Route>

                        <Route path="*" element={<Navigate to="/parking" replace />} />
                    </Routes>
                </AuthProvider>
            </ToastProvider>
        </BrowserRouter>
    );
}

export default App;
