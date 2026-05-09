import api from './axiosConfig';

const authRepository = {
    login: (credentials) =>
        api.post('/auth/login', credentials),

    register: (userData) =>
        api.post('/auth/register', userData),

    logout: () =>
        api.post('/auth/logout'),

    getProfile: () =>
        api.get('/users/profile'),

    updateProfile: (data) =>
        api.put('/users/profile', data),

    changePassword: (data) =>
        api.put('/users/change-password', data),
};

export default authRepository;