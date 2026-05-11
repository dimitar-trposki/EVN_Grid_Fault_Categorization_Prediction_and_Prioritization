import api from './axiosConfig';

const notificationRepository = {
    getAll: () => api.get('/v1/notifications'),
    getUnread: () => api.get('/v1/notifications/unread'),
    getUnreadCount: () => api.get('/v1/notifications/unread/count'),
    markRead: (id) => api.put(`/v1/notifications/${id}/read`),
    markAllRead: () => api.put('/v1/notifications/read-all'),
};

export default notificationRepository;
