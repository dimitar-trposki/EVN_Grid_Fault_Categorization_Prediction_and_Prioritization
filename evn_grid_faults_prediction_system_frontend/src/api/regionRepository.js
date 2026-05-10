import api from './axiosConfig';

const regionRepository = {
    getAll: () => api.get('/regions'),
    getById: (id) => api.get(`/regions/${id}`),
    create: (data) => api.post('/regions', data),
    update: (id, data) => api.put(`/regions/${id}`, data),
    delete: (id) => api.delete(`/regions/${id}`),
};

export default regionRepository;