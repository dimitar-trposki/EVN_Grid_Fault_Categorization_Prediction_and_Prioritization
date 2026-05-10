import api from './axiosConfig';

const equipmentRepository = {
    getAll: () => api.get('/equipment'),
    getById: (id) => api.get(`/equipment/${id}`),
    getByLocation: (locationId) => api.get(`/equipment/location/${locationId}`),
    getByType: (type) => api.get(`/equipment/type/${type}`),
    create: (data) => api.post('/equipment', data),
    update: (id, data) => api.put(`/equipment/${id}`, data),
    delete: (id) => api.delete(`/equipment/${id}`),
};

export default equipmentRepository;