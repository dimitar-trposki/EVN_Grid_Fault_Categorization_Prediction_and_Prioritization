import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authStore';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <nav style={styles.navbar}>
            <h1 style={styles.logo}>⚡ EVN Grid System</h1>
            <div style={styles.navRight}>
                <span style={styles.welcome}>
                    Welcome, {user?.firstName || 'User'}
                </span>
                <button style={styles.navLinkBtn} onClick={() => navigate('/dashboard')}>
                    Dashboard
                </button>
                <button style={styles.navLinkBtn} onClick={() => navigate('/faults')}>
                    Faults
                </button>
                <button style={styles.logoutBtn} onClick={handleLogout}>
                    Logout
                </button>
            </div>
        </nav>
    );
};

const styles = {
    navbar: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '1rem 2rem',
        backgroundColor: '#1e293b',
        borderBottom: '1px solid #334155',
        position: 'sticky',
        top: 0,
        zIndex: 50,
    },
    logo: {
        color: '#f1f5f9',
        fontSize: '1.3rem',
        margin: 0,
    },
    navRight: {
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
    },
    welcome: {
        color: '#94a3b8',
    },
    navLinkBtn: {
        padding: '0.5rem 1rem',
        borderRadius: '8px',
        border: '1px solid #c084fc',
        backgroundColor: 'transparent',
        color: '#c084fc',
        cursor: 'pointer',
    },
    logoutBtn: {
        padding: '0.5rem 1rem',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#ef4444',
        color: 'white',
        cursor: 'pointer',
    },
};

export default Navbar;
