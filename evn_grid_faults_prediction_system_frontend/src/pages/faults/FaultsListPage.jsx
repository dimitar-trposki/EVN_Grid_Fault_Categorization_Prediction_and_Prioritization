import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/authStore';
import faultRepository from '../../api/faultRepository';
import locationRepository from '../../api/locationRepository';
import regionRepository from '../../api/regionRepository';
import Navbar from '../../components/Navbar';
import './Faults.css';

const FAULT_TYPES = [
    'SHORT_CIRCUIT', 'OVERLOAD', 'EQUIPMENT_FAILURE', 'POWER_OUTAGE', 
    'VOLTAGE_DROP', 'INSULATION_FAILURE', 'GROUND_FAULT', 'OTHER'
];

const FaultsListPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const initialSearch = queryParams.get('search') || '';

    const [faults, setFaults] = useState([]);
    const [loading, setLoading] = useState(true);
    
    // Data catalogs for cascading select filters
    const [regions, setRegions] = useState([]);
    const [locations, setLocations] = useState([]);

    // Filters state
    const [statusFilter, setStatusFilter] = useState('');
    const [priorityFilter, setPriorityFilter] = useState('');
    const [typeFilter, setTypeFilter] = useState('');
    const [regionFilter, setRegionFilter] = useState('');
    const [locationFilter, setLocationFilter] = useState('');
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // Initial Catalog Fetch
    useEffect(() => {
        const fetchBaseData = async () => {
            try {
                const res = await regionRepository.getAll();
                setRegions(res.data || []);
            } catch (e) { console.error(e); }
        };
        fetchBaseData();
    }, []);

    // Cascading Location Update
    useEffect(() => {
        const updateLocs = async () => {
            if (!regionFilter) {
                setLocations([]);
                setLocationFilter('');
                return;
            }
            try {
                const res = await locationRepository.getByRegion(regionFilter);
                setLocations(res.data || []);
                setLocationFilter(''); // Reset child select
            } catch (e) { console.error(e); }
        };
        updateLocs();
    }, [regionFilter]);

    const fetchFaults = async () => {
        setLoading(true);
        try {
            // Format dates for ISO Backend compatibility
            const isoFrom = fromDate ? new Date(fromDate).toISOString() : undefined;
            const isoTo = toDate ? new Date(toDate).toISOString() : undefined;

            const params = {
                page,
                size: 20,
                status: statusFilter || undefined,
                faultPriority: priorityFilter || undefined,
                faultType: typeFilter || undefined,
                regionId: regionFilter || undefined,
                locationId: locationFilter || undefined,
                from: isoFrom,
                to: isoTo
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
    }, [user?.role, statusFilter, priorityFilter, typeFilter, regionFilter, locationFilter, fromDate, toDate, page]);

    // Handle global tracking search from Navbar
    useEffect(() => {
        const performTrackingRedirect = async () => {
            if (!initialSearch) return;
            try {
                const res = await faultRepository.trackByCode(initialSearch);
                if (res.data && res.data.id) {
                    navigate(`/faults/${res.data.id}`);
                }
            } catch (e) {
                console.log("Search wasn't a direct tracking code, continuing list load.");
            }
        };
        performTrackingRedirect();
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
        const headers = ['Tracking ID', 'Issue Title', 'Categorization', 'System Priority', 'Operational Status', 'Log Date'];
        const rows = faults.map(f => [
            f.trackingCode,
            `"${f.title.replace(/"/g, '""')}"`,
            f.faultType,
            f.faultPriority,
            f.currentStatus,
            new Date(f.reportedAt).toLocaleString()
        ]);

        const csvString = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `EVN_FaultRegistry_Export_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    };

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <div className="page-header">
                    <div>
                        <h1 className="title">Global Fault Ledger</h1>
                        <p style={{ color: '#94a3b8', margin: 0 }}>Master registry of grid perturbations and critical outages.</p>
                    </div>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button className="secondary-btn" onClick={handleExportCSV} disabled={faults.length === 0}>
                            💾 Dump CSV
                        </button>
                        <button className="primary-btn" onClick={() => navigate('/faults/new')}>
                            ⚡ Log Issue
                        </button>
                    </div>
                </div>

                {/* Advanced Heavy Duty Filter Frame */}
                <div className="glass-panel" style={{ marginBottom: '1.5rem', padding: '1.25rem' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem' }}>
                        <div>
                            <label className="filter-label">Region Group</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem', backgroundColor: '#0f172a' }}
                                value={regionFilter}
                                onChange={(e) => setRegionFilter(e.target.value)}
                            >
                                <option value="">Global All</option>
                                {regions.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                            </select>
                        </div>
                        
                        <div>
                            <label className="filter-label">Local Node</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem', backgroundColor: '#0f172a' }}
                                value={locationFilter}
                                onChange={(e) => setLocationFilter(e.target.value)}
                                disabled={!regionFilter}
                            >
                                <option value="">Select Node...</option>
                                {locations.map(l => <option key={l.id} value={l.id}>{l.address}</option>)}
                            </select>
                        </div>

                        <div>
                            <label className="filter-label">Status</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem', backgroundColor: '#0f172a' }}
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                            >
                                <option value="">Any State</option>
                                <option value="REPORTED">REPORTED</option>
                                <option value="ASSIGNED">ASSIGNED</option>
                                <option value="IN_PROGRESS">IN PROGRESS</option>
                                <option value="RESOLVED">RESOLVED</option>
                                <option value="CLOSED">CLOSED</option>
                            </select>
                        </div>

                        <div>
                            <label className="filter-label">Log Type</label>
                            <select 
                                className="search-input" 
                                style={{ width: '100%', padding: '0.5rem', backgroundColor: '#0f172a' }}
                                value={typeFilter}
                                onChange={(e) => setTypeFilter(e.target.value)}
                            >
                                <option value="">Any Type</option>
                                {FAULT_TYPES.map(t => (
                                    <option key={t} value={t}>{t.replace('_', ' ')}</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <div style={{ flex: 1 }}>
                                <label className="filter-label">From</label>
                                <input type="date" className="search-input" style={{ width: '100%', padding: '0.5rem', fontSize: '0.8rem' }}
                                    value={fromDate} onChange={e => setFromDate(e.target.value)} />
                            </div>
                            <div style={{ flex: 1 }}>
                                <label className="filter-label">To</label>
                                <input type="date" className="search-input" style={{ width: '100%', padding: '0.5rem', fontSize: '0.8rem' }}
                                    value={toDate} onChange={e => setToDate(e.target.value)} />
                            </div>
                        </div>

                        <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                            <button 
                                className="secondary-btn" 
                                style={{ width: '100%', height: '38px' }}
                                onClick={() => {
                                    setStatusFilter('');
                                    setPriorityFilter('');
                                    setTypeFilter('');
                                    setRegionFilter('');
                                    setFromDate('');
                                    setToDate('');
                                    setPage(0);
                                    navigate('/faults');
                                }}
                            >
                                Reset Query
                            </button>
                        </div>
                    </div>
                </div>

                <style>{`
                    .filter-label { display: block; color: #94a3b8; font-size: 0.7rem; text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em; margin-bottom: 0.25rem; }
                `}</style>

                <div className="glass-panel">
                    {loading ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>Scanning server ledgers...</p>
                    ) : faults.length === 0 ? (
                        <p style={{textAlign: 'center', color: '#94a3b8', padding: '2rem'}}>No matching discrepancies found in grid data buffers.</p>
                    ) : (
                        <>
                            <div className="table-wrapper">
                                <table className="faults-table">
                                    <thead>
                                        <tr>
                                            <th>Tracking Key</th>
                                            <th>Operational Title</th>
                                            <th>Severity</th>
                                            <th>Resolution Phase</th>
                                            <th>Timestamp</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {faults.map(fault => (
                                            <tr key={fault.id} onClick={() => navigate(`/faults/${fault.id}`)} className="clickable-row">
                                                <td><code>{fault.trackingCode}</code></td>
                                                <td style={{ fontWeight: 500 }}>{fault.title}</td>
                                                <td>
                                                    <span className={`badge ${getPriorityBadgeClass(fault.faultPriority)}`}>
                                                        {fault.faultPriority || 'NULL'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <span className="dashboard-badge" style={{ fontSize: '0.7rem', backgroundColor: '#1e293b' }}>
                                                        {fault.currentStatus}
                                                    </span>
                                                </td>
                                                <td style={{ fontSize: '0.8rem', color: '#94a3b8' }}>{new Date(fault.reportedAt).toLocaleDateString()}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            
                            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '1.5rem', padding: '1rem', borderTop: '1px solid #334155' }}>
                                <button 
                                    className="secondary-btn" 
                                    disabled={page === 0}
                                    onClick={() => { setPage(p => p - 1); window.scrollTo(0,0); }}
                                >
                                    « Back
                                </button>
                                <span style={{ color: '#f1f5f9', alignSelf: 'center', fontSize: '0.9rem', fontWeight: 600 }}>
                                    Index {page + 1} / {totalPages}
                                </span>
                                <button 
                                    className="secondary-btn" 
                                    disabled={page >= totalPages - 1}
                                    onClick={() => { setPage(p => p + 1); window.scrollTo(0,0); }}
                                >
                                    Next »
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
