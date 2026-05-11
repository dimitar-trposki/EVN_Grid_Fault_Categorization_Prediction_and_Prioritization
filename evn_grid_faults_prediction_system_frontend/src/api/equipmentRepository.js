import api from './axiosConfig';

const equipmentRepository = {
    getAll: () => api.get('/v1/equipment'),
    getByLocation: (locationId) => api.get(`/v1/locations/${locationId}/equipment`),
    getById: (id) => api.get(`/v1/equipment/${id}`),
    getByType: (type) => api.get(`/v1/equipment/by-type`, { params: { type } }),
    create: (data) => api.post('/v1/equipment', data),
    update: (id, data) => api.put(`/v1/equipment/${id}`, data),
    delete: (id) => api.delete(`/v1/equipment/${id}`)
};

export default equipmentRepository;

