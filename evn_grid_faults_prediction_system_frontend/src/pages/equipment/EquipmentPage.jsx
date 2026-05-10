import { useState, useEffect } from 'react';
import equipmentRepository from '../../api/equipmentRepository';
import locationRepository from '../../api/locationRepository';

const EQUIPMENT_TYPES = [
    'TRANSFORMER', 'CABLE', 'SWITCH', 'FUSE',
    'METER', 'SUBSTATION', 'FEEDER', 'POLE', 'OTHER'
];

const EquipmentPage = () => {
    const [equipment, setEquipment] = useState([]);
    const [locations, setLocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editItem, setEditItem] = useState(null);
    const [formData, setFormData] = useState({
        assetCode: '',
        name: '',
        equipmentType: 'TRANSFORMER',
        status: 'ACTIVE',
        installationYear: '',
        lastMaintenanceDate: '',
        locationId: '',
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [eqRes, locRes] = await Promise.all([
                equipmentRepository.getAll(),
                locationRepository.getAll(),
            ]);
            setEquipment(eqRes.data);
            setLocations(locRes.data);
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
                await equipmentRepository.update(editItem.id, formData);
            } else {
                await equipmentRepository.create(formData);
            }
            setShowForm(false);
            setEditItem(null);
            resetForm();
            fetchData();
        } catch (err) {
            console.error('Failed to save equipment', err);
        }
    };

    const handleEdit = (item) => {
        setEditItem(item);
        setFormData({
            assetCode: item.assetCode,
            name: item.name,
            equipmentType: item.equipmentType,
            status: item.status || 'ACTIVE',
            installationYear: item.installationYear || '',
            lastMaintenanceDate: item.lastMaintenanceDate || '',
            locationId: item.locationId,
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this equipment?')) return;
        try {
            await equipmentRepository.delete(id);
            setEquipment(equipment.filter(e => e.id !== id));
        } catch (err) {
            console.error('Failed to delete', err);
        }
    };

    const resetForm = () => {
        setFormData({
            assetCode: '',
            name: '',
            equipmentType: 'TRANSFORMER',
            status: 'ACTIVE',
            installationYear: '',
            lastMaintenanceDate: '',
            locationId: '',
        });
    };

    if (loading) return (
        <div style={styles.loadingContainer}>
            <p style={{ color: '#94a3b8' }}>Loading equipment...</p>
        </div>
    );

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h1 style={styles.title}>Equipment</h1>
                <button style={styles.addBtn} onClick={() => {
                    setEditItem(null);
                    resetForm();
                    setShowForm(true);
                }}>
                    + Add Equipment
                </button>
            </div>

            {/* Form */}
            {showForm && (
                <div style={styles.formCard}>
                    <h2 style={styles.formTitle}>
                        {editItem ? 'Edit Equipment' : 'New Equipment'}
                    </h2>
                    <form onSubmit={handleSubmit}>
                        <div style={styles.formGrid}>
                            <div style={styles.field}>
                                <label style={styles.label}>Asset Code</label>
                                <input style={styles.input}
                                       value={formData.assetCode}
                                       onChange={e => setFormData({ ...formData, assetCode: e.target.value })}
                                       placeholder="e.g. TRANS-001"
                                       required />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Name</label>
                                <input style={styles.input}
                                       value={formData.name}
                                       onChange={e => setFormData({ ...formData, name: e.target.value })}
                                       required />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Type</label>
                                <select style={styles.input}
                                        value={formData.equipmentType}
                                        onChange={e => setFormData({ ...formData, equipmentType: e.target.value })}>
                                    {EQUIPMENT_TYPES.map(t => (
                                        <option key={t} value={t}>{t}</option>
                                    ))}
                                </select>
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Status</label>
                                <select style={styles.input}
                                        value={formData.status}
                                        onChange={e => setFormData({ ...formData, status: e.target.value })}>
                                    <option value="ACTIVE">Active</option>
                                    <option value="INACTIVE">Inactive</option>
                                    <option value="MAINTENANCE">Maintenance</option>
                                    <option value="FAULTY">Faulty</option>
                                </select>
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Installation Year</label>
                                <input style={styles.input} type="number"
                                       value={formData.installationYear}
                                       onChange={e => setFormData({ ...formData, installationYear: e.target.value })} />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Last Maintenance</label>
                                <input style={styles.input} type="date"
                                       value={formData.lastMaintenanceDate}
                                       onChange={e => setFormData({ ...formData, lastMaintenanceDate: e.target.value })} />
                            </div>
                            <div style={styles.field}>
                                <label style={styles.label}>Location</label>
                                <select style={styles.input}
                                        value={formData.locationId}
                                        onChange={e => setFormData({ ...formData, locationId: e.target.value })}
                                        required>
                                    <option value="">Select location...</option>
                                    {locations.map(l => (
                                        <option key={l.id} value={l.id}>{l.address}</option>
                                    ))}
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
                        <th style={styles.th}>Asset Code</th>
                        <th style={styles.th}>Name</th>
                        <th style={styles.th}>Type</th>
                        <th style={styles.th}>Status</th>
                        <th style={styles.th}>Age (years)</th>
                        <th style={styles.th}>Location</th>
                        <th style={styles.th}>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {equipment.map(e => (
                        <tr key={e.id} style={styles.tr}>
                            <td style={styles.td}>{e.assetCode}</td>
                            <td style={styles.td}>{e.name}</td>
                            <td style={styles.td}>{e.equipmentType}</td>
                            <td style={styles.td}>
                  <span style={{
                      ...styles.badge,
                      backgroundColor: e.status === 'ACTIVE' ? '#22c55e' :
                          e.status === 'FAULTY' ? '#ef4444' :
                              e.status === 'MAINTENANCE' ? '#f97316' : '#94a3b8',
                  }}>
                    {e.status}
                  </span>
                            </td>
                            <td style={styles.td}>{e.ageYears ?? '—'}</td>
                            <td style={styles.td}>{e.locationAddress}</td>
                            <td style={styles.td}>
                                <button style={styles.editBtn} onClick={() => handleEdit(e)}>
                                    Edit
                                </button>
                                <button style={styles.deleteBtn} onClick={() => handleDelete(e.id)}>
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

export default EquipmentPage;