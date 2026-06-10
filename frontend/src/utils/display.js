export function assetUrl(path) {
  if (!path || /^(https?:|blob:|data:)/.test(path)) return path || ''
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  return `${new URL(apiBase, window.location.origin).origin}${path.startsWith('/') ? path : `/${path}`}`
}

export function displayName(user) {
  return user?.remark || user?.nickname || user?.username || '未知用户'
}

export function displayInitial(user) {
  return String(displayName(user)).slice(0, 1)
}
