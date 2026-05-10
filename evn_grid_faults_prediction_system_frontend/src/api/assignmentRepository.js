import api from './axiosConfig';

const assignmentRepository = {
    assignCrew: (data) =>
        api.post('/v1/assignments', data),

    reassignCrew: (faultId, data) =>
        api.put(`/v1/assignments/fault/${faultId}/reassign`, data),

    getRecommendations: (faultId) =>
        api.get(`/v1/assignments/recommendations/${faultId}`),

    getByFault: (faultId) =>
        api.get(`/v1/assignments/fault/${faultId}`),
};

export default assignmentRepository;
