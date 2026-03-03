import { AnalyticsData } from '../types/analytics';

export const analyticsApi = {
    getDashboard: async (): Promise<AnalyticsData> => {
        const token = localStorage.getItem('token');
        const res = await fetch('/api/analytics/dashboard', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error('Impossible de charger les statistiques');
        return res.json();
    }
};
