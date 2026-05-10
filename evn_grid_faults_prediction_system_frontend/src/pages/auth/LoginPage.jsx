import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const LoginPage = () => {
    const [credentials, setCredentials] = useState({ email: '', password: '' });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setCredentials({ ...credentials, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await login(credentials);
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid email or password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>EVN Grid System</h2>
                <p style={styles.subtitle}>Sign in to your account</p>

                {error && <div style={styles.error}>{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div style={styles.field}>
                        <label style={styles.label}>Email</label>
                        <input
                            style={styles.input}
                            type="email"
                            name="email"
                            value={credentials.email}
                            onChange={handleChange}
                            placeholder="your@email.com"
                            required
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>Password</label>
                        <input
                            style={styles.input}
                            type="password"
                            name="password"
                            value={credentials.password}
                            onChange={handleChange}
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <button style={styles.button} type="submit" disabled={loading}>
                        {loading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>

                <p style={styles.registerText}>
                    Don't have an account?{' '}
                    <span style={styles.link} onClick={() => navigate('/register')}>
            Register here
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
        maxWidth: '400px',
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
    field: {
        marginBottom: '1rem',
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
    registerText: {
        color: '#94a3b8',
        textAlign: 'center',
        marginTop: '1rem',
    },
    link: {
        color: '#3b82f6',
        cursor: 'pointer',
    },
};

export default LoginPage;