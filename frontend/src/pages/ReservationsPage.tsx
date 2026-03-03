import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { parkingApi } from '../api/parkingApi';
import { checkInApi } from '../api/checkInApi';
import { Reservation, ReservationStatus } from '../types/parking';
import { Card, DataTable, Badge, Button, Column } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const ReservationsPage: React.FC = () => {
    const { user } = useAuth();
    const { showToast } = useToast();
    const [reservations, setReservations] = useState<Reservation[]>([]);
    const [loading, setLoading] = useState(true);

    const isManagerOrAdmin = user?.role === 'MANAGER' || user?.role === 'ADMIN';

    useEffect(() => {
        loadReservations();
    }, []);

    const loadReservations = async () => {
        setLoading(true);
        try {
            const data = isManagerOrAdmin
                ? await parkingApi.getAllReservations()
                : await parkingApi.getMyReservations();
            // tri desc
            data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
            setReservations(data);
        } catch (err: any) {
            showToast('Impossible de charger les réservations', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (id: number) => {
        if (!confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) return;
        try {
            await parkingApi.cancelReservation(id);
            showToast('Réservation annulée', 'success');
            loadReservations();
        } catch (err: any) {
            showToast(err.message || 'Erreur annulation', 'error');
        }
    };

    const handleCheckIn = async (spotId: number) => {
        try {
            const res = await checkInApi.checkInDashboard(spotId);
            showToast(res.message, 'success');
            loadReservations();
        } catch (err: any) {
            showToast(err.message || 'Check-in impossible', 'error');
        }
    };

    const handleApprove = async (id: number) => {
        try {
            await parkingApi.approveReservation(id);
            showToast('Réservation approuvée avec succès', 'success');
            loadReservations();
        } catch (err: any) {
            showToast(err.message || 'Erreur approbation', 'error');
        }
    };

    const handleReject = async (id: number) => {
        if (!confirm('Êtes-vous sûr de vouloir refuser cette réservation ?')) return;
        try {
            await parkingApi.rejectReservation(id);
            showToast('Réservation refusée avec succès', 'success');
            loadReservations();
        } catch (err: any) {
            showToast(err.message || 'Erreur refus', 'error');
        }
    };

    const getStatusBadge = (status: ReservationStatus) => {
        switch (status) {
            case 'PENDING': return <Badge variant="warning">ATTENTE APPROBATION</Badge>;
            case 'APPROVED': return <Badge variant="info">APPROUVÉ / EN ATTENTE CHECK-IN</Badge>;
            case 'CHECKED_IN': return <Badge variant="success">CHECK-IN OK</Badge>;
            case 'CANCELLED': return <Badge variant="danger">ANNULÉ</Badge>;
            case 'REJECTED': return <Badge variant="danger">REFUSÉ</Badge>;
            case 'NO_SHOW': return <Badge variant="warning">NO SHOW OMISSION</Badge>;
            default: return <Badge>{status}</Badge>;
        }
    };

    const canCheckIn = (res: Reservation) => {
        if (res.status !== 'APPROVED') return false;
        const todayStr = new Date().toISOString().split('T')[0];
        return res.startDate <= todayStr && res.endDate >= todayStr && res.userId === user?.id;
    };

    const columns: Column<Reservation>[] = [
        { header: 'ID', accessor: 'id' },
        ...(isManagerOrAdmin ? [{ header: 'Employé', accessor: (r: Reservation) => `${r.userFirstName} ${r.userLastName}` }] : []),
        { header: 'Place', accessor: 'spotCode' },
        { header: 'Début', accessor: (r: Reservation) => new Date(r.startDate).toLocaleDateString() },
        { header: 'Fin', accessor: (r: Reservation) => new Date(r.endDate).toLocaleDateString() },
        { header: 'Statut', accessor: (r: Reservation) => getStatusBadge(r.status) },
        {
            header: 'Actions',
            accessor: (r: Reservation) => (
                <div className="action-buttons">
                    {canCheckIn(r) && (
                        <Button variant="primary" onClick={() => handleCheckIn(r.spotId)} style={{ marginRight: 8, padding: '4px 8px', fontSize: 12 }}>
                            Check-in
                        </Button>
                    )}
                    {(r.status === 'PENDING' && isManagerOrAdmin) && (
                        <>
                            <Button variant="success" onClick={() => handleApprove(r.id)} style={{ marginRight: 8, padding: '4px 8px', fontSize: 12 }}>
                                Accepter
                            </Button>
                            <Button variant="danger" onClick={() => handleReject(r.id)} style={{ marginRight: 8, padding: '4px 8px', fontSize: 12 }}>
                                Refuser
                            </Button>
                        </>
                    )}
                    {((r.status === 'PENDING' || r.status === 'APPROVED') && (r.userId === user?.id || isManagerOrAdmin)) && (
                        <Button variant="danger" onClick={() => handleCancel(r.id)} style={{ padding: '4px 8px', fontSize: 12 }}>
                            Annuler
                        </Button>
                    )}
                </div>
            )
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <h1>{isManagerOrAdmin ? 'Toutes les réservations (Historique)' : 'Mes réservations'}</h1>
            </div>

            <Card>
                {loading ? (
                    <div>Chargement...</div>
                ) : (
                    <DataTable
                        data={reservations}
                        columns={columns}
                        keyExtractor={(r) => r.id}
                        emptyMessage="Aucune réservation trouvée."
                    />
                )}
            </Card>
        </div>
    );
};
