import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'

interface RegisterPageProps {
  onSuccess: () => void
}

function RegisterPage({ onSuccess }: RegisterPageProps) {
  const { register } = useAuth()
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await register({ firstName, lastName, email, password })
      onSuccess()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Création du compte impossible')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-card auth-card">
      <h2>Créer un compte</h2>
      <p>Accédez au plan et à l&apos;historique de vos réservations.</p>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="register-firstname">Prénom</label>
          <input
            id="register-firstname"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="register-lastname">Nom</label>
          <input
            id="register-lastname"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="register-email">Email</label>
          <input
            id="register-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="register-password">Mot de passe</label>
          <input
            id="register-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={6}
            required
          />
        </div>
        {error && <div className="form-error">{error}</div>}
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Création...' : 'Créer mon compte'}
        </button>
      </form>
      <p className="form-footer">
        Déjà inscrit ? <a href="#/login">Se connecter</a>
      </p>
    </div>
  )
}

export default RegisterPage
