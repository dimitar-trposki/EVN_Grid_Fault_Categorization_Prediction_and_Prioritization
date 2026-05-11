import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/authStore';
import notificationRepository from '../api/notificationRepository';
import './Navbar.css';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    
    // Notifications state
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);
    const [isNotifOpen, setIsNotifOpen] = useState(false);
    const [loadingNotifs, setLoadingNotifs] = useState(false);
    const dropdownRef = useRef(null);

    // Handle global clicks outside the dropdown to close it
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsNotifOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Fetch unread count every 60s
    const loadCount = async () => {
        try {
            const res = await notificationRepository.getUnreadCount();
            setUnreadCount(res.data);
        } catch (e) {
            console.error("Fail to update counter", e);
        }
    };

    useEffect(() => {
        if (user) {
            loadCount();
            const interval = setInterval(loadCount, 60000);
            return () => clearInterval(interval);
        }
    }, [user]);

    const handleToggleDropdown = async () => {
        const newState = !isNotifOpen;
        setIsNotifOpen(newState);

        if (newState) {
            setLoadingNotifs(true);
            try {
                const res = await notificationRepository.getUnread();
                setNotifications(res.data || []);
            } catch (e) {
                console.error(e);
            } finally {
                setLoadingNotifs(false);
            }
        }
    };

    const handleMarkRead = async (id) => {
        try {
            await notificationRepository.markRead(id);
            setNotifications(prev => prev.filter(n => n.id !== id));
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (e) {
            console.error(e);
        }
    };

    const handleMarkAll = async () => {
        try {
            await notificationRepository.markAllRead();
            setNotifications([]);
            setUnreadCount(0);
        } catch (e) {
            console.error(e);
        }
    };

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

                {/* Notification Dropdown UI */}
                <div className="nav-notification-container" ref={dropdownRef}>
                    <button className="nav-bell-btn" onClick={handleToggleDropdown}>
                        🔔
                        {unreadCount > 0 && <span className="bell-badge">{unreadCount}</span>}
                    </button>

                    {isNotifOpen && (
                        <div className="notif-dropdown">
                            <div className="notif-header">
                                <span>Recent Alerts</span>
                                {notifications.length > 0 && (
                                    <button className="clear-all-btn" onClick={handleMarkAll}>Clear All</button>
                                )}
                            </div>
                            <div className="notif-body">
                                {loadingNotifs ? (
                                    <p className="notif-msg">Loading...</p>
                                ) : notifications.length === 0 ? (
                                    <p className="notif-msg">Inbox clear.</p>
                                ) : (
                                    notifications.map(n => (
                                        <div key={n.id} className="notif-item" onClick={() => handleMarkRead(n.id)}>
                                            <div className="notif-dot"></div>
                                            <div className="notif-content">
                                                <p className="notif-title">{n.title}</p>
                                                <p className="notif-text">{n.message}</p>
                                                <span className="notif-time">{new Date(n.createdAt).toLocaleString()}</span>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    )}
                </div>

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

