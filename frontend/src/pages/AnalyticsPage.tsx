import React, { useState, useEffect } from 'react';
import { analyticsApi } from '../api/analyticsApi';
import { AnalyticsData } from '../types/analytics';
import { Card } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const AnalyticsPage: React.FC = () => {
    const { showToast } = useToast();
    const [stats, setStats] = useState<AnalyticsData | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await analyticsApi.getDashboard();
                setStats(data);
            } catch (err: any) {
                showToast(err.message, 'error');
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, [showToast]);

    if (loading) return <div>Chargement des statistiques...</div>;
    if (!stats) return null;

    return (
        <div className="page-container">
            <div className="page-header">
                <h1>Statistiques (Métriques)</h1>
                <p>Tableau de bord de gestion du parking</p>
            </div>

            <div className="analytics-grid">
                <Card className="metric-card">
                    <h3>Taux d'occupation</h3>
                    <div className="metric-value">{stats.occupancyRate.toFixed(1)}%</div>
                    <div className="metric-subtitle">{stats.occupiedToday} / {stats.totalSpots} places occupées aujourd'hui</div>
                    <div className="progress-bar mt-3">
                        <div className="progress-fill bg-primary" style={{ width: `${stats.occupancyRate}%` }}></div>
                    </div>
                </Card>

                <Card className="metric-card">
                    <h3>Taux d'annulation (No-Show)</h3>
                    <div className="metric-value">{stats.noShowRate.toFixed(1)}%</div>
                    <div className="metric-subtitle">{stats.noShowCount} oublier(s) de check-in / {stats.totalReservations} résa</div>
                    <div className="progress-bar mt-3">
                        <div className="progress-fill bg-danger" style={{ width: `${stats.noShowRate}%` }}></div>
                    </div>
                </Card>

                <Card className="metric-card">
                    <h3>Usage Électrique</h3>
                    <div className="metric-value">{stats.electricUsageRate.toFixed(1)}%</div>
                    <div className="metric-subtitle">{stats.electricSpotsInUse} chargeurs en cours / {stats.totalElectricSpots} dispo</div>
                    <div className="progress-bar mt-3">
                        <div className="progress-fill bg-success" style={{ width: `${stats.electricUsageRate}%` }}></div>
                    </div>
                </Card>
            </div>
        </div>
    );
};
