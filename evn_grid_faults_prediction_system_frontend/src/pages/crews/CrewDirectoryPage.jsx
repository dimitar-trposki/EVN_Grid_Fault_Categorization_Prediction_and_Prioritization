import { useState, useEffect } from 'react';
import crewRepository from '../../api/crewRepository';
import Navbar from '../../components/Navbar';
import { useAuth } from '../../context/authStore';
import CrewFormModal from '../../components/CrewFormModal';
import CrewMembersModal from '../../components/CrewMembersModal';
import './Crews.css';

const CrewDirectoryPage = () => {
    const { user } = useAuth();
    const [crews, setCrews] = useState([]);
    const [loading, setLoading] = useState(true);

    // Manage Modals
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [isRosterOpen, setIsRosterOpen] = useState(false);
    const [targetCrew, setTargetCrew] = useState(null);

    const fetchCrews = async () => {
        try {
            const res = await crewRepository.listAll();
            setCrews(res.data);
        } catch (err) {
            console.error('Failed to load crews', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCrews();
    }, []);

    const canManage = user?.role === 'ADMIN' || user?.role === 'DISPATCHER';

    const handleOpenCreate = () => {
        setTargetCrew(null);
        setIsFormOpen(true);
    };

    const handleOpenEdit = (crew) => {
        setTargetCrew(crew);
        setIsFormOpen(true);
    };

    const handleOpenRoster = (crew) => {
        setTargetCrew(crew);
        setIsRosterOpen(true);
    };

    const handleDelete = async (crewId, name) => {
        if (!window.confirm(`Permanently remove "${name}" team registration? This breaks all ongoing mission routing linked to the unit.`)) return;
        try {
            await crewRepository.delete(crewId);
            await fetchCrews();
        } catch (e) {
            alert("Cannot archive operational unit. Remove active assignments first.");
        }
    };

    if (loading && crews.length === 0) return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-loading">
                <p>Synchronizing workforce directories...</p>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <h1 className="section-title" style={{ margin: 0 }}>Workforce / Crew Directory</h1>
                    {canManage && (
                        <button className="primary-btn" onClick={handleOpenCreate}>
                            + Assemble New Crew
                        </button>
                    )}
                </div>
                
                <div className="stats-grid">
                    <div className="stat-card">
                        <p className="stat-label">Total Force Pools</p>
                        <h2 className="stat-value">{crews.length}</h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Idle & Available</p>
                        <h2 className="stat-value" style={{ color: '#22c55e' }}>
                            {crews.filter(c => c.status === 'IDLE').length}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Active Deployments</p>
                        <h2 className="stat-value" style={{ color: '#3b82f6' }}>
                            {crews.filter(c => c.status !== 'IDLE').length}
                        </h2>
                    </div>
                </div>

                <div className="dashboard-section">
                    <div style={{ overflowX: 'auto' }}>
                        <table className="dashboard-table">
                            <thead>
                                <tr>
                                    <th>ID Code</th>
                                    <th>Unit Designation</th>
                                    <th>Current Region</th>
                                    <th>Duty Status</th>
                                    <th>Personnel Size</th>
                                    {canManage && <th>Logistical Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {crews.map((crew) => (
                                    <tr key={crew.id}>
                                        <td><code>{crew.crewCode || `#${crew.id}`}</code></td>
                                        <td style={{ fontWeight: 600, color: '#f1f5f9' }}>{crew.name}</td>
                                        <td>{crew.regionName || 'Stationary'}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ 
                                                backgroundColor: crew.status === 'IDLE' ? '#064e3b' : '#1e3a8a',
                                                color: crew.status === 'IDLE' ? '#34d399' : '#93c5fd'
                                            }}>
                                                {crew.status?.replace('_', ' ') || 'UNKNOWN'}
                                            </span>
                                        </td>
                                        <td>{crew.crewMembers?.length || 0} specialists</td>
                                        {canManage && (
                                            <td>
                                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                    <button 
                                                        className="secondary-btn" 
                                                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                                                        onClick={() => handleOpenEdit(crew)}
                                                    >
                                                        Basic Info
                                                    </button>
                                                    <button 
                                                        className="secondary-btn" 
                                                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', borderColor: '#3b82f6', color: '#60a5fa' }}
                                                        onClick={() => handleOpenRoster(crew)}
                                                    >
                                                        Roster
                                                    </button>
                                                    <button 
                                                        className="secondary-btn" 
                                                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', borderColor: 'rgba(239,68,68,0.3)', color: '#fca5a5' }}
                                                        onClick={() => handleDelete(crew.id, crew.name)}
                                                    >
                                                        Kill
                                                    </button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {isFormOpen && (
                <CrewFormModal 
                    crewItem={targetCrew}
                    onClose={() => setIsFormOpen(false)}
                    onSuccess={fetchCrews}
                />
            )}

            {isRosterOpen && targetCrew && (
                <CrewMembersModal 
                    crewId={targetCrew.id}
                    crewName={targetCrew.name}
                    onClose={() => setIsRosterOpen(false)}
                    onSuccess={fetchCrews}
                />
            )}
        </div>
    );
};

export default CrewDirectoryPage;

