import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import { useAuth } from './context/authStore';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import FaultsListPage from './pages/faults/FaultsListPage';
import FaultSubmissionPage from './pages/faults/FaultSubmissionPage';
import FaultDetailsPage from './pages/faults/FaultDetailsPage';
import EquipmentPage from './pages/equipment/EquipmentPage';

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" />;
};

const PublicRoute = ({ children }) => {
  const { user } = useAuth();
  return user ? <Navigate to="/dashboard" /> : children;
};

const App = () => {
  return (
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route
              path="/login"
              element={
                <PublicRoute>
                  <LoginPage />
                </PublicRoute>
              }
            />
            <Route
              path="/register"
              element={
                <PublicRoute>
                  <RegisterPage />
                </PublicRoute>
              }
            />

            <Route path="/dashboard" element={
              <ProtectedRoute>
                <DashboardPage /> {}
              </ProtectedRoute>
            } />

            <Route path="/faults" element={
              <ProtectedRoute>
                <FaultsListPage />
              </ProtectedRoute>
            } />

            <Route path="/equipment" element={
              <ProtectedRoute>
                <EquipmentPage />
              </ProtectedRoute>
            } />

            <Route path="/faults/new" element={
              <ProtectedRoute>
                <FaultSubmissionPage />
              </ProtectedRoute>
            } />

            <Route path="/faults/:id" element={
              <ProtectedRoute>
                <FaultDetailsPage />
              </ProtectedRoute>
            } />

            <Route path="*" element={<Navigate to="/dashboard" />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
  );
};

export default App;