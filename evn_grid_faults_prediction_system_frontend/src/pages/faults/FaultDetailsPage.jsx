import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import faultRepository from '../../api/faultRepository';
import assignmentRepository from '../../api/assignmentRepository';
import Navbar from '../../components/Navbar';
import AssignmentModal from '../../components/AssignmentModal';
import './Faults.css';

const FaultDetailsPage = () => {
    const { id } = useParams();

    const [fault, setFault] = useState(null);
    const [history, setHistory] = useState([]);
    const [attachments, setAttachments] = useState([]);
    const [classification, setClassification] = useState(null);
    const [priority, setPriority] = useState(null);
    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);

    const [uploading, setUploading] = useState(false);
    const [showAssignModal, setShowAssignModal] = useState(false);
    
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

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <Link to="/faults" className="back-link">
                        ← Back to Faults
                    </Link>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button 
                            className="primary-btn" 
                            style={{ backgroundColor: '#10b981' }}
                            onClick={() => setShowAssignModal(true)}
                        >
                            {assignment ? 'Reassign Crew' : 'Assign to Crew'}
                        </button>
                    </div>
                </div>

                <div className="page-header">
                    <div>
                        <h1 className="title">{fault.title}</h1>
                        <p style={{ color: '#94a3b8', margin: '0.25rem 0 0 0' }}>Code: {fault.trackingCode}</p>
                    </div>
                    <span className={`badge ${getPriorityBadgeClass(fault.faultPriority)}`} style={{ fontSize: '1rem' }}>
                        {fault.faultPriority || 'UNASSIGNED'}
                    </span>
                </div>

                <div className="details-grid">
                    <div className="main-details">
                        <div className="glass-panel" style={{ marginBottom: '2rem' }}>
                            <h2 className="section-header">General Info</h2>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <p className="detail-row"><strong>Type:</strong> {fault.faultType?.replace('_', ' ')}</p>
                                <p className="detail-row"><strong>Status:</strong> {fault.currentStatus}</p>
                                <p className="detail-row"><strong>Reported:</strong> {new Date(fault.reportedAt).toLocaleString()}</p>
                                <p className="detail-row"><strong>Location:</strong> {fault.locationAddress || `Location #${fault.locationId}`}</p>
                                {assignment && (
                                    <p className="detail-row" style={{ gridColumn: 'span 2' }}>
                                        <strong>Assigned Crew:</strong> <span style={{ color: '#10b981', fontWeight: 600 }}>{assignment.crewName}</span>
                                    </p>
                                )}
                            </div>

                            <h3 className="section-header" style={{ marginTop: '1.5rem', fontSize: '1.1rem' }}>Description</h3>
                            <div className="description-box">
                                {fault.description}
                            </div>
                        </div>

                        {/* Management Actions */}
                        <div className="glass-panel" style={{ marginBottom: '2rem' }}>
                            <h2 className="section-header">Management Actions</h2>
                            <div className="status-update-form">
                                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                                    <div style={{ flex: 1 }}>
                                        <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.8rem', marginBottom: '0.5rem' }}>Update Status</label>
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
                                        <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.8rem', marginBottom: '0.5rem' }}>Resolution Note / Update</label>
                                        <textarea 
                                            className="search-input" 
                                            style={{ width: '100%', padding: '0.6rem', height: '40px', resize: 'none' }}
                                            placeholder="Required for Resolved/Closed"
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
                                    {updatingStatus ? 'Updating...' : 'Update Status'}
                                </button>
                            </div>
                        </div>

                        <div className="glass-panel">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                                <h2 className="section-header" style={{ margin: 0 }}>Attachments</h2>
                                <div className="file-input-wrapper">
                                    <button className="secondary-btn">{uploading ? 'Uploading...' : '+ Upload File'}</button>
                                    <input type="file" onChange={handleFileUpload} disabled={uploading} />
                                </div>
                            </div>

                            {attachments.length === 0 ? (
                                <p className="text-muted">No attachments yet.</p>
                            ) : (
                                <ul className="attachment-list">
                                    {attachments.map(att => (
                                        <li key={att.id} className="attachment-item">
                                            <span style={{ color: '#cbd5e1' }}>{att.fileName} ({(att.fileSize / 1024).toFixed(1)} KB)</span>
                                            <button className="secondary-btn" style={{ padding: '0.2rem 0.6rem', fontSize: '0.8rem' }} onClick={() => faultRepository.downloadAttachment(id, att.id, att.fileName)}>
                                                Download
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
                                <h2 style={{ marginTop: 0, color: '#f1f5f9', fontSize: '1.2rem', marginBottom: '1rem' }}>AI Analysis</h2>
                                {classification && (
                                    <div style={{ marginBottom: '1rem' }}>
                                        <p style={{ color: '#94a3b8', margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Classification</p>
                                        <p style={{ color: '#c084fc', margin: 0, fontWeight: '600' }}>{classification.predictedFaultCategory}</p>
                                        <p style={{ color: '#64748b', margin: '0.25rem 0 0 0', fontSize: '0.8rem' }}>Confidence: {(classification.classificationConfidence * 100).toFixed(1)}%</p>
                                    </div>
                                )}
                                {priority && (
                                    <div>
                                        <p style={{ color: '#94a3b8', margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Calculated Priority Score</p>
                                        <p style={{ color: '#38bdf8', margin: 0, fontWeight: '600' }}>{priority.priorityScore}</p>
                                        <p style={{ color: '#64748b', margin: '0.25rem 0 0 0', fontSize: '0.8rem' }}>Source: {priority.calculationSource}</p>
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="glass-panel">
                            <h2 style={{ marginTop: 0, color: '#f1f5f9', fontSize: '1.2rem', marginBottom: '1.5rem' }}>Status History</h2>
                            <div className="timeline">
                                {history.length === 0 ? (
                                    <p style={{ color: '#94a3b8' }}>No history recorded.</p>
                                ) : (
                                    history.map((entry, idx) => (
                                        <div key={idx} className="timeline-item">
                                            <p style={{ margin: 0, color: '#e2e8f0', fontWeight: '500' }}>{entry.faultStatus}</p>
                                            <p style={{ margin: '0.25rem 0 0 0', color: '#64748b', fontSize: '0.8rem' }}>
                                                {new Date(entry.changedAt).toLocaleString()} by {entry.changedByName || 'System'}
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

