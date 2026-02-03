import { useState } from 'react'
import { ParkingSpot, CreateReservationRequest } from '../types/types'

interface ReservationModalProps {
  spot: ParkingSpot
  selectedDate: string
  onClose: () => void
  onReserve: (request: CreateReservationRequest) => Promise<void>
}

function isWeekend(dateString: string): boolean {
  const date = new Date(dateString)
  const day = date.getDay()
  return day === 0 || day === 6
}

function ReservationModal({ spot, selectedDate, onClose, onReserve }: ReservationModalProps) {
  const [email, setEmail] = useState('')
  const [date, setDate] = useState(selectedDate)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const today = new Date().toISOString().split('T')[0]
  const isDateWeekend = isWeekend(date)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (isDateWeekend) {
      setError('Les reservations ne sont pas autorisees le weekend')
      return
    }

    setLoading(true)
    setError(null)

    try {
      await onReserve({
        parkingSpotId: spot.id,
        userEmail: email,
        reservationDate: date,
      })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Reservation failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Reserver la place {spot.code}</h3>
        {spot.hasCharger && <p className="charger-note">Cette place dispose d'une borne de recharge</p>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="votre@email.com"
            />
          </div>

          <div className="form-group">
            <label htmlFor="date">Date (Lun-Ven)</label>
            <input
              type="date"
              id="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              min={today}
              required
            />
            {isDateWeekend && (
              <span className="weekend-warning">Weekend non autorise</span>
            )}
          </div>

          {error && <div className="modal-error">{error}</div>}

          <div className="modal-actions">
            <button type="button" onClick={onClose} disabled={loading}>
              Annuler
            </button>
            <button type="submit" className="btn-primary" disabled={loading || isDateWeekend}>
              {loading ? 'Reservation...' : 'Reserver'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ReservationModal
