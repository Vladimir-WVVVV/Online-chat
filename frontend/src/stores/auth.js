import { defineStore } from 'pinia'
import { getMe, login, logout, register } from '../api/auth'

const TOKEN_KEY = 'onlinechat_token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem('onlinechat_user') || 'null'),
    loaded: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.user)
  },
  actions: {
    persist() {
      if (this.token) localStorage.setItem(TOKEN_KEY, this.token)
      else localStorage.removeItem(TOKEN_KEY)
      if (this.user) localStorage.setItem('onlinechat_user', JSON.stringify(this.user))
      else localStorage.removeItem('onlinechat_user')
    },
    async login(form) {
      const data = await login(form)
      this.token = data.token
      this.user = data.user
      this.loaded = true
      this.persist()
    },
    async register(form) {
      await register(form)
    },
    async fetchMe() {
      if (!this.token) {
        this.loaded = true
        return
      }
      try {
        this.user = await getMe()
      } catch {
        this.token = ''
        this.user = null
      } finally {
        this.loaded = true
        this.persist()
      }
    },
    async logout() {
      try {
        if (this.token) await logout()
      } finally {
        this.token = ''
        this.user = null
        this.loaded = true
        this.persist()
      }
    }
  }
})

