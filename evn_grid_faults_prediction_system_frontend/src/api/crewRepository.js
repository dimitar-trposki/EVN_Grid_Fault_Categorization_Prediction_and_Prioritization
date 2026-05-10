import api from './axiosConfig';

const crewRepository = {
    listAll: () =>
        api.get('/v1/crews'),

    getById: (id) =>
        api.get(`/v1/crews/${id}`),

    getAvailable: () =>
        api.get('/v1/crews/available'),

    updateLocation: (id, data) =>
        api.put(`/v1/crews/${id}/location`, data),

    getMembers: (id) =>
        api.get(`/v1/crews/${id}/members`),
};

export default crewRepository;
