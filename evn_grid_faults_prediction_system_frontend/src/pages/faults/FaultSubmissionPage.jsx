import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/authStore';
import faultRepository from '../../api/faultRepository';
import locationRepository from '../../api/locationRepository';
import Navbar from '../../components/Navbar';
import './Faults.css';

const FaultTypes = [
    'SHORT_CIRCUIT',
    'OVERLOAD',
    'EQUIPMENT_FAILURE',
    'POWER_OUTAGE',
    'VOLTAGE_DROP',
    'INSULATION_FAILURE',
    'GROUND_FAULT',
    'OTHER'
];

const FaultSubmissionPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [locations, setLocations] = useState([]);
    
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        faultType: 'POWER_OUTAGE',
        locationId: ''
    });
    
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchLocations = async () => {
            try {
                const res = await locationRepository.getAll();
                setLocations(res.data.content || res.data);
                if ((res.data.content || res.data).length > 0) {
                    setFormData(prev => ({ ...prev, locationId: (res.data.content || res.data)[0].id }));
                }
            } catch (err) {
                console.error("Failed to fetch locations", err);
            }
        };
        fetchLocations();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        if (!formData.locationId) {
            setError('Please select a valid location before submitting.');
            setSubmitting(false);
            return;
        }

        const payload = {
            ...formData,
            locationId: Number(formData.locationId)
        };

        try {
            if (user?.role === 'CUSTOMER') {
                await faultRepository.createByCustomer(payload);
            } else {
                await faultRepository.createByOperator(payload);
            }
            navigate('/faults');
        } catch (err) {
            console.error('Submission failed', err);
            let errorMsg = 'Failed to submit fault report. Please try again.';
            if (err.response?.data) {
                errorMsg = typeof err.response.data === 'string' 
                    ? err.response.data 
                    : JSON.stringify(err.response.data);
            }
            setError(errorMsg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="faults-container">
            <Navbar />

            <div className="faults-content">
                <Link to="/faults" className="back-link">
                    ← Back to Faults
                </Link>

                <div className="page-header">
                    <h1 className="title">Report a Fault</h1>
                </div>

                <div className="glass-panel" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    {error && <div style={{color: '#ef4444', marginBottom: '1rem', background: 'rgba(239, 68, 68, 0.1)', padding: '1rem', borderRadius: '8px'}}>{error}</div>}
                    
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Title</label>
                            <input 
                                type="text" 
                                name="title"
                                className="form-control" 
                                value={formData.title}
                                onChange={handleChange}
                                required 
                                maxLength={80}
                                placeholder="Brief description of the issue (max 80 chars)"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Description</label>
                            <textarea 
                                name="description"
                                className="form-control" 
                                value={formData.description}
                                onChange={handleChange}
                                required
                                rows="4"
                                placeholder="Provide more details..."
                            ></textarea>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Fault Type</label>
                                <select 
                                    name="faultType"
                                    className="form-control"
                                    value={formData.faultType}
                                    onChange={handleChange}
                                >
                                    {FaultTypes.map(type => (
                                        <option key={type} value={type}>{type.replace('_', ' ')}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Location</label>
                                <select 
                                    name="locationId"
                                    className="form-control"
                                    value={formData.locationId}
                                    onChange={handleChange}
                                    required
                                    disabled={locations.length === 0}
                                >
                                    {locations.length === 0 ? (
                                        <option value="">No locations available</option>
                                    ) : (
                                        locations.map(loc => (
                                            <option key={loc.id} value={loc.id}>
                                                {loc.address || `${loc.latitude?.toFixed?.(4) ?? 'N/A'}, ${loc.longitude?.toFixed?.(4) ?? 'N/A'}`}
                                            </option>
                                        ))
                                    )}
                                </select>
                            </div>
                        </div>

                        <button type="submit" className="primary-btn" style={{width: '100%'}} disabled={submitting}>
                            {submitting ? 'Submitting...' : 'Submit Report'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default FaultSubmissionPage;
