import { useEffect, useMemo, useRef, useState } from 'react'
import { ParkingSpot, CreateReservationRequest } from '../types/types'
import ReservationModal from '../components/ReservationModal'

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F']
const UPCOMING_DAYS = 5

const toISODate = (date: Date) => date.toISOString().split('T')[0]

function ParkingMap() {
  const [spots, setSpots] = useState<ParkingSpot[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  )
  const [reservedSpotIds, setReservedSpotIds] = useState<Set<number>>(new Set())
  const [selectedSpot, setSelectedSpot] = useState<ParkingSpot | null>(null)
  const [showElectricOnly, setShowElectricOnly] = useState(false)
  const [zoneFilter, setZoneFilter] = useState<'all' | 'A-C' | 'D-F'>('all')
  const [zoomLevel, setZoomLevel] = useState(5)
  const calendarInputRef = useRef<HTMLInputElement>(null)

  const upcomingDays = useMemo(() => {
    const today = new Date()
    return Array.from({ length: UPCOMING_DAYS }, (_, idx) => {
      const d = new Date(today)
      d.setDate(today.getDate() + idx)
      return {
        label: d.toLocaleDateString('fr-FR', { weekday: 'short' }).toUpperCase(),
        day: d.getDate().toString().padStart(2, '0'),
        value: toISODate(d),
      }
    })
  }, [])

  const zoneRows = useMemo(() => {
    if (zoneFilter === 'A-C') return new Set(['A', 'B', 'C'])
    if (zoneFilter === 'D-F') return new Set(['D', 'E', 'F'])
    return null
  }, [zoneFilter])

  const filteredSpots = useMemo(
    () =>
      spots.filter(
        (spot) =>
          (!zoneRows || zoneRows.has(spot.row)) &&
          (!showElectricOnly || spot.hasCharger),
      ),
    [spots, zoneRows, showElectricOnly],
  )

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
      const contentType = res.headers.get('content-type') ?? ''
      let message: string | null = null

      if (contentType.includes('application/json')) {
        try {
          const body = await res.json()
          if (typeof body?.message === 'string') {
            message = body.message
          }
        } catch {
          // ignore JSON parse errors
        }
      }

      if (!message) {
        const errorText = (await res.text()).trim()
        if (errorText) {
          message = errorText
        }
      }

      if (!message && res.status === 409) {
        message = 'Cette place est déjà réservée pour cette date'
      }

      throw new Error(message ?? 'Reservation failed')
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

  const totalSpots = filteredSpots.length
  const reservedCount = filteredSpots.filter((s) => reservedSpotIds.has(s.id)).length
  const availableCount = Math.max(totalSpots - reservedCount, 0)
  const totalEvSpots = filteredSpots.filter((s) => s.hasCharger).length
  const reservedEv = filteredSpots.filter((s) => s.hasCharger && reservedSpotIds.has(s.id)).length
  const availableEv = Math.max(totalEvSpots - reservedEv, 0)

  const visibleRows = useMemo(
    () => ROWS.filter((row) => !zoneRows || zoneRows.has(row)),
    [zoneRows],
  )

  const spotWidth = 70 + zoomLevel * 3

  const openCalendar = () => {
    const input = calendarInputRef.current
    if (!input) return
    const anyInput = input as HTMLInputElement & { showPicker?: () => void }
    if (typeof anyInput.showPicker === 'function') {
      anyInput.showPicker()
    } else {
      input.focus()
      input.click()
    }
  }

  if (loading) return <div className="loading">Chargement des places...</div>
  if (error) return <div className="error">Erreur : {error}</div>

  return (
    <div className="dashboard-card">
      <div className="panel-toolbar">
        <div className="date-tabs">
          {upcomingDays.map((day) => (
            <button
              type="button"
              key={day.value}
              className={day.value === selectedDate ? 'active' : ''}
              onClick={() => setSelectedDate(day.value)}
            >
              <span>{day.label}</span>
              <strong>{day.day}</strong>
            </button>
          ))}
          <button type="button" className="calendar-trigger" onClick={openCalendar}>
            <input
              ref={calendarInputRef}
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
            />
            Calendrier
          </button>
        </div>
        <div className="availability-summary">
          <div>
            <span>Disponibles</span>
            <strong>
              {availableCount} / {totalSpots}
            </strong>
          </div>
          <div>
            <span>Bornes EV</span>
            <strong>{availableEv} libres</strong>
          </div>
        </div>
      </div>

      <div className="filter-bar">
        <span>Filtres :</span>
        <button
          type="button"
          className={`chip${showElectricOnly ? ' active' : ''}`}
          onClick={() => setShowElectricOnly((prev) => !prev)}
          aria-pressed={showElectricOnly}
        >
          ⚡ Électrique
        </button>
        <button
          type="button"
          className={`chip${zoneFilter === 'A-C' ? ' active' : ''}`}
          onClick={() => setZoneFilter(zoneFilter === 'A-C' ? 'all' : 'A-C')}
        >
          Zone A-C
        </button>
        <button
          type="button"
          className={`chip${zoneFilter === 'D-F' ? ' active' : ''}`}
          onClick={() => setZoneFilter(zoneFilter === 'D-F' ? 'all' : 'D-F')}
        >
          Zone D-F
        </button>
        <div className="zoom-control">
          <input
            type="range"
            min="0"
            max="10"
            value={zoomLevel}
            onChange={(e) => setZoomLevel(Number(e.target.value))}
          />
        </div>
        <div className="legend">
          <span className="dot free" /> Libre
          <span className="dot busy" /> Occupé
          <span className="dot mine" /> Ma résa.
          <span className="dot ev" /> EV
        </div>
      </div>

      {visibleRows.map((row) => {
        const rowSpots = spots
          .filter((s) => s.row === row)
          .filter((s) => (!showElectricOnly || s.hasCharger))
        if (rowSpots.length === 0) return null
        return (
        <div key={row} className="row-container">
          <div className="row-label">{row}</div>
          <div className="parking-grid" style={{ gridTemplateColumns: `repeat(auto-fill, minmax(${spotWidth}px, 1fr))` }}>
            {rowSpots
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
        )
      })}
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
