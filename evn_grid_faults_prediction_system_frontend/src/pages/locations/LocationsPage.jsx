import { useState, useEffect } from 'react';
import locationRepository from '../../api/locationRepository';
import regionRepository from '../../api/regionRepository';

const LocationsPage = () => {
    const [locations, setLocations] = useState([]);
    const [regions, setRegions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editItem, setEditItem] = useState(null);
    const [formData, setFormData] = useState({
        address: '',
        latitude: '',
        longitude: '',
        municipality: '',
        cityArea: '',
        street: '',
        criticalityLevel: 'LOW',
        locationType: '',
        regionId: '',
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [locRes, regRes] = await Promise.all([
                locationRepository.getAll(),
                regionRepository.getAll(),
            ]);
            setLocations(locRes.data);
            setRegions(regRes.data);
        } catch (err) {
            console.error('Failed to load data', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editItem) {
                await locationRepository.update(editItem.id, formData);
            } else {
                await locationRepository.create(formData);
            }
            setShowForm(false);
            setEditItem(null);
            resetForm();
            fetchData();
        } catch (err) {
            console.error('Failed to save location', err);
        }
    };

    const handleEdit = (location) => {
        setEditItem(location);
        setFormData({
            address: location.address,
            latitude: location.latitude,
            longitude: location.longitude,
            municipality: location.municipality || '',
            cityArea: location.cityArea || '',
            street: location.street || '',
            criticalityLevel: location.criticalityLevel || 'LOW',
            locationType: location.locationType || '',
            regionId: location.regionId,
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this location?')) return;
        try {
            await locationRepository.delete(id);
            setLocations(locations.filter(l => l.id !== id));
        } catch (err) {
            console.error('Failed to delete', err);
        }
    };

    const resetForm = () => {
        setFormData({
            address: '',
            latitude: '',
            longitude: '',
            municipality: '',
            cityArea: '',
            street: '',
            criticalityLevel: 'LOW',
            locationType: '',
            regionId: '',
        });
    };

    if (loading) return (
        <div style={styles.loadingContainer}>
            <p style={{ color: '#94a3b8' }}>Loading locations...</p>
        </div>
    );

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h1 style={styles.title}>Locations</h1>
                <button style={styles.addBtn} onClick={() => {
                    setEditItem(null);
                    resetForm();
                    setShowForm(true);
                }}>
                    + Add Location
                </button>
            </div>

            {/* Form */}
            {showForm && (
                <div style={styles.formCard}>
                    <h2 style={styles.formTitle}>
                        {editItem ? 'Edit Location' : 'New Location'}
                    </h2>
                    <form onSubmit={handleSubmit}>
                        <div style={styles.formGrid}>
                            <div style={styles.field}>
                                <label style={styles.label}>Address</label>
                                <input style={styles.input} value={formData.address}
                                       onChange={e => setFormData({ ...formData, address: e.target.value })}
                                       required />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Region</label>
                                <select style={styles.input} value={formData.regionId}
                                        onChange={e => setFormData({ ...formData, regionId: e.target.value })}
                                        required>
                                    <option value="">Select region...</option>
                                    {regions.map(r => (
                                        <option key={r.id} value={r.id}>{r.name}</option>
                                    ))}
                                </select>
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Latitude</label>
                                <input style={styles.input} type="number" step="any"
                                       value={formData.latitude}
                                       onChange={e => setFormData({ ...formData, latitude: e.target.value })}
                                       required />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Longitude</label>
                                <input style={styles.input} type="number" step="any"
                                       value={formData.longitude}
                                       onChange={e => setFormData({ ...formData, longitude: e.target.value })}
                                       required />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Municipality</label>
                                <input style={styles.input} value={formData.municipality}
                                       onChange={e => setFormData({ ...formData, municipality: e.target.value })} />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Criticality Level</label>
                                <select style={styles.input} value={formData.criticalityLevel}
                                        onChange={e => setFormData({ ...formData, criticalityLevel: e.target.value })}>
                                    <option value="LOW">Low</option>
                                    <option value="MEDIUM">Medium</option>
                                    <option value="HIGH">High</option>
                                    <option value="CRITICAL">Critical</option>
                                </select>
                            </div>
                        </div>
                        <div style={styles.formActions}>
                            <button type="submit" style={styles.saveBtn}>
                                {editItem ? 'Update' : 'Create'}
                            </button>
                            <button type="button" style={styles.cancelBtn}
                                    onClick={() => setShowForm(false)}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Table */}
            <div style={styles.tableCard}>
                <table style={styles.table}>
                    <thead>
                    <tr>
                        <th style={styles.th}>Address</th>
                        <th style={styles.th}>Region</th>
                        <th style={styles.th}>Municipality</th>
                        <th style={styles.th}>Criticality</th>
                        <th style={styles.th}>Coordinates</th>
                        <th style={styles.th}>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {locations.map(l => (
                        <tr key={l.id} style={styles.tr}>
                            <td style={styles.td}>{l.address}</td>
                            <td style={styles.td}>{l.regionName}</td>
                            <td style={styles.td}>{l.municipality || '—'}</td>
                            <td style={styles.td}>
                  <span style={{
                      ...styles.badge,
                      backgroundColor: l.criticalityLevel === 'CRITICAL' ? '#ef4444' :
                          l.criticalityLevel === 'HIGH' ? '#f97316' :
                              l.criticalityLevel === 'MEDIUM' ? '#eab308' : '#22c55e',
                  }}>
                    {l.criticalityLevel}
                  </span>
                            </td>
                            <td style={styles.td}>
                                {l.latitude?.toFixed(4)}, {l.longitude?.toFixed(4)}
                            </td>
                            <td style={styles.td}>
                                <button style={styles.editBtn} onClick={() => handleEdit(l)}>
                                    Edit
                                </button>
                                <button style={styles.deleteBtn} onClick={() => handleDelete(l.id)}>
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

const styles = {
    container: { padding: '2rem', backgroundColor: '#0f172a', minHeight: '100vh' },
    loadingContainer: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#0f172a' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
    title: { color: '#f1f5f9', margin: 0 },
    addBtn: { padding: '0.5rem 1rem', borderRadius: '8px', border: 'none', backgroundColor: '#3b82f6', color: 'white', cursor: 'pointer' },
    formCard: { backgroundColor: '#1e293b', padding: '1.5rem', borderRadius: '12px', border: '1px solid #334155', marginBottom: '1.5rem' },
    formTitle: { color: '#f1f5f9', marginTop: 0 },
    formGrid: { display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' },
    field: { display: 'flex', flexDirection: 'column' },
    label: { color: '#94a3b8', marginBottom: '0.4rem', fontSize: '0.9rem' },
    input: { padding: '0.65rem', borderRadius: '8px', border: '1px solid #334155', backgroundColor: '#0f172a', color: '#f1f5f9', fontSize: '1rem' },
    formActions: { display: 'flex', gap: '1rem', marginTop: '1rem' },
    saveBtn: { padding: '0.6rem 1.5rem', borderRadius: '8px', border: 'none', backgroundColor: '#3b82f6', color: 'white', cursor: 'pointer' },
    cancelBtn: { padding: '0.6rem 1.5rem', borderRadius: '8px', border: 'none', backgroundColor: '#475569', color: 'white', cursor: 'pointer' },
    tableCard: { backgroundColor: '#1e293b', padding: '1.5rem', borderRadius: '12px', border: '1px solid #334155' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { color: '#94a3b8', textAlign: 'left', padding: '0.75rem', borderBottom: '1px solid #334155', fontSize: '0.9rem' },
    tr: { borderBottom: '1px solid #334155' },
    td: { color: '#f1f5f9', padding: '0.75rem' },
    badge: { padding: '0.25rem 0.75rem', borderRadius: '999px', color: 'white', fontSize: '0.8rem', fontWeight: 'bold' },
    editBtn: { padding: '0.3rem 0.75rem', borderRadius: '6px', border: 'none', backgroundColor: '#3b82f6', color: 'white', cursor: 'pointer', marginRight: '0.5rem', fontSize: '0.85rem' },
    deleteBtn: { padding: '0.3rem 0.75rem', borderRadius: '6px', border: 'none', backgroundColor: '#ef4444', color: 'white', cursor: 'pointer', fontSize: '0.85rem' },
};

export default LocationsPage;