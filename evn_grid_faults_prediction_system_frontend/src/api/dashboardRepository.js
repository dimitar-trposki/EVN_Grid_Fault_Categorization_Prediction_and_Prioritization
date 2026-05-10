import api from './axiosConfig';

const dashboardRepository = {
    getKpis: () =>
        api.get('/v1/dashboard/kpis'),

    getActiveFaults: () =>
        api.get('/v1/dashboard/map/faults'),

    getFaultsByRegion: () =>
        api.get('/v1/dashboard/faults-by-region'),

    getFaultsByType: () =>
        api.get('/v1/dashboard/faults-by-type'),

    getFaultsByPeriod: (groupBy = 'day') =>
        api.get(`/v1/dashboard/faults-by-period?groupBy=${groupBy}`),

    getCrewPerformance: () =>
        api.get('/v1/dashboard/crew-performance'),
};

export default dashboardRepository;