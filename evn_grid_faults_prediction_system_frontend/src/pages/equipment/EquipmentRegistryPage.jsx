import { useState, useEffect } from 'react';
import equipmentRepository from '../../api/equipmentRepository';
import locationRepository from '../../api/locationRepository';
import Navbar from '../../components/Navbar';
import { useAuth } from '../../context/authStore';
import EquipmentModal from '../../components/EquipmentModal';

const EquipmentRegistryPage = () => {
    const { user } = useAuth();
    const [equipment, setEquipment] = useState([]);
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [locationFilter, setLocationFilter] = useState('');
    
    // Modal states
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);

    const fetchEquipment = async () => {
        setLoading(true);
        try {
            let res;
            if (locationFilter) {
                res = await equipmentRepository.getByLocation(locationFilter);
            } else {
                res = await equipmentRepository.getAll();
            }
            setEquipment(res.data);
        } catch (err) {
            console.error('Failed to load equipment', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const fetchAuxiliaryData = async () => {
            try {
                const locRes = await locationRepository.getAll();
                setLocations(locRes.data.content || locRes.data || []);
            } catch (e) {
                console.error("Failed auxiliary load", e);
            }
        };
        fetchAuxiliaryData();
    }, []);

    useEffect(() => {
        fetchEquipment();
    }, [locationFilter]);

    const handleOpenCreate = () => {
        setSelectedItem(null);
        setIsModalOpen(true);
    };

    const handleOpenEdit = (item) => {
        setSelectedItem(item);
        setIsModalOpen(true);
    };

    const handleDelete = async (id, name) => {
        if (window.confirm(`Are you certain you wish to permanently decommission and remove "${name}" from inventory?`)) {
            try {
                await equipmentRepository.delete(id);
                await fetchEquipment(); // Refresh list
            } catch (err) {
                console.error("Deletion failed", err);
                alert("Failed to delete equipment record. System constraint may apply.");
            }
        }
    };

    const filteredEquipment = equipment.filter(item => 
        item.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.equipmentType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.locationAddress?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Check permissions for write operations
    const canManage = user?.role === 'ADMIN' || user?.role === 'OPERATOR' || user?.role === 'MANAGER';

    if (loading && equipment.length === 0) return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-loading">
                <p>Synchronizing equipment registry...</p>
            </div>
        </div>
    );

    return (
        <div className="dashboard-container">
            <Navbar />
            <div className="dashboard-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <h1 className="section-title" style={{ margin: 0 }}>Equipment Registry</h1>
                    {canManage && (
                        <button className="primary-btn" onClick={handleOpenCreate}>
                            + Onboard Hardware
                        </button>
                    )}
                </div>
                
                <div className="dashboard-section" style={{ marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                    <div style={{ flex: 2 }}>
                        <input 
                            type="text" 
                            className="search-input" 
                            placeholder="Text filter by name, serial, or category..." 
                            style={{ width: '100%', padding: '0.8rem', backgroundColor: '#0f172a' }}
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <div style={{ flex: 1 }}>
                        <select 
                            className="search-input" 
                            style={{ width: '100%', padding: '0.8rem', backgroundColor: '#0f172a', color: locationFilter ? '#fff' : '#94a3b8' }}
                            value={locationFilter}
                            onChange={(e) => setLocationFilter(e.target.value)}
                        >
                            <option value="">All Locations / Nodes</option>
                            {locations.map(loc => (
                                <option key={loc.id} value={loc.id} style={{ color: '#fff' }}>
                                    {loc.address || `Grid Node #${loc.id}`}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="dashboard-section">
                    <div style={{ overflowX: 'auto' }}>
                        <table className="dashboard-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Asset Name</th>
                                    <th>Category</th>
                                    <th>Location Node</th>
                                    <th>Status</th>
                                    {canManage && <th>Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {filteredEquipment.map((item) => (
                                    <tr key={item.id}>
                                        <td><code>#{item.id}</code></td>
                                        <td style={{ fontWeight: 600, color: '#f1f5f9' }}>{item.name}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: '#334155', color: '#cbd5e1' }}>
                                                {item.equipmentType?.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td>{item.locationAddress || `Grid Ref: ${item.locationId}`}</td>
                                        <td>
                                            <span className="dashboard-badge" style={{ backgroundColor: '#065f46', color: '#34d399' }}>
                                                ONLINE
                                            </span>
                                        </td>
                                        {canManage && (
                                            <td>
                                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                    <button 
                                                        className="secondary-btn" 
                                                        style={{ padding: '0.3rem 0.75rem', fontSize: '0.8rem' }}
                                                        onClick={() => handleOpenEdit(item)}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button 
                                                        className="secondary-btn" 
                                                        style={{ 
                                                            padding: '0.3rem 0.75rem', 
                                                            fontSize: '0.8rem', 
                                                            borderColor: 'rgba(239,68,68,0.4)', 
                                                            color: '#fca5a5' 
                                                        }}
                                                        onClick={() => handleDelete(item.id, item.name)}
                                                    >
                                                        Delete
                                                    </button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {filteredEquipment.length === 0 && !loading && (
                            <p style={{ textAlign: 'center', padding: '2rem', color: '#94a3b8' }}>No assets found matching your query.</p>
                        )}
                    </div>
                </div>
            </div>

            {isModalOpen && (
                <EquipmentModal 
                    equipmentItem={selectedItem}
                    onClose={() => setIsModalOpen(false)}
                    onSuccess={fetchEquipment}
                />
            )}
        </div>
    );
};

export default EquipmentRegistryPage;


