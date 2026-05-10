import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/dashboard/DashboardPage';

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

            <Route path="*" element={<Navigate to="/dashboard" />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
  );
};

export default App;