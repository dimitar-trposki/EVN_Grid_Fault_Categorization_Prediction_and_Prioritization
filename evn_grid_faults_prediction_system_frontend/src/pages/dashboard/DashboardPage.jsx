import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    ResponsiveContainer, 
    PieChart, Pie, Cell, 
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
    LineChart, Line
} from 'recharts';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import dashboardRepository from '../../api/dashboardRepository';
import Navbar from '../../components/Navbar';
import './Dashboard.css';

// Fix for Leaflet default icon issues in React
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2x,
    iconUrl: markerIcon,
    shadowUrl: markerShadow,
});

const CHART_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

const DashboardPage = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [activeFaults, setActiveFaults] = useState([]);
    const [regionData, setRegionData] = useState([]);
    const [typeData, setTypeData] = useState([]);
    const [trendData, setTrendData] = useState([]);
    const [crewData, setCrewData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [
                    statsRes, 
                    faultsRes, 
                    regionRes, 
                    typeRes, 
                    trendRes, 
                    crewRes
                ] = await Promise.all([
                    dashboardRepository.getKpis(),
                    dashboardRepository.getActiveFaults(),
                    dashboardRepository.getFaultsByRegion(),
                    dashboardRepository.getFaultsByType(),
                    dashboardRepository.getFaultsByPeriod('day'),
                    dashboardRepository.getCrewPerformance(),
                ]);

                setStats(statsRes.data);
                setActiveFaults(faultsRes.data);
                
                setRegionData(regionRes.data.map(item => ({
                    name: item.regionName,
                    value: item.count
                })));

                setTypeData(typeRes.data.map(item => ({
                    name: item.typeName,
                    count: item.count
                })));

                setTrendData(trendRes.data.map(item => ({
                    date: item.period,
                    count: item.count
                })));

                setCrewData(crewRes.data);
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
            <p>Loading analytics dashboard...</p>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />

            <div className="dashboard-content">
                <h1 className="section-title">Grid Operations Overview</h1>

                {/* KPI Stats Cards */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <p className="stat-label">Active Faults</p>
                        <h2 className="stat-value">{stats?.totalActiveFaults ?? '0'}</h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Critical</p>
                        <h2 className="stat-value" style={{ color: '#ef4444' }}>
                            {stats?.criticalFaults ?? '0'}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Faults Today</p>
                        <h2 className="stat-value" style={{ color: '#3b82f6' }}>
                            {stats?.faultsToday ?? '0'}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Crews Active</p>
                        <h2 className="stat-value" style={{ color: '#10b981' }}>
                            {stats?.crewsActive ?? '0'} / {stats?.crewsTotal ?? '—'}
                        </h2>
                    </div>
                    <div className="stat-card">
                        <p className="stat-label">Avg Resolution</p>
                        <h2 className="stat-value">
                            {stats?.avgResolutionTimeMin ? Math.round(stats.avgResolutionTimeMin) : '—'} min
                        </h2>
                    </div>
                </div>

                {/* Interactive Map */}
                <div className="dashboard-section">
                    <h2 className="chart-title">Active Faults Map</h2>
                    <div className="map-container">
                        <MapContainer 
                            center={[41.9981, 21.4254]} // Default center (e.g., Skopje)
                            zoom={12} 
                            style={{ height: '100%', width: '100%' }}
                        >
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                            />
                            {activeFaults.map(fault => (
                                <Marker 
                                    key={fault.id} 
                                    position={[fault.latitude, fault.longitude]}
                                    eventHandlers={{
                                        click: () => navigate(`/faults/${fault.id}`)
                                    }}
                                >
                                    <Popup>
                                        <strong>{fault.faultTypeName}</strong><br />
                                        Priority: {fault.priorityLevel}<br />
                                        Status: {fault.status}<br />
                                        <button onClick={() => navigate(`/faults/${fault.id}`)} style={{ marginTop: '5px', cursor: 'pointer' }}>
                                            View Details
                                        </button>
                                    </Popup>
                                </Marker>
                            ))}
                        </MapContainer>
                    </div>
                </div>

                {/* Charts Grid */}
                <div className="charts-grid">
                    <div className="chart-card">
                        <h3 className="chart-title">Faults by Region</h3>
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={regionData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                    label
                                >
                                    {regionData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>

                    <div className="chart-card">
                        <h3 className="chart-title">Faults by Type</h3>
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={typeData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                                <XAxis dataKey="name" stroke="#94a3b8" />
                                <YAxis stroke="#94a3b8" />
                                <Tooltip 
                                    contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155' }}
                                    itemStyle={{ color: '#f1f5f9' }}
                                />
                                <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    <div className="chart-card" style={{ gridColumn: 'span 2' }}>
                        <h3 className="chart-title">Fault Trends (Daily)</h3>
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={trendData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                                <XAxis dataKey="date" stroke="#94a3b8" />
                                <YAxis stroke="#94a3b8" />
                                <Tooltip 
                                    contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155' }}
                                    itemStyle={{ color: '#f1f5f9' }}
                                />
                                <Line type="monotone" dataKey="count" stroke="#10b981" strokeWidth={3} dot={{ r: 6 }} activeDot={{ r: 8 }} />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Crew Performance Table */}
                <div className="dashboard-section">
                    <h2 className="section-title">Crew Performance</h2>
                    <div style={{ overflowX: 'auto' }}>
                        <table className="dashboard-table">
                            <thead>
                                <tr>
                                    <th>Crew Name</th>
                                    <th>Region</th>
                                    <th>Status</th>
                                    <th>Tasks Completed</th>
                                    <th>Avg Resolution</th>
                                </tr>
                            </thead>
                            <tbody>
                                {crewData.map((crew) => (
                                    <tr key={crew.crewId}>
                                        <td>{crew.crewName}</td>
                                        <td>{crew.regionName}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: crew.status === 'IDLE' ? '#22c55e' : '#3b82f6' }}>
                                                {crew.status}
                                            </span>
                                        </td>
                                        <td>{crew.tasksCompleted}</td>
                                        <td>{Math.round(crew.avgResolutionTimeMin)} min</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Active Faults List */}
                <div className="dashboard-section">
                    <h2 className="section-title">Recent Fault Reports</h2>
                    <div style={{ overflowX: 'auto' }}>
                        <table className="dashboard-table">
                            <thead>
                                <tr>
                                    <th>Code</th>
                                    <th>Type</th>
                                    <th>Priority</th>
                                    <th>Status</th>
                                    <th>Reported At</th>
                                </tr>
                            </thead>
                            <tbody>
                                {activeFaults.map((fault) => (
                                    <tr key={fault.id} className="clickable-row" onClick={() => navigate(`/faults/${fault.id}`)}>
                                        <td>#{fault.id}</td>
                                        <td>{fault.faultTypeName}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: getPriorityColor(fault.priorityLevel) }}>
                                                {fault.priorityLevel}
                                            </span>
                                        </td>
                                        <td>{fault.status}</td>
                                        <td>{new Date(fault.reportedAt).toLocaleString()}</td>
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

export default DashboardPage;