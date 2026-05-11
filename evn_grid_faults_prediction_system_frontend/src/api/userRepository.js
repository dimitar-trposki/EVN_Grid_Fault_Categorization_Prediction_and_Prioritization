import api from './axiosConfig';

const userRepository = {
    getAll: () => api.get('/v1/users'),
    getById: (id) => api.get(`/v1/users/${id}`),
};

export default userRepository;
