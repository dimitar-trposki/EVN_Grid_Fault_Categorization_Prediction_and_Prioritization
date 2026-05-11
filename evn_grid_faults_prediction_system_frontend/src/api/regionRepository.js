import api from './axiosConfig';

const regionRepository = {
    getAll: () => api.get('/v1/regions'),
    getById: (id) => api.get(`/v1/regions/${id}`),
    getLocations: (regionId) => api.get(`/v1/regions/${regionId}/locations`),
};

export default regionRepository;
