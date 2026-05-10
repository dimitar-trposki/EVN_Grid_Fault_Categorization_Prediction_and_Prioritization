import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authStore';
import './Navbar.css';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <h1 className="nav-logo">⚡ EVN Grid System</h1>
            <div className="nav-right">
                <span className="nav-welcome">
                    Welcome, {user?.firstName || 'User'}
                </span>
                <button className="nav-link-btn" onClick={() => navigate('/dashboard')}>
                    Dashboard
                </button>
                <button className="nav-link-btn" onClick={() => navigate('/faults')}>
                    Faults
                </button>
                <button className="nav-logout-btn" onClick={handleLogout}>
                    Logout
                </button>
            </div>
        </nav>
    );
};

export default Navbar;
