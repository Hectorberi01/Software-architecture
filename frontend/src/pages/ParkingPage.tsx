import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { parkingApi } from '../api/parkingApi';
import { ParkingSpot } from '../types/parking';
import { ParkingMap } from './ParkingMap';
import { Button, Card, Badge, Modal } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const ParkingPage: React.FC = () => {
    const { user } = useAuth();
    const { showToast } = useToast();
    const [availableSpots, setAvailableSpots] = useState<ParkingSpot[]>([]);
    const [loading, setLoading] = useState(false);

    // Filtres
    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [needsCharger, setNeedsCharger] = useState(false);

    // Modal
    const [selectedSpot, setSelectedSpot] = useState<ParkingSpot | null>(null);
    const [isBooking, setIsBooking] = useState(false);

    useEffect(() => {
        loadAvailableSpots();
    }, [startDate, endDate, needsCharger]);

    const loadAvailableSpots = async () => {
        setLoading(true);
        try {
            const spots = await parkingApi.getAvailableSpots(startDate, endDate, needsCharger ? true : undefined);
            setAvailableSpots(spots);
        } catch (err: any) {
            showToast(err.message || 'Erreur lors de la récupération des places', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleSpotSelect = (spot: ParkingSpot) => {
        setSelectedSpot(spot);
    };

    const handleBooking = async () => {
        if (!selectedSpot) return;
        setIsBooking(true);
        try {
            await parkingApi.createReservation({
                spotId: selectedSpot.id,
                startDate,
                endDate
            });
            showToast(`Réservation de la place ${selectedSpot.code} confirmée !`, 'success');
            setSelectedSpot(null);
            loadAvailableSpots(); // Recharger les disponibilités
        } catch (err: any) {
            showToast(err.message || 'Erreur lors de la réservation', 'error');
        } finally {
            setIsBooking(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h1>Réserver une place</h1>
                <p>Sélectionnez vos dates pour voir les disponibilités</p>
            </div>

            <Card className="filter-card mb-4">
                <div className="filters-grid">
                    <div className="form-group">
                        <label>Date de début</label>
                        <input
                            type="date"
                            className="form-control"
                            value={startDate}
                            min={new Date().toISOString().split('T')[0]}
                            onChange={e => setStartDate(e.target.value)}
                        />
                    </div>
                    <div className="form-group">
                        <label>Date de fin</label>
                        <input
                            type="date"
                            className="form-control"
                            value={endDate}
                            min={startDate}
                            onChange={e => setEndDate(e.target.value)}
                        />
                    </div>
                    <div className="form-group flex-center-vertical pt-4">
                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={needsCharger}
                                onChange={e => setNeedsCharger(e.target.checked)}
                            />
                            J'ai besoin d'une prise électrique
                        </label>
                    </div>
                    <div className="form-group flex-center-vertical pt-4">
                        <Button onClick={loadAvailableSpots} disabled={loading}>
                            {loading ? 'Recherche...' : 'Rechercher'}
                        </Button>
                    </div>
                </div>
            </Card>

            <Card>
                {loading ? (
                    <div className="loading-state">Recherche des places disponibles...</div>
                ) : (
                    <ParkingMap
                        spots={availableSpots}
                        onSpotSelect={handleSpotSelect}
                        selectedSpotId={selectedSpot?.id}
                    />
                )}
            </Card>

            <Modal
                isOpen={!!selectedSpot}
                onClose={() => setSelectedSpot(null)}
                title={`Confirmer la réservation - ${selectedSpot?.code}`}
                actions={
                    <>
                        <Button variant="ghost" onClick={() => setSelectedSpot(null)}>Annuler</Button>
                        <Button onClick={handleBooking} disabled={isBooking}>
                            {isBooking ? 'Confirmation...' : 'Confirmer'}
                        </Button>
                    </>
                }
            >
                {selectedSpot && (
                    <div className="booking-summary">
                        <p><strong>Place :</strong> <Badge variant="info">{selectedSpot.code}</Badge></p>
                        <p><strong>Equipement :</strong> {selectedSpot.hasCharger ? 'Prise électrique incluse' : 'Standard'}</p>
                        <p><strong>Période :</strong> du {new Date(startDate).toLocaleDateString()} au {new Date(endDate).toLocaleDateString()}</p>

                        <div className="alert alert-warning mt-4">
                            <strong>Rappels importants :</strong>
                            <ul className="mt-2">
                                <li>Vous devez faire un "Check-in" avant 11h00 chaque jour sur votre place.</li>
                                <li>En cas d'oubli, la réservation de la journée est annulée et la place est libérée.</li>
                                {user?.role === 'EMPLOYEE' && <li>Maximum autorisé : 5 jours ouvrés.</li>}
                            </ul>
                        </div>
                    </div>
                )}
            </Modal>
        </div>
    );
};
