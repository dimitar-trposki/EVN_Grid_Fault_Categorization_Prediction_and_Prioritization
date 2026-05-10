import api from './axiosConfig';

const notificationRepository = {
    getMyNotifications: () =>
        api.get('/notifications/my'),

    markAsRead: (id) =>
        api.put(`/notifications/${id}/read`),

    markAllAsRead: () =>
        api.put('/notifications/read-all'),

    deleteNotification: (id) =>
        api.delete(`/notifications/${id}`),
};

export default notificationRepository;