import { createContext, useContext, useState, useEffect } from 'react';
import authRepository from '../api/authRepository';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const mapAuthPayloadToUser = (payload) => ({
        userId: payload.userId,
        email: payload.email,
        firstName: payload.firstName,
        lastName: payload.lastName,
        role: payload.role,
    });

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            authRepository.getProfile()
                .then(res => setUser(res.data))
                .catch(() => localStorage.removeItem('token'))
                .finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, []);

    const login = async (credentials) => {
        const res = await authRepository.login(credentials);
        localStorage.setItem('token', res.data.token);
        setUser(mapAuthPayloadToUser(res.data));
        return res.data;
    };

    const register = async (formData) => {
        const res = await authRepository.register(formData);
        localStorage.setItem('token', res.data.token);
        setUser(mapAuthPayloadToUser(res.data));
        return res.data;
    };

    const logout = async () => {
        await authRepository.logout();
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);