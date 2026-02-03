import { useEffect } from 'react'
import ParkingMap from './pages/ParkingMap'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ReservationHistoryPage from './pages/ReservationHistoryPage'
import StatsPage from './pages/StatsPage'
import VehiclesPage from './pages/VehiclesPage'
import { useAuth } from './contexts/AuthContext'
import { useHashRoute } from './hooks/useHashRoute'

function App() {
  const { user, logout } = useAuth()
  const { path, navigate } = useHashRoute()
  const currentRoute = path
  const isAuthRoute = currentRoute === '/login' || currentRoute === '/register'

  useEffect(() => {
    if (!user && !isAuthRoute) {
      navigate('/login', true)
    } else if (user && isAuthRoute) {
      navigate('/', true)
    }
  }, [user, currentRoute, isAuthRoute, navigate])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const renderSecureContent = () => {
    switch (currentRoute) {
      case '/reservations':
        return <ReservationHistoryPage />
      case '/stats':
        return <StatsPage />
      case '/vehicles':
        return <VehiclesPage />
      default:
        return <ParkingMap />
    }
  }

  if (!user) {
    const authContent = currentRoute === '/register'
      ? <RegisterPage onSuccess={() => navigate('/')} />
      : <LoginPage onSuccess={() => navigate('/')} />

    return (
      <div className="auth-shell">
        <div className="auth-brand">ParkSys</div>
        <div className="auth-panel">{authContent}</div>
      </div>
    )
  }

  return (
    <div className="dashboard-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-icon">P</div>
          <div>
            <p>ParkSys</p>
            <small>En direct</small>
          </div>
        </div>
        <nav className="sidebar-nav">
          <a className={currentRoute === '/' ? 'active' : ''} href="#/">Plan du Parking</a>
          <a className={currentRoute === '/reservations' ? 'active' : ''} href="#/reservations">
            Mes Réservations
            <span className="badge">2</span>
          </a>
          <a className={currentRoute === '/stats' ? 'active' : ''} href="#/stats">Statistiques</a>
          <a className={currentRoute === '/vehicles' ? 'active' : ''} href="#/vehicles">Mes Véhicules</a>
        </nav>
        <div className="sidebar-footer">
          <div>
            <strong>{user.firstName}</strong>
            <small>Employé</small>
          </div>
          <button className="link" onClick={handleLogout}>Déconnexion</button>
        </div>
      </aside>
      <div className="main-panel">
        <header className="panel-header">
          <div>
            <h1>Réserver une place</h1>
            <span className="live-pill">En direct</span>
          </div>
          <div className="panel-actions">
            <input placeholder="Rechercher place (ex: A04)..." />
            <button className="ghost-btn" aria-label="Notifications">🔔</button>
          </div>
        </header>
        <section className="panel-content">
          {renderSecureContent()}
        </section>
      </div>
    </div>
  )
}

export default App
