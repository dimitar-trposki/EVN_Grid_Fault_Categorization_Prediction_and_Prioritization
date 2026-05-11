import api from './axiosConfig';

const crewRepository = {
    listAll: () => api.get('/v1/crews'),
    getById: (id) => api.get(`/v1/crews/${id}`),
    getAvailable: () => api.get('/v1/crews/available'),
    
    create: (data) => api.post('/v1/crews', data),
    update: (id, data) => api.put(`/v1/crews/${id}`, data),
    delete: (id) => api.delete(`/v1/crews/${id}`),
    
    updateLocation: (id, data) => api.put(`/v1/crews/${id}/location`, data),
    
    // Member Management
    getMembers: (id) => api.get(`/v1/crews/${id}/members`),
    addMember: (crewId, memberData) => api.post(`/v1/crews/${crewId}/members`, memberData),
    removeMember: (crewId, memberId) => api.delete(`/v1/crews/${crewId}/members/${memberId}`),
};

export default crewRepository;

