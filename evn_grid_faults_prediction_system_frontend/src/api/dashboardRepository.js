import api from './axiosConfig';

const dashboardRepository = {
    getKpis: () =>
        api.get('/v1/dashboard/kpis'),

    getActiveFaults: () =>
        api.get('/v1/dashboard/map/faults'),
};

export default dashboardRepository;