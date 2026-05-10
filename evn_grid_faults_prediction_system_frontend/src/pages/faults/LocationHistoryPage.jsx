import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet';
import faultRepository from '../../api/faultRepository';
import Navbar from '../../components/Navbar';
import './History.css';

const LocationHistoryPage = () => {
    const [historicalFaults, setHistoricalFaults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                // Fetch a large number of faults to show history
                const res = await faultRepository.getAll({ size: 1000, status: 'CLOSED' });
                setHistoricalFaults(res.data.content || []);
            } catch (err) {
                console.error('Failed to load historical faults', err);
            } finally {
                setLoading(false);
            }
        };
        fetchHistory();
    }, []);

    if (loading) return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-loading">
                <p>Loading location history map...</p>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-content">
                <h1 className="section-title">Grid Fault History & Hotspots</h1>
                
                <div className="dashboard-section">
                    <div className="map-container" style={{ height: '70vh' }}>
                        <MapContainer 
                            center={[41.9981, 21.4254]} 
                            zoom={11} 
                            style={{ height: '100%', width: '100%' }}
                        >
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                            />
                            {historicalFaults.map(fault => (
                                <CircleMarker 
                                    key={fault.id} 
                                    center={[fault.latitude, fault.longitude]}
                                    radius={8}
                                    pathOptions={{ 
                                        fillColor: '#ef4444', 
                                        color: '#ef4444', 
                                        fillOpacity: 0.4,
                                        weight: 1
                                    }}
                                >
                                    <Popup>
                                        <strong>{fault.title}</strong><br />
                                        Type: {fault.faultType}<br />
                                        Status: {fault.status}<br />
                                        Reported: {new Date(fault.reportedAt).toLocaleDateString()}
                                    </Popup>
                                </CircleMarker>
                            ))}
                        </MapContainer>
                    </div>
                    <div style={{ marginTop: '1.5rem', color: '#94a3b8', fontSize: '0.9rem' }}>
                        <p>Showing {historicalFaults.length} historical faults. Red circles indicate locations where faults have occurred and been resolved.</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LocationHistoryPage;
