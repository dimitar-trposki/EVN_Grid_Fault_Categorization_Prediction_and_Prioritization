import api from './axiosConfig';

const locationRepository = {
    getAll: () => api.get('/v1/locations'),
    getByRegion: (regionId) => api.get(`/v1/regions/${regionId}/locations`),
    getById: (id) => api.get(`/v1/locations/${id}`),
};

export default locationRepository;
