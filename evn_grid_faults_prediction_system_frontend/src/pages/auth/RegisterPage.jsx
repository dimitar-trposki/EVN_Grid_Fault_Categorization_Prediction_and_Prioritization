import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authStore';

const RegisterPage = () => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { register } = useAuth();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }
        setLoading(true);
        setError('');
        try {
            await register(formData);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>Create Account</h2>
                <p style={styles.subtitle}>Join EVN Grid System</p>

                {error && <div style={styles.error}>{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div style={styles.row}>
                        <div style={styles.field}>
                            <label style={styles.label}>First Name</label>
                            <input
                                style={styles.input}
                                type="text"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                placeholder="John"
                                required
                            />
                        </div>
                        <div style={styles.field}>
                            <label style={styles.label}>Last Name</label>
                            <input
                                style={styles.input}
                                type="text"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                placeholder="Doe"
                                required
                            />
                        </div>
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>Email</label>
                        <input
                            style={styles.input}
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="your@email.com"
                            required
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>Phone</label>
                        <input
                            style={styles.input}
                            type="tel"
                            name="phone"
                            value={formData.phone}
                            onChange={handleChange}
                            placeholder="+389 70 123 456"
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>Password</label>
                        <input
                            style={styles.input}
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>Confirm Password</label>
                        <input
                            style={styles.input}
                            type="password"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <button style={styles.button} type="submit" disabled={loading}>
                        {loading ? 'Creating account...' : 'Register'}
                    </button>
                </form>

                <p style={styles.loginText}>
                    Already have an account?{' '}
                    <span style={styles.link} onClick={() => navigate('/login')}>
            Sign in
          </span>
                </p>
            </div>
        </div>
    );
};

const styles = {
    container: {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#0f172a',
    },
    card: {
        backgroundColor: '#1e293b',
        padding: '2rem',
        borderRadius: '12px',
        width: '100%',
        maxWidth: '450px',
        boxShadow: '0 4px 24px rgba(0,0,0,0.3)',
    },
    title: {
        color: '#f1f5f9',
        textAlign: 'center',
        marginBottom: '0.5rem',
        fontSize: '1.8rem',
    },
    subtitle: {
        color: '#94a3b8',
        textAlign: 'center',
        marginBottom: '1.5rem',
    },
    error: {
        backgroundColor: '#fee2e2',
        color: '#dc2626',
        padding: '0.75rem',
        borderRadius: '8px',
        marginBottom: '1rem',
        textAlign: 'center',
    },
    row: {
        display: 'flex',
        gap: '1rem',
    },
    field: {
        marginBottom: '1rem',
        flex: 1,
    },
    label: {
        display: 'block',
        color: '#94a3b8',
        marginBottom: '0.5rem',
        fontSize: '0.9rem',
    },
    input: {
        width: '100%',
        padding: '0.75rem',
        borderRadius: '8px',
        border: '1px solid #334155',
        backgroundColor: '#0f172a',
        color: '#f1f5f9',
        fontSize: '1rem',
        boxSizing: 'border-box',
    },
    button: {
        width: '100%',
        padding: '0.75rem',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#3b82f6',
        color: 'white',
        fontSize: '1rem',
        cursor: 'pointer',
        marginTop: '0.5rem',
    },
    loginText: {
        color: '#94a3b8',
        textAlign: 'center',
        marginTop: '1rem',
    },
    link: {
        color: '#3b82f6',
        cursor: 'pointer',
    },
};

export default RegisterPage;