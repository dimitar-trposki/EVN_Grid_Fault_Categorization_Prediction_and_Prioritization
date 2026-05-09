import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import dashboardRepository from '../../api/dashboardRepository';

const DashboardPage = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [activeFaults, setActiveFaults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [statsRes, faultsRes] = await Promise.all([
                    dashboardRepository.getStats(),
                    dashboardRepository.getActiveFaults(),
                ]);
                setStats(statsRes.data);
                setActiveFaults(faultsRes.data);
            } catch (err) {
                console.error('Failed to load dashboard', err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const getPriorityColor = (priority) => {
        switch (priority) {
            case 'CRITICAL': return '#ef4444';
            case 'HIGH': return '#f97316';
            case 'MEDIUM': return '#eab308';
            case 'LOW': return '#22c55e';
            default: return '#94a3b8';
        }
    };

    if (loading) return (
        <div style={styles.loadingContainer}>
            <p style={{ color: '#94a3b8' }}>Loading dashboard...</p>
        </div>
    );

    return (
        <div style={styles.container}>
            {/* Navbar */}
            <nav style={styles.navbar}>
                <h1 style={styles.logo}>⚡ EVN Grid System</h1>
                <div style={styles.navRight}>
          <span style={styles.welcome}>
            Welcome, {user?.firstName || 'User'}
          </span>
                    <button style={styles.logoutBtn} onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </nav>

            <div style={styles.content}>
                {/* Stats Cards */}
                <div style={styles.statsGrid}>
                    <div style={styles.statCard}>
                        <p style={styles.statLabel}>Active Faults</p>
                        <h2 style={styles.statValue}>{stats?.activeFaults ?? '—'}</h2>
                    </div>
                    <div style={styles.statCard}>
                        <p style={styles.statLabel}>Critical</p>
                        <h2 style={{ ...styles.statValue, color: '#ef4444' }}>
                            {stats?.criticalFaults ?? '—'}
                        </h2>
                    </div>
                    <div style={styles.statCard}>
                        <p style={styles.statLabel}>Crews Active</p>
                        <h2 style={{ ...styles.statValue, color: '#22c55e' }}>
                            {stats?.activeCrews ?? '—'}
                        </h2>
                    </div>
                    <div style={styles.statCard}>
                        <p style={styles.statLabel}>Avg Response</p>
                        <h2 style={styles.statValue}>
                            {stats?.avgResponseTime ?? '—'} min
                        </h2>
                    </div>
                </div>

                {/* Active Faults Table */}
                <div style={styles.section}>
                    <h2 style={styles.sectionTitle}>Active Faults</h2>
                    {activeFaults.length === 0 ? (
                        <p style={{ color: '#94a3b8' }}>No active faults</p>
                    ) : (
                        <table style={styles.table}>
                            <thead>
                            <tr>
                                <th style={styles.th}>Code</th>
                                <th style={styles.th}>Location</th>
                                <th style={styles.th}>Type</th>
                                <th style={styles.th}>Priority</th>
                                <th style={styles.th}>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            {activeFaults.map((fault) => (
                                <tr key={fault.id} style={styles.tr}>
                                    <td style={styles.td}>{fault.reportCode}</td>
                                    <td style={styles.td}>{fault.locationAddress}</td>
                                    <td style={styles.td}>{fault.faultType}</td>
                                    <td style={styles.td}>
                      <span style={{
                          ...styles.badge,
                          backgroundColor: getPriorityColor(fault.priority),
                      }}>
                        {fault.priority}
                      </span>
                                    </td>
                                    <td style={styles.td}>{fault.status}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

const styles = {
    container: {
        minHeight: '100vh',
        backgroundColor: '#0f172a',
    },
    loadingContainer: {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#0f172a',
    },
    navbar: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '1rem 2rem',
        backgroundColor: '#1e293b',
        borderBottom: '1px solid #334155',
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
    logoutBtn: {
        padding: '0.5rem 1rem',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#ef4444',
        color: 'white',
        cursor: 'pointer',
    },
    content: {
        padding: '2rem',
    },
    statsGrid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '1rem',
        marginBottom: '2rem',
    },
    statCard: {
        backgroundColor: '#1e293b',
        padding: '1.5rem',
        borderRadius: '12px',
        border: '1px solid #334155',
    },
    statLabel: {
        color: '#94a3b8',
        margin: '0 0 0.5rem 0',
        fontSize: '0.9rem',
    },
    statValue: {
        color: '#f1f5f9',
        margin: 0,
        fontSize: '2rem',
    },
    section: {
        backgroundColor: '#1e293b',
        padding: '1.5rem',
        borderRadius: '12px',
        border: '1px solid #334155',
    },
    sectionTitle: {
        color: '#f1f5f9',
        marginTop: 0,
        marginBottom: '1rem',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
    },
    th: {
        color: '#94a3b8',
        textAlign: 'left',
        padding: '0.75rem',
        borderBottom: '1px solid #334155',
        fontSize: '0.9rem',
    },
    tr: {
        borderBottom: '1px solid #1e293b',
    },
    td: {
        color: '#f1f5f9',
        padding: '0.75rem',
    },
    badge: {
        padding: '0.25rem 0.75rem',
        borderRadius: '999px',
        color: 'white',
        fontSize: '0.8rem',
        fontWeight: 'bold',
    },
};

export default DashboardPage;