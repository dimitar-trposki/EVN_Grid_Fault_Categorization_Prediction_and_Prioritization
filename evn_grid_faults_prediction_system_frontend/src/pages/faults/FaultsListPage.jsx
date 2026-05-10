import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/authStore';
import faultRepository from '../../api/faultRepository';
import Navbar from '../../components/Navbar';
import './Faults.css';

const FaultsListPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const initialSearch = queryParams.get('search') || '';

    const [faults, setFaults] = useState([]);
    const [loading, setLoading] = useState(true);
    
    // Filters state
    const [statusFilter, setStatusFilter] = useState('');
    const [priorityFilter, setPriorityFilter] = useState('');
    const [typeFilter, setTypeFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchFaults = async () => {
        setLoading(true);
        try {
            const params = {
                page,
                size: 20,
                status: statusFilter || undefined,
                faultPriority: priorityFilter || undefined,
                faultType: typeFilter || undefined,
                // In a real app, global search would be handled by a specific backend parameter or separate endpoint
                // For now we'll assume the backend handles it or we'll filter on the frontend if needed
            };

            let res;
            if (user?.role === 'CUSTOMER') {
                res = await faultRepository.getMyFaults(params);
            } else {
                res = await faultRepository.getAll(params);
            }
            
            setFaults(res.data.content || []);
            setTotalPages(res.data.totalPages || 1);
        } catch (err) {
            console.error('Failed to fetch faults', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFaults();
    }, [user?.role, statusFilter, priorityFilter, typeFilter, page]);

    // Handle search from Navbar
    useEffect(() => {
        if (initialSearch) {
            // If there's a search term, we might want to filter the current list
            // or perform a fresh fetch if the backend supports search param
            const filtered = faults.filter(f => 
                f.title.toLowerCase().includes(initialSearch.toLowerCase()) || 
                f.trackingCode.toLowerCase().includes(initialSearch.toLowerCase())
            );
            if (initialSearch && filtered.length > 0) {
                setFaults(filtered);
            }
        }
    }, [initialSearch]);

    const getPriorityBadgeClass = (priority) => {
        switch (priority) {
            case 'CRITICAL': return 'badge-critical';
            case 'HIGH': return 'badge-high';
            case 'MEDIUM': return 'badge-medium';
            case 'LOW': return 'badge-low';
            default: return 'badge-default';
        }
    };

    const handleExportCSV = () => {
        if (faults.length === 0) return;
        
        const headers = ['Code', 'Title', 'Type', 'Priority', 'Status', 'Date Reported'];
        const rows = faults.map(f => [
            f.trackingCode,
            f.title,
            f.faultType,
            f.faultPriority,
            f.currentStatus,
            new Date(f.reportedAt).toLocaleDateString()
        ]);

        const csvContent = [
            headers.join(','),
            ...rows.map(r => r.join(','))
        ].join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.setAttribute('href', url);
        link.setAttribute('download', `fault_report_${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const handleRowClick = (id) => {
        navigate(`/faults/${id}`);
    };

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <div className="page-header">
                    <div>
                        <h1 className="title">Fault Reports</h1>
                        <p style={{ color: '#94a3b8', margin: 0 }}>Manage and track grid issues</p>
                    </div>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button className="secondary-btn" onClick={handleExportCSV}>
                            Export CSV
                        </button>
                        <button className="primary-btn" onClick={() => navigate('/faults/new')}>
                            + Report Fault
                        </button>
                    </div>
                </div>

                {/* Filter Bar */}
                <div className="glass-panel" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
                        <div>
                            <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.75rem', marginBottom: '0.25rem' }}>Status</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem' }}
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                            >
                                <option value="">All Statuses</option>
                                <option value="REPORTED">REPORTED</option>
                                <option value="ASSIGNED">ASSIGNED</option>
                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                                <option value="RESOLVED">RESOLVED</option>
                                <option value="CLOSED">CLOSED</option>
                            </select>
                        </div>
                        <div>
                            <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.75rem', marginBottom: '0.25rem' }}>Priority</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem' }}
                                value={priorityFilter}
                                onChange={(e) => setPriorityFilter(e.target.value)}
                            >
                                <option value="">All Priorities</option>
                                <option value="CRITICAL">CRITICAL</option>
                                <option value="HIGH">HIGH</option>
                                <option value="MEDIUM">MEDIUM</option>
                                <option value="LOW">LOW</option>
                            </select>
                        </div>
                        <div>
                            <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.75rem', marginBottom: '0.25rem' }}>Fault Type</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem' }}
                                value={typeFilter}
                                onChange={(e) => setTypeFilter(e.target.value)}
                            >
                                <option value="">All Types</option>
                                <option value="LINE_FAILURE">LINE FAILURE</option>
                                <option value="TRANSFORMER_BLOWOUT">TRANSFORMER BLOWOUT</option>
                                <option value="SUBSTATION_ERROR">SUBSTATION ERROR</option>
                                <option value="STORM_DAMAGE">STORM DAMAGE</option>
                            </select>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                            <button 
                                className="secondary-btn" 
                                style={{ width: '100%', height: '38px' }}
                                onClick={() => {
                                    setStatusFilter('');
                                    setPriorityFilter('');
                                    setTypeFilter('');
                                    navigate('/faults');
                                }}
                            >
                                Reset Filters
                            </button>
                        </div>
                    </div>
                </div>

                <div className="glass-panel">
                    {loading ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>Loading faults...</p>
                    ) : faults.length === 0 ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>No faults found matching criteria.</p>
                    ) : (
                        <>
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
                                                    <span className={`badge ${getPriorityBadgeClass(fault.faultPriority)}`}>
                                                        {fault.faultPriority || 'UNASSIGNED'}
                                                    </span>
                                                </td>
                                                <td>{fault.currentStatus}</td>
                                                <td>{new Date(fault.reportedAt).toLocaleDateString()}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            
                            {/* Pagination */}
                            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '1.5rem', padding: '1rem' }}>
                                <button 
                                    className="secondary-btn" 
                                    disabled={page === 0}
                                    onClick={() => setPage(p => p - 1)}
                                >
                                    Previous
                                </button>
                                <span style={{ color: '#94a3b8', alignSelf: 'center' }}>
                                    Page {page + 1} of {totalPages}
                                </span>
                                <button 
                                    className="secondary-btn" 
                                    disabled={page >= totalPages - 1}
                                    onClick={() => setPage(p => p + 1)}
                                >
                                    Next
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default FaultsListPage;