import api from './axiosConfig';

const equipmentRepository = {
    getAll: () => api.get('/v1/equipment'),
    getByLocation: (locationId) => api.get(`/v1/locations/${locationId}/equipment`),
    getById: (id) => api.get(`/v1/equipment/${id}`),
    getByType: (type) => api.get(`/v1/equipment/by-type`, { params: { type } }),
};

export default equipmentRepository;
