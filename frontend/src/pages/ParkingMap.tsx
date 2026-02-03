import { useEffect, useState } from 'react'
import { ParkingSpot, CreateReservationRequest } from '../types/types'
import DateSelector from '../components/DateSelector'
import ReservationModal from '../components/ReservationModal'

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F']

function ParkingMap() {
  const [spots, setSpots] = useState<ParkingSpot[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  )
  const [reservedSpotIds, setReservedSpotIds] = useState<Set<number>>(new Set())
  const [selectedSpot, setSelectedSpot] = useState<ParkingSpot | null>(null)

  useEffect(() => {
    fetch('/api/v1/spots')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data: ParkingSpot[]) => {
        setSpots(data)
        setLoading(false)
      })
      .catch((err) => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  useEffect(() => {
    fetch(`/api/v1/reservations/reserved-spots?date=${selectedDate}`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((ids: number[]) => setReservedSpotIds(new Set(ids)))
      .catch(console.error)
  }, [selectedDate])

  const handleReserve = async (request: CreateReservationRequest) => {
    const res = await fetch('/api/v1/reservations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
    if (!res.ok) {
      const errorText = await res.text()
      throw new Error(errorText || 'Reservation failed')
    }
    // Mettre à jour la date affichée vers la date réservée et rafraîchir
    setSelectedDate(request.reservationDate)
    const idsRes = await fetch(`/api/v1/reservations/reserved-spots?date=${request.reservationDate}`)
    const ids = await idsRes.json()
    setReservedSpotIds(new Set(ids))
  }

  const handleSpotClick = (spot: ParkingSpot) => {
    if (!reservedSpotIds.has(spot.id)) {
      setSelectedSpot(spot)
    }
  }

  if (loading) return <div className="loading">Chargement des places...</div>
  if (error) return <div className="error">Erreur : {error}</div>

  return (
    <div>
      <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>
        Plan du parking ({spots.length} places)
      </h2>
      <DateSelector selectedDate={selectedDate} onChange={setSelectedDate} />
      {ROWS.map((row) => (
        <div key={row} className="row-container">
          <div className="row-label">{row}</div>
          <div className="parking-grid">
            {spots
              .filter((s) => s.row === row)
              .sort((a, b) => a.number - b.number)
              .map((spot) => (
                <div
                  key={spot.id}
                  className={`parking-spot${spot.hasCharger ? ' has-charger' : ''}${
                    reservedSpotIds.has(spot.id) ? ' reserved' : ''
                  }`}
                  onClick={() => handleSpotClick(spot)}
                >
                  <span className="spot-code">{spot.code}</span>
                  {spot.hasCharger && <span className="charger-icon">EV</span>}
                </div>
              ))}
          </div>
        </div>
      ))}
      {selectedSpot && (
        <ReservationModal
          spot={selectedSpot}
          selectedDate={selectedDate}
          onClose={() => setSelectedSpot(null)}
          onReserve={handleReserve}
        />
      )}
    </div>
  )
}

export default ParkingMap
