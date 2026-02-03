import { useCallback, useEffect, useState } from 'react'

function normalize(path: string | null | undefined): string {
  if (!path || path.length === 0) {
    return '/'
  }
  const trimmed = path.startsWith('#') ? path.slice(1) : path
  if (!trimmed) return '/'
  return trimmed.startsWith('/') ? trimmed : `/${trimmed}`
}

export function useHashRoute() {
  const [path, setPath] = useState(() => normalize(window.location.hash))

  useEffect(() => {
    const handleChange = () => setPath(normalize(window.location.hash))
    window.addEventListener('hashchange', handleChange)
    return () => window.removeEventListener('hashchange', handleChange)
  }, [])

  const navigate = useCallback((target: string, replace = false) => {
    const next = normalize(target)
    if (replace) {
      const url = `${window.location.pathname}${window.location.search}#${next}`
      window.history.replaceState(null, '', url)
      setPath(next)
    } else {
      window.location.hash = next
    }
  }, [])

  return { path, navigate }
}
