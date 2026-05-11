import { useState, useEffect } from 'react';
import equipmentRepository from '../api/equipmentRepository';
import locationRepository from '../api/locationRepository';
import './EquipmentModal.css';

const EQUIPMENT_TYPES = [
    'TRANSFORMER',
    'CABLE',
    'OVERHEAD_LINE',
    'SWITCHGEAR',
    'METER',
    'SUBSTATION',
    'CIRCUIT_BREAKER',
    'FUSE',
    'CAPACITOR_BANK',
    'DISTRIBUTION_BOX'
];

const EquipmentModal = ({ equipmentItem, onClose, onSuccess }) => {
    const isEdit = !!equipmentItem;
    
    const [locations, setLocations] = useState([]);
    const [loadingLocations, setLoadingLocations] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        equipmentType: 'TRANSFORMER',
        locationId: ''
    });

    useEffect(() => {
        if (isEdit) {
            setFormData({
                name: equipmentItem.name || '',
                equipmentType: equipmentItem.equipmentType || 'TRANSFORMER',
                locationId: equipmentItem.locationId || ''
            });
        }
    }, [equipmentItem, isEdit]);

    useEffect(() => {
        const fetchLocations = async () => {
            try {
                const res = await locationRepository.getAll();
                const data = res.data.content || res.data;
                setLocations(data);
                
                // Automatically select first location if none selected during CREATE
                if (!isEdit && data.length > 0 && !formData.locationId) {
                    setFormData(prev => ({ ...prev, locationId: data[0].id }));
                }
            } catch (err) {
                console.error('Failed to fetch locations', err);
            } finally {
                setLoadingLocations(false);
            }
        };
        fetchLocations();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        const payload = {
            ...formData,
            locationId: Number(formData.locationId)
        };

        try {
            if (isEdit) {
                await equipmentRepository.update(equipmentItem.id, payload);
            } else {
                await equipmentRepository.create(payload);
            }
            onSuccess();
            onClose();
        } catch (err) {
            console.error('Failed to save equipment', err);
            setError(err.response?.data?.message || 'Failed to save equipment details. Ensure all inputs are valid.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ maxWidth: '500px' }}>
                <div className="modal-header">
                    <h2>{isEdit ? 'Edit Asset Details' : 'Register New Asset'}</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    {error && <div className="modal-error-banner">{error}</div>}

                    <div className="modal-form-group">
                        <label className="modal-label">Equipment / Asset Name</label>
                        <input
                            type="text"
                            name="name"
                            className="modal-input"
                            value={formData.name}
                            onChange={handleChange}
                            required
                            maxLength={80}
                            placeholder="e.g. Delta-12 Transformer"
                        />
                    </div>

                    <div className="modal-form-group">
                        <label className="modal-label">Asset Category</label>
                        <select
                            name="equipmentType"
                            className="modal-input"
                            value={formData.equipmentType}
                            onChange={handleChange}
                            required
                        >
                            {EQUIPMENT_TYPES.map(type => (
                                <option key={type} value={type}>{type.replace('_', ' ')}</option>
                            ))}
                        </select>
                    </div>

                    <div className="modal-form-group">
                        <label className="modal-label">Grid Location Binding</label>
                        <select
                            name="locationId"
                            className="modal-input"
                            value={formData.locationId}
                            onChange={handleChange}
                            disabled={loadingLocations}
                            required
                        >
                            {loadingLocations ? (
                                <option>Loading grid locations...</option>
                            ) : locations.length === 0 ? (
                                <option value="">No available locations found</option>
                            ) : (
                                locations.map(loc => (
                                    <option key={loc.id} value={loc.id}>
                                        {loc.address || `Node #${loc.id}`}
                                    </option>
                                ))
                            )}
                        </select>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="secondary-btn" onClick={onClose}>Cancel</button>
                        <button type="submit" className="primary-btn" disabled={submitting}>
                            {submitting ? 'Saving...' : isEdit ? 'Update Hardware' : 'Confirm Onboarding'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EquipmentModal;
