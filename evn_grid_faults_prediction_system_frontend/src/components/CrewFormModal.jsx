import { useState, useEffect } from 'react';
import crewRepository from '../api/crewRepository';

const CREW_STATUSES = ['IDLE', 'ON_MISSION', 'MAINTENANCE', 'OFF_DUTY'];

const CrewFormModal = ({ crewItem, onClose, onSuccess }) => {
    const isEdit = !!crewItem;
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        crewCode: '',
        status: 'IDLE',
        currentLatitude: 41.9981,
        currentLongitude: 21.4254
    });

    useEffect(() => {
        if (isEdit) {
            setFormData({
                name: crewItem.name || '',
                crewCode: crewItem.crewCode || '',
                status: crewItem.status || 'IDLE',
                currentLatitude: crewItem.latitude || 41.9981,
                currentLongitude: crewItem.longitude || 21.4254
            });
        }
    }, [crewItem, isEdit]);

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
            currentLatitude: Number(formData.currentLatitude),
            currentLongitude: Number(formData.currentLongitude)
        };

        try {
            if (isEdit) {
                // Backend update uses separate UpdateCrewRequest structure
                await crewRepository.update(crewItem.id, payload);
            } else {
                await crewRepository.create(payload);
            }
            onSuccess();
            onClose();
        } catch (err) {
            console.error('Crew Save failed', err);
            setError(err.response?.data?.message || 'Failed to commit crew record. Confirm input validations.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ maxWidth: '500px' }}>
                <div className="modal-header">
                    <h2>{isEdit ? `Edit Crew: ${crewItem.name}` : 'Register New Emergency Crew'}</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-body">
                    {error && <div className="modal-error-banner">{error}</div>}

                    <div className="modal-form-group">
                        <label className="modal-label">Display Name / Identifier</label>
                        <input
                            type="text"
                            name="name"
                            className="modal-input"
                            value={formData.name}
                            onChange={handleChange}
                            required
                            maxLength={80}
                            placeholder="e.g. Alpha-Team Quick Response"
                        />
                    </div>

                    {!isEdit && (
                        <div className="modal-form-group">
                            <label className="modal-label">Unique Identifier Code</label>
                            <input
                                type="text"
                                name="crewCode"
                                className="modal-input"
                                value={formData.crewCode}
                                onChange={handleChange}
                                required
                                maxLength={30}
                                placeholder="e.g. ATC-742"
                            />
                        </div>
                    )}

                    <div className="modal-form-group">
                        <label className="modal-label">Initial Status Duty</label>
                        <select
                            name="status"
                            className="modal-input"
                            value={formData.status}
                            onChange={handleChange}
                            required
                        >
                            {CREW_STATUSES.map(s => (
                                <option key={s} value={s}>{s.replace('_', ' ')}</option>
                            ))}
                        </select>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div className="modal-form-group">
                            <label className="modal-label">Starting Lat.</label>
                            <input
                                type="number"
                                step="any"
                                name="currentLatitude"
                                className="modal-input"
                                value={formData.currentLatitude}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="modal-form-group">
                            <label className="modal-label">Starting Long.</label>
                            <input
                                type="number"
                                step="any"
                                name="currentLongitude"
                                className="modal-input"
                                value={formData.currentLongitude}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="secondary-btn" onClick={onClose}>Cancel</button>
                        <button type="submit" className="primary-btn" disabled={submitting}>
                            {submitting ? 'Saving...' : isEdit ? 'Update Fleet Info' : 'Enroll Crew'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CrewFormModal;
