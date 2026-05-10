import { useState, useEffect } from 'react';
import crewRepository from '../../api/crewRepository';
import Navbar from '../../components/Navbar';
import './Crews.css';

const CrewDirectoryPage = () => {
    const [crews, setCrews] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
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
        fetchCrews();
    }, []);

    if (loading) return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-loading">
                <p>Loading crew directory...</p>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-content">
                <h1 className="section-title">Crew Directory</h1>
                
                <div className="stats-grid">
                    <div className="stat-card">
                        <p className="stat-label">Total Crews</p>
                        <h2 className="stat-value">{crews.length}</h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Crews Idle</p>
                        <h2 className="stat-value" style={{ color: '#22c55e' }}>
                            {crews.filter(c => c.status === 'IDLE').length}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Crews Working</p>
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
                                    <th>ID</th>
                                    <th>Crew Name</th>
                                    <th>Region</th>
                                    <th>Status</th>
                                    <th>Members</th>
                                    <th>Last Location</th>
                                </tr>
                            </thead>
                            <tbody>
                                {crews.map((crew) => (
                                    <tr key={crew.id}>
                                        <td>#{crew.id}</td>
                                        <td style={{ fontWeight: 600, color: '#f1f5f9' }}>{crew.name}</td>
                                        <td>{crew.regionName}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: crew.status === 'IDLE' ? '#22c55e' : '#3b82f6' }}>
                                                {crew.status}
                                            </span>
                                        </td>
                                        <td>{crew.crewMembers?.length || 0} members</td>
                                        <td>{crew.latitude?.toFixed(4)}, {crew.longitude?.toFixed(4)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CrewDirectoryPage;
