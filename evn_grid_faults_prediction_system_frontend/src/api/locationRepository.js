import api from './axiosConfig';

const locationRepository = {
    getAll: () => api.get('/locations'),
    getById: (id) => api.get(`/locations/${id}`),
    getByRegion: (regionId) => api.get(`/regions/${regionId}/locations`),
    create: (data) => api.post('/locations', data),
    update: (id, data) => api.put(`/locations/${id}`, data),
    delete: (id) => api.delete(`/locations/${id}`),
};

export default locationRepository;