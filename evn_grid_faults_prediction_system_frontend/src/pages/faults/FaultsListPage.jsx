import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/authStore';
import faultRepository from '../../api/faultRepository';
import Navbar from '../../components/Navbar';
import './Faults.css';

const FaultsListPage = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [faults, setFaults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchFaults();
    }, []);

    const fetchFaults = async () => {
        setLoading(true);
        try {
            let res;
            if (user?.role === 'CUSTOMER') {
                res = await faultRepository.getMyFaults({ page: 0, size: 50 });
            } else {
                res = await faultRepository.getAll({ page: 0, size: 50 });
            }
            // Spring returns paginated responses usually containing 'content' array
            setFaults(res.data.content || res.data);
        } catch (err) {
            console.error('Failed to fetch faults', err);
        } finally {
            setLoading(false);
        }
    };

    const getPriorityBadgeClass = (priority) => {
        switch (priority) {
            case 'CRITICAL': return 'badge-critical';
            case 'HIGH': return 'badge-high';
            case 'MEDIUM': return 'badge-medium';
            case 'LOW': return 'badge-low';
            default: return 'badge-default';
        }
    };

    const handleRowClick = (id) => {
        navigate(`/faults/${id}`);
    };

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <div className="page-header">
                    <h1 className="title">Fault Reports</h1>
                    <button className="primary-btn" onClick={() => navigate('/faults/new')}>
                        + Report Fault
                    </button>
                </div>

                <div className="glass-panel">
                    {loading ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>Loading faults...</p>
                    ) : faults.length === 0 ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>No faults found.</p>
                    ) : (
                        <div className="table-wrapper">
                            <table className="faults-table">
                                <thead>
                                    <tr>
                                        <th>Code</th>
                                        <th>Title</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th>Date Reported</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {faults.map(fault => (
                                        <tr key={fault.id} onClick={() => handleRowClick(fault.id)} className="clickable-row">
                                            <td><code>{fault.trackingCode}</code></td>
                                            <td>{fault.title}</td>
                                            <td>
                                                <span className={`badge ${getPriorityBadgeClass(fault.priorityLevel)}`}>
                                                    {fault.priorityLevel || 'UNASSIGNED'}
                                                </span>
                                            </td>
                                            <td>{fault.status}</td>
                                            <td>{new Date(fault.reportedAt).toLocaleDateString()}</td>
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

export default FaultsListPage;
