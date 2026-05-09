import api from './axiosConfig';

const dashboardRepository = {
    getStats: () =>
        api.get('/dashboard/stats'),

    getActiveFaults: () =>
        api.get('/faults?status=ACTIVE'),

    getRecentInterventions: () =>
        api.get('/interventions/recent'),

    getRiskPredictions: () =>
        api.get('/risk-predictions/active'),

    getCrewAvailability: () =>
        api.get('/crews/availability'),
};

export default dashboardRepository;