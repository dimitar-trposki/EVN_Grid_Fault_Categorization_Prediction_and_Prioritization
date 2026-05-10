import api from './axiosConfig';

const faultRepository = {
    // Queries
    getAll: (params) => api.get('/v1/faults', { params }), // supports page, size, regionId, locationId, status, priority, type
    getMyFaults: (params) => api.get('/v1/faults/my', { params }),
    getById: (id) => api.get(`/v1/faults/${id}`),
    trackByCode: (code) => api.get(`/v1/faults/track/${code}`),
    getHistory: (id) => api.get(`/v1/faults/${id}/history`),

    // Mutations
    createByCustomer: (data) => api.post('/v1/faults', data),
    createByOperator: (data) => api.post('/v1/faults/operator', data),
    updateStatus: (id, statusData) => api.patch(`/v1/faults/${id}/status`, statusData),
    updateFault: (id, data) => api.put(`/v1/faults/${id}`, data),
    
    // Attachments
    getAttachments: (id) => api.get(`/v1/faults/${id}/attachments`),
    uploadAttachment: (id, formData) => api.post(`/v1/faults/${id}/attachments`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    downloadAttachment: (faultId, attachmentId) => api.get(`/v1/faults/${faultId}/attachments/${attachmentId}/download`, { responseType: 'blob' }),

    // AI Classification
    getClassification: (id) => api.get(`/v1/faults/${id}/classification`),
    classifyFault: (id) => api.post(`/v1/faults/${id}/classification/classify`),
    overrideClassification: (id, data) => api.put(`/v1/faults/${id}/classification/override`, data),

    // Priority
    getPriority: (id) => api.get(`/v1/faults/${id}/priority`),
    calculatePriority: (id) => api.post(`/v1/faults/${id}/priority/calculate`),
    overridePriority: (id, data) => api.put(`/v1/faults/${id}/priority/override`, data)
};

export default faultRepository;
