import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import type { LoginRequest, RegisterRequest, User } from '../types/types'

interface AuthContextValue {
  user: User | null
  initializing: boolean
  login: (credentials: LoginRequest) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  logout: () => void
}

const STORAGE_KEY = 'parking-user'

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredUser(): User | null {
  if (typeof window === 'undefined') return null
  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as User
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => readStoredUser())
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    setInitializing(false)
  }, [])

  const persistUser = (data: User | null) => {
    setUser(data)
    if (typeof window === 'undefined') {
      return
    }
    if (data) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    } else {
      window.localStorage.removeItem(STORAGE_KEY)
    }
  }

  const handleResponse = async (res: Response, fallback: string) => {
    if (res.ok) {
      const payload: User = await res.json()
      persistUser(payload)
    } else {
      const text = await res.text()
      throw new Error(text || fallback)
    }
  }

  const login = async (credentials: LoginRequest) => {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    })
    await handleResponse(res, 'Connexion impossible')
  }

  const register = async (payload: RegisterRequest) => {
    const res = await fetch('/api/v1/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    await handleResponse(res, 'Création du compte impossible')
  }

  const logout = () => persistUser(null)

  return (
    <AuthContext.Provider value={{ user, initializing, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
