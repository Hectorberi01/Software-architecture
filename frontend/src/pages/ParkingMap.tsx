import { useEffect, useState } from 'react'

interface ParkingSpot {
  id: number
  code: string
  row: string
  number: number
  hasCharger: boolean
}

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F']

function ParkingMap() {
  const [spots, setSpots] = useState<ParkingSpot[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

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

  if (loading) return <div className="loading">Chargement des places...</div>
  if (error) return <div className="error">Erreur : {error}</div>

  return (
    <div>
      <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>
        Plan du parking ({spots.length} places)
      </h2>
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
                  className={`parking-spot${spot.hasCharger ? ' has-charger' : ''}`}
                >
                  <span className="spot-code">{spot.code}</span>
                  {spot.hasCharger && <span className="charger-icon">EV</span>}
                </div>
              ))}
          </div>
        </div>
      ))}
    </div>
  )
}

export default ParkingMap
