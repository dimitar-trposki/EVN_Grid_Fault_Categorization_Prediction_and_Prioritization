import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import dashboardRepository from '../../api/dashboardRepository';
import Navbar from '../../components/Navbar';
import './Dashboard.css';

const DashboardPage = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [activeFaults, setActiveFaults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [statsRes, faultsRes] = await Promise.all([
                    dashboardRepository.getKpis(),
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
        <div className="dashboard-loading">
            <p>Loading dashboard...</p>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />

            <div className="dashboard-content">
                {/* Stats Cards */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <p className="stat-label">Active Faults</p>
                        <h2 className="stat-value">{stats?.totalActiveFaults ?? '—'}</h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Critical</p>
                        <h2 className="stat-value" style={{ color: '#ef4444' }}>
                            {stats?.criticalFaults ?? '—'}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Crews Active</p>
                        <h2 className="stat-value" style={{ color: '#22c55e' }}>
                            {stats?.crewsActive ?? '—'}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Avg Response</p>
                        <h2 className="stat-value">
                            {stats?.avgResponseTimeMin ? Math.round(stats.avgResponseTimeMin) : '—'} min
                        </h2>
                    </div>
                </div>

                {/* Active Faults Table */}
                <div className="dashboard-section">
                    <h2 className="section-title">Active Faults</h2>
                    {activeFaults.length === 0 ? (
                        <p style={{ color: '#94a3b8' }}>No active faults</p>
                    ) : (
                        <div style={{ overflowX: 'auto' }}>
                            <table className="dashboard-table">
                                <thead>
                                    <tr>
                                        <th>Code</th>
                                        <th>Location</th>
                                        <th>Type</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {activeFaults.map((fault) => (
                                        <tr key={fault.id} className="clickable-row" onClick={() => navigate(`/faults/${fault.id}`)}>
                                            <td>{fault.id}</td>
                                            <td>{`${fault.latitude?.toFixed?.(4) ?? '—'}, ${fault.longitude?.toFixed?.(4) ?? '—'}`}</td>
                                            <td>{fault.faultTypeName || '—'}</td>
                                            <td>
                                                <span className="dashboard-badge" style={{ backgroundColor: getPriorityColor(fault.priorityLevel) }}>
                                                    {fault.priorityLevel ?? 'N/A'}
                                                </span>
                                            </td>
                                            <td>{fault.status ?? '—'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;