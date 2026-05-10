import { useState, useEffect } from 'react';
import equipmentRepository from '../../api/equipmentRepository';
import Navbar from '../../components/Navbar';

const EquipmentRegistryPage = () => {
    const [equipment, setEquipment] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const fetchEquipment = async () => {
            try {
                const res = await equipmentRepository.getAll();
                setEquipment(res.data);
            } catch (err) {
                console.error('Failed to load equipment', err);
            } finally {
                setLoading(false);
            }
        };
        fetchEquipment();
    }, []);

    const filteredEquipment = equipment.filter(item => 
        item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.equipmentType.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.locationAddress?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (loading) return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-loading">
                <p>Loading equipment registry...</p>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-content">
                <h1 className="section-title">Equipment Registry</h1>
                
                <div className="dashboard-section" style={{ marginBottom: '2rem' }}>
                    <input 
                        type="text" 
                        className="search-input" 
                        placeholder="Search by name, type or location..." 
                        style={{ width: '100%', padding: '0.8rem', backgroundColor: '#0f172a' }}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                <div className="dashboard-section">
                    <div style={{ overflowX: 'auto' }}>
                        <table className="dashboard-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Type</th>
                                    <th>Location</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredEquipment.map((item) => (
                                    <tr key={item.id}>
                                        <td>#{item.id}</td>
                                        <td style={{ fontWeight: 600, color: '#f1f5f9' }}>{item.name}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: '#475569' }}>
                                                {item.equipmentType.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td>{item.locationAddress || `Location #${item.locationId}`}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: '#22c55e' }}>
                                                OPERATIONAL
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {filteredEquipment.length === 0 && (
                            <p style={{ textAlign: 'center', padding: '2rem', color: '#94a3b8' }}>No equipment found matching your search.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EquipmentRegistryPage;
