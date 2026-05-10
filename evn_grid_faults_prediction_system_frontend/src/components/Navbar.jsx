import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authStore';
import './Navbar.css';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');


    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const handleSearch = (e) => {
        if (e.key === 'Enter' && searchQuery.trim()) {
            navigate(`/faults?search=${searchQuery.trim()}`);
            setSearchQuery('');
        }
    };

    return (
        <nav className="navbar">
            <h1 className="nav-logo" onClick={() => navigate('/dashboard')} style={{ cursor: 'pointer' }}>
                ⚡ EVN Grid System
            </h1>
            
            <div className="nav-center">
                <input 
                    type="text" 
                    className="nav-search" 
                    placeholder="Search Tracking Code or Title..." 
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={handleSearch}
                />
            </div>

            <div className="nav-right">
                <button className="nav-link-btn" onClick={() => navigate('/dashboard')}>
                    Dashboard
                </button>
                <button className="nav-link-btn" onClick={() => navigate('/faults')}>
                    Faults
                </button>
                {user?.role !== 'CUSTOMER' && (
                    <>
                        <button className="nav-link-btn" onClick={() => navigate('/crews')}>
                            Crews
                        </button>
                        <button className="nav-link-btn" onClick={() => navigate('/equipment')}>
                            Equipment
                        </button>
                    </>
                )}

                <div className="user-profile">
                    <span className="nav-welcome">
                        {user?.firstName || 'User'}
                    </span>
                    <button className="nav-logout-btn" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>
        </nav>
    );
};


export default Navbar;
