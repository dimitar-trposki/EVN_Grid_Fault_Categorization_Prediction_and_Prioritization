import { useState, useEffect } from 'react';
import crewRepository from '../api/crewRepository';
import assignmentRepository from '../api/assignmentRepository';
import './AssignmentModal.css';

const AssignmentModal = ({ faultId, onClose, onSuccess }) => {
    const [crews, setCrews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCrews = async () => {
            try {
                const res = await crewRepository.getAvailable();
                setCrews(res.data);
            } catch (err) {
                console.error('Failed to load available crews', err);
                setError('Failed to load available crews');
            } finally {
                setLoading(false);
            }
        };
        fetchCrews();
    }, []);

    const handleAssign = async (crewId) => {
        setSubmitting(true);
        setError(null);
        try {
            await assignmentRepository.assignCrew({
                faultReportId: faultId,
                crewId: crewId
            });
            onSuccess();
            onClose();
        } catch (err) {
            console.error('Failed to assign crew', err);
            setError(err.response?.data?.message || 'Failed to assign crew');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h2>Assign Crew</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>
                
                <div className="modal-body">
                    {loading ? (
                        <p>Loading available crews...</p>
                    ) : error ? (
                        <p className="error-text">{error}</p>
                    ) : crews.length === 0 ? (
                        <p>No idle crews available at the moment.</p>
                    ) : (
                        <div className="crew-list">
                            {crews.map(crew => (
                                <div key={crew.id} className="crew-item">
                                    <div className="crew-info">
                                        <p className="crew-name">{crew.name}</p>
                                        <p className="crew-meta">{crew.regionName} • {crew.crewMembers?.length || 0} members</p>
                                    </div>
                                    <button 
                                        className="primary-btn" 
                                        onClick={() => handleAssign(crew.id)}
                                        disabled={submitting}
                                    >
                                        {submitting ? 'Assigning...' : 'Assign'}
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AssignmentModal;
