import { useState, useEffect } from 'react';
import crewRepository from '../api/crewRepository';
import userRepository from '../api/userRepository';

const CrewMembersModal = ({ crewId, crewName, onClose, onSuccess }) => {
    const [currentMembers, setCurrentMembers] = useState([]);
    const [availableUsers, setAvailableUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [processingId, setProcessingId] = useState(null);
    const [error, setError] = useState(null);

    // Form for adding new
    const [selectedUserId, setSelectedUserId] = useState('');
    const [memberPosition, setMemberPosition] = useState('TECHNICIAN');

    const loadAllData = async () => {
        setLoading(true);
        setError(null);
        try {
            const [membersRes, usersRes] = await Promise.all([
                crewRepository.getMembers(crewId),
                userRepository.getAll()
            ]);
            
            setCurrentMembers(membersRes.data);
            
            // Filter available users down to candidates not already explicitly in this crew
            const currentMemberUserIds = membersRes.data.map(m => m.userId);
            const potentialCandidates = usersRes.data.filter(u => 
                !currentMemberUserIds.includes(u.id) && u.role === 'FIELD_CREW'
            );
            setAvailableUsers(potentialCandidates);

            if (potentialCandidates.length > 0) {
                setSelectedUserId(potentialCandidates[0].id);
            } else {
                setSelectedUserId('');
            }
        } catch (err) {
            console.error("Load failed", err);
            setError("Critical API breakdown downloading user directories.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAllData();
    }, [crewId]);

    const handleAdd = async (e) => {
        e.preventDefault();
        if (!selectedUserId) return;
        
        setProcessingId('ADD');
        try {
            await crewRepository.addMember(crewId, {
                userId: Number(selectedUserId),
                position: memberPosition
            });
            await loadAllData();
            onSuccess(); // signal page to refresh counts
        } catch (err) {
            console.error(err);
            alert("Add execution failed.");
        } finally {
            setProcessingId(null);
        }
    };

    const handleRemove = async (memberId) => {
        if (!window.confirm("Release technician from active duty role in this crew?")) return;

        setProcessingId(memberId);
        try {
            await crewRepository.removeMember(crewId, memberId);
            await loadAllData();
            onSuccess(); 
        } catch (err) {
            console.error(err);
            alert("Deregistration rejected by server.");
        } finally {
            setProcessingId(null);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ maxWidth: '600px' }}>
                <div className="modal-header">
                    <h2>Roster Management: {crewName}</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>

                <div className="modal-body">
                    {loading ? (
                        <p style={{ color: '#94a3b8' }}>Reading personnel records...</p>
                    ) : (
                        <>
                            {error && <div className="modal-error-banner">{error}</div>}
                            
                            <h3 style={{ color: '#f1f5f9', fontSize: '1rem', margin: '0 0 1rem' }}>Currently Assigned Team</h3>
                            
                            {currentMembers.length === 0 ? (
                                <p style={{ color: '#64748b', fontStyle: 'italic', padding: '1rem', border: '1px dashed #334155', borderRadius: '8px', textAlign: 'center' }}>
                                    Skeleton crew. No active members registered.
                                </p>
                            ) : (
                                <div className="crew-list" style={{ marginBottom: '2rem' }}>
                                    {currentMembers.map(member => (
                                        <div key={member.id} className="crew-item">
                                            <div className="crew-info">
                                                <p className="crew-name">{member.firstName} {member.lastName}</p>
                                                <p className="crew-meta">{member.position || 'FIELD SPECIALIST'}</p>
                                            </div>
                                            <button 
                                                className="secondary-btn" 
                                                style={{ borderColor: '#ef4444', color: '#ef4444', padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                                                onClick={() => handleRemove(member.id)}
                                                disabled={processingId === member.id}
                                            >
                                                {processingId === member.id ? 'Removing...' : 'Remove'}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}

                            <hr style={{ borderColor: '#334155', margin: '1.5rem 0' }} />
                            <h3 style={{ color: '#f1f5f9', fontSize: '1rem', margin: '0 0 1rem' }}>Recruit New Member</h3>

                            <form onSubmit={handleAdd} style={{ background: '#0f172a', padding: '1rem', borderRadius: '8px', border: '1px solid #334155' }}>
                                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr auto', gap: '0.75rem', alignItems: 'flex-end' }}>
                                    <div>
                                        <label className="modal-label">Field Specialist</label>
                                        <select 
                                            className="modal-input" 
                                            value={selectedUserId}
                                            onChange={e => setSelectedUserId(e.target.value)}
                                            disabled={availableUsers.length === 0}
                                        >
                                            {availableUsers.length === 0 ? (
                                                <option>No unassigned specialists</option>
                                            ) : (
                                                availableUsers.map(u => (
                                                    <option key={u.id} value={u.id}>{u.firstName} {u.lastName} ({u.username})</option>
                                                ))
                                            )}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="modal-label">Role Duty</label>
                                        <input 
                                            className="modal-input" 
                                            placeholder="e.g. DRIVER" 
                                            value={memberPosition}
                                            onChange={e => setMemberPosition(e.target.value)}
                                        />
                                    </div>
                                    <button 
                                        type="submit" 
                                        className="primary-btn" 
                                        disabled={!selectedUserId || processingId === 'ADD'}
                                        style={{ padding: '0.8rem' }}
                                    >
                                        {processingId === 'ADD' ? '...' : '+ Add'}
                                    </button>
                                </div>
                            </form>
                        </>
                    )}
                </div>
                <div className="modal-footer">
                    <button className="secondary-btn" onClick={onClose}>Done</button>
                </div>
            </div>
        </div>
    );
};

export default CrewMembersModal;
