import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authStore';
import faultRepository from '../../api/faultRepository';
import assignmentRepository from '../../api/assignmentRepository';
import Navbar from '../../components/Navbar';
import AssignmentModal from '../../components/AssignmentModal';
import './Faults.css';

const FaultDetailsPage = () => {
    const { id } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [fault, setFault] = useState(null);
    const [history, setHistory] = useState([]);
    const [attachments, setAttachments] = useState([]);
    const [classification, setClassification] = useState(null);
    const [priority, setPriority] = useState(null);
    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);

    const [uploading, setUploading] = useState(false);
    const [showAssignModal, setShowAssignModal] = useState(false);
    
    // Edit State
    const [isEditingDesc, setIsEditingDesc] = useState(false);
    const [editDescription, setEditDescription] = useState('');
    const [savingEdit, setSavingEdit] = useState(false);

    // Status management state
    const [newStatus, setNewStatus] = useState('');
    const [statusNote, setStatusNote] = useState('');
    const [updatingStatus, setUpdatingStatus] = useState(false);

    const fetchData = async () => {
        setLoading(true);
        try {
            const faultRes = await faultRepository.getById(id);
            setFault(faultRes.data);
            setNewStatus(faultRes.data.currentStatus);
            setEditDescription(faultRes.data.description);

            const [histRes, attRes, classRes, prioRes, assignRes] = await Promise.all([
                faultRepository.getHistory(id).catch(() => ({ data: [] })),
                faultRepository.getAttachments(id).catch(() => ({ data: [] })),
                faultRepository.getClassification(id).catch(() => ({ data: null })),
                faultRepository.getPriority(id).catch(() => ({ data: null })),
                assignmentRepository.getByFault(id).catch(() => ({ data: null }))
            ]);

            setHistory(histRes.data);
            setAttachments(attRes.data);
            setClassification(classRes.data);
            setPriority(prioRes.data);
            setAssignment(assignRes.data);
        } catch (err) {
            console.error('Failed to load fault details', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [id]);

    const handleUpdateDescription = async () => {
        if (!editDescription.trim()) return;
        setSavingEdit(true);
        try {
            await faultRepository.updateFault(id, {
                title: fault.title,
                description: editDescription,
                locationId: fault.locationId,
                faultType: fault.faultType,
                faultPriority: fault.faultPriority,
                faultClassification: fault.faultClassification
            });
            setIsEditingDesc(false);
            setFault(prev => ({ ...prev, description: editDescription }));
        } catch (err) {
            console.error(err);
            alert("Failed to save changes.");
        } finally {
            setSavingEdit(false);
        }
    };

    const handleDeleteFault = async () => {
        if (!window.confirm("CRITICAL WARNING: Are you positive you wish to permanently purge this record from the main ledger? This action cannot be undone.")) return;
        try {
            await faultRepository.deleteFault(id);
            alert("Ticket purged successfully.");
            navigate('/faults');
        } catch (err) {
            console.error(err);
            alert("Fail to delete ticket. It may possess tied dependent histories.");
        }
    };

    const handleFileUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        const formData = new FormData();
        formData.append('file', file);

        try {
            await faultRepository.uploadAttachment(id, formData);
            const attRes = await faultRepository.getAttachments(id);
            setAttachments(attRes.data);
            e.target.value = null; // reset input
        } catch (err) {
            console.error('Failed to upload', err);
            alert('Upload failed');
        } finally {
            setUploading(false);
        }
    };

    const handleStatusUpdate = async () => {
        if (!newStatus) return;
        if ((newStatus === 'RESOLVED' || newStatus === 'CLOSED') && !statusNote) {
            alert('Please provide a resolution note.');
            return;
        }

        setUpdatingStatus(true);
        try {
            await faultRepository.updateStatus(id, {
                status: newStatus,
                note: statusNote,
                customerVisible: true
            });
            await fetchData();
            setStatusNote('');
            alert('Status updated successfully');
        } catch (err) {
            console.error('Failed to update status', err);
            alert('Failed to update status');
        } finally {
            setUpdatingStatus(false);
        }
    };

    const getPriorityBadgeClass = (level) => {
        switch (level) {
            case 'CRITICAL': return 'badge-critical';
            case 'HIGH': return 'badge-high';
            case 'MEDIUM': return 'badge-medium';
            case 'LOW': return 'badge-low';
            default: return 'badge-default';
        }
    };

    if (loading) return (
        <div className="faults-container">
            <Navbar />
            <div className="centered-loading">
                <p className="text-muted">Loading fault details...</p>
            </div>
        </div>
    );

    if (!fault) return (
        <div className="faults-container">
            <Navbar />
            <p className="text-muted" style={{ textAlign: 'center', marginTop: '2rem' }}>Fault not found.</p>
        </div>
    );

    const canManage = user?.role !== 'CUSTOMER';
    const isAdmin = user?.role === 'ADMIN';

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <Link to="/faults" className="back-link">
                        ← Back to Ledger
                    </Link>
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                        {isAdmin && (
                            <button 
                                className="secondary-btn" 
                                style={{ borderColor: '#ef4444', color: '#ef4444' }}
                                onClick={handleDeleteFault}
                            >
                                🗑️ Archive Ticket
                            </button>
                        )}
                        {canManage && (
                            <button 
                                className="primary-btn" 
                                style={{ backgroundColor: '#10b981' }}
                                onClick={() => setShowAssignModal(true)}
                            >
                                {assignment ? '🔄 Reassign Crew' : '📥 Assign Crew'}
                            </button>
                        )}
                    </div>
                </div>

                <div className="page-header">
                    <div>
                        <h1 className="title">{fault.title}</h1>
                        <p style={{ color: '#94a3b8', margin: '0.25rem 0 0 0' }}>Internal Key: {fault.trackingCode}</p>
                    </div>
                    <span className={`badge ${getPriorityBadgeClass(fault.faultPriority)}`} style={{ fontSize: '1rem' }}>
                        {fault.faultPriority || 'UNASSIGNED'}
                    </span>
                </div>

                <div className="details-grid">
                    <div className="main-details">
                        <div className="glass-panel" style={{ marginBottom: '2rem' }}>
                            <h2 className="section-header">Telemetry Diagnostics</h2>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <p className="detail-row"><strong>Domain Type:</strong> {fault.faultType?.replace('_', ' ')}</p>
                                <p className="detail-row"><strong>Live Status:</strong> {fault.currentStatus}</p>
                                <p className="detail-row"><strong>Log Timestamp:</strong> {new Date(fault.reportedAt).toLocaleString()}</p>
                                <p className="detail-row"><strong>Network Node:</strong> {fault.locationAddress || `ID: ${fault.locationId}`}</p>
                                {assignment && (
                                    <p className="detail-row" style={{ gridColumn: 'span 2' }}>
                                        <strong>Deployed Crew:</strong> <span style={{ color: '#34d399', fontWeight: 600 }}>{assignment.crewName}</span>
                                    </p>
                                )}
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem', borderTop: '1px solid #334155', paddingTop: '1.5rem' }}>
                                <h3 className="section-header" style={{ fontSize: '1.1rem', margin: 0 }}>Description Detail</h3>
                                {canManage && !isEditingDesc && (
                                    <button className="secondary-btn" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }} onClick={() => setIsEditingDesc(true)}>✏️ Edit</button>
                                )}
                            </div>

                            <div style={{ marginTop: '0.75rem' }}>
                                {isEditingDesc ? (
                                    <div>
                                        <textarea 
                                            className="search-input" 
                                            style={{ width: '100%', minHeight: '100px', padding: '0.75rem' }} 
                                            value={editDescription} 
                                            onChange={e => setEditDescription(e.target.value)} 
                                        />
                                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                                            <button className="primary-btn" style={{ fontSize: '0.8rem' }} onClick={handleUpdateDescription} disabled={savingEdit}>
                                                {savingEdit ? 'Saving...' : 'Save Revision'}
                                            </button>
                                            <button className="secondary-btn" style={{ fontSize: '0.8rem' }} onClick={() => setIsEditingDesc(false)}>Cancel</button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="description-box">
                                        {fault.description || "No telemetry notes provided."}
                                    </div>
                                )}
                            </div>
                        </div>

                        {canManage && (
                            <div className="glass-panel" style={{ marginBottom: '2rem' }}>
                                <h2 className="section-header">Operational Workflow Control</h2>
                                <div className="status-update-form">
                                    <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                                        <div style={{ flex: 1 }}>
                                            <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.8rem', marginBottom: '0.5rem' }}>Cycle Advancement</label>
                                            <select 
                                                className="search-input" 
                                                style={{ width: '100%', padding: '0.6rem' }}
                                                value={newStatus}
                                                onChange={(e) => setNewStatus(e.target.value)}
                                            >
                                                <option value="REPORTED">REPORTED</option>
                                                <option value="ASSIGNED">ASSIGNED</option>
                                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                                                <option value="RESOLVED">RESOLVED</option>
                                                <option value="CLOSED">CLOSED</option>
                                            </select>
                                        </div>
                                        <div style={{ flex: 2 }}>
                                            <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.8rem', marginBottom: '0.5rem' }}>Resolution/Handoff Directive</label>
                                            <textarea 
                                                className="search-input" 
                                                style={{ width: '100%', padding: '0.6rem', height: '40px', resize: 'none' }}
                                                placeholder="Brief note mandatory for resolution..."
                                                value={statusNote}
                                                onChange={(e) => setStatusNote(e.target.value)}
                                            />
                                        </div>
                                    </div>
                                    <button 
                                        className="primary-btn" 
                                        style={{ width: '100%' }}
                                        onClick={handleStatusUpdate}
                                        disabled={updatingStatus || newStatus === fault.currentStatus}
                                    >
                                        {updatingStatus ? 'Propagating Cycle Update...' : 'Execute State Shift'}
                                    </button>
                                </div>
                            </div>
                        )}

                        <div className="glass-panel">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                                <h2 className="section-header" style={{ margin: 0 }}>Telemetry Objects / Media</h2>
                                <div className="file-input-wrapper">
                                    <button className="secondary-btn">{uploading ? 'Uplinking...' : '📤 Upload Pack'}</button>
                                    <input type="file" onChange={handleFileUpload} disabled={uploading} />
                                </div>
                            </div>

                            {attachments.length === 0 ? (
                                <p className="text-muted">Zero payload packets detected.</p>
                            ) : (
                                <ul className="attachment-list">
                                    {attachments.map(att => (
                                        <li key={att.id} className="attachment-item">
                                            <span style={{ color: '#cbd5e1' }}>{att.fileName} ({(att.fileSize / 1024).toFixed(1)} KB)</span>
                                            <button className="secondary-btn" style={{ padding: '0.2rem 0.6rem', fontSize: '0.8rem' }} onClick={() => faultRepository.downloadAttachment(id, att.id, att.fileName)}>
                                                ⬇ Download
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    </div>

                    <div className="side-details">
                        {(classification || priority) && (
                            <div className="glass-panel" style={{ marginBottom: '2rem' }}>
                                <h2 style={{ marginTop: 0, color: '#f1f5f9', fontSize: '1.2rem', marginBottom: '1rem' }}>Neural Engine Evaluation</h2>
                                {classification && (
                                    <div style={{ marginBottom: '1rem' }}>
                                        <p style={{ color: '#94a3b8', margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Inferred Vector</p>
                                        <p style={{ color: '#c084fc', margin: 0, fontWeight: '600' }}>{classification.predictedFaultCategory}</p>
                                        <p style={{ color: '#64748b', margin: '0.25rem 0 0 0', fontSize: '0.8rem' }}>Probability Index: {(classification.classificationConfidence * 100).toFixed(1)}%</p>
                                    </div>
                                )}
                                {priority && (
                                    <div>
                                        <p style={{ color: '#94a3b8', margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Computed Kinetic Priority</p>
                                        <p style={{ color: '#38bdf8', margin: 0, fontWeight: '600' }}>SCALAR {priority.priorityScore}</p>
                                        <p style={{ color: '#64748b', margin: '0.25rem 0 0 0', fontSize: '0.8rem' }}>Engine ID: {priority.calculationSource}</p>
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="glass-panel">
                            <h2 style={{ marginTop: 0, color: '#f1f5f9', fontSize: '1.2rem', marginBottom: '1.5rem' }}>Lifecycle Ledger</h2>
                            <div className="timeline">
                                {history.length === 0 ? (
                                    <p style={{ color: '#94a3b8' }}>Base state initial.</p>
                                ) : (
                                    history.map((entry, idx) => (
                                        <div key={idx} className="timeline-item">
                                            <p style={{ margin: 0, color: '#e2e8f0', fontWeight: '500' }}>{entry.faultStatus}</p>
                                            <p style={{ margin: '0.25rem 0 0 0', color: '#64748b', fontSize: '0.8rem' }}>
                                                {new Date(entry.changedAt).toLocaleString()} • Operator: {entry.changedByName || 'Auto'}
                                            </p>
                                            {entry.note && (
                                                <p style={{ margin: '0.5rem 0 0 0', color: '#94a3b8', fontSize: '0.9rem', fontStyle: 'italic' }}>"{entry.note}"</p>
                                            )}
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {showAssignModal && (
                <AssignmentModal 
                    faultId={id} 
                    onClose={() => setShowAssignModal(false)} 
                    onSuccess={fetchData}
                />
            )}
        </div>
    );
};

export default FaultDetailsPage;


