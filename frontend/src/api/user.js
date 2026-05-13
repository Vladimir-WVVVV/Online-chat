import http from './http'

export function getProfile() {
  return http.get('/users/me')
}

export function updateProfile(payload) {
  return http.put('/users/me', payload)
}

