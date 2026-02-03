import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import type { Reservation, ReservationHistory } from '../types/types'

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleDateString('fr-FR', {
    weekday: 'long',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

function formatDateTime(dateString: string) {
  return new Date(dateString).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function ReservationHistoryPage() {
  const { user } = useAuth()
  const [history, setHistory] = useState<ReservationHistory | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadHistory = useCallback(async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`/api/v1/reservations/user/history?userId=${user.id}`)
      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || 'Impossible de charger votre historique')
      }
      const data: ReservationHistory = await res.json()
      setHistory(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur inconnue')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    loadHistory()
  }, [loadHistory])

  if (!user) {
    return null
  }

  const renderReservations = (reservations: Reservation[], emptyMessage: string) => {
    if (!reservations.length) {
      return <p className="history-empty">{emptyMessage}</p>
    }
    return (
      <ul className="history-list">
        {reservations.map((reservation) => (
          <li key={reservation.id} className="history-item">
            <div>
              <strong>Place {reservation.spotCode}</strong>
              <p>Date : {formatDate(reservation.reservationDate)}</p>
            </div>
            <small>Réservé le {formatDateTime(reservation.createdAt)}</small>
          </li>
        ))}
      </ul>
    )
  }

  return (
    <div className="page-card">
      <div className="page-card-header">
        <div>
          <h2>Mes réservations</h2>
          <p>Historique pour {user.email}</p>
        </div>
        <button onClick={loadHistory} disabled={loading}>
          Rafraîchir
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      {loading && <div className="loading">Chargement de vos réservations...</div>}

      {!loading && history && (
        <div className="history-sections">
          <section>
            <h3>En cours</h3>
            {renderReservations(history.activeReservations, 'Aucune réservation future.')}
          </section>
          <section>
            <h3>Passées</h3>
            {renderReservations(history.pastReservations, 'Pas encore de réservation passée.')}
          </section>
        </div>
      )}
    </div>
  )
}

export default ReservationHistoryPage
