import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'
import type { User } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('water-token') || '')
  const saved = localStorage.getItem('water-user')
  const user = ref<User | null>(saved ? JSON.parse(saved) : null)

  async function login(username: string, password: string) {
    const result = await api.post<unknown, { token: string; user: User }>('/auth/login', { username, password })
    token.value = result.token
    user.value = result.user
    localStorage.setItem('water-token', result.token)
    localStorage.setItem('water-user', JSON.stringify(result.user))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('water-token')
    localStorage.removeItem('water-user')
  }

  return { token, user, login, logout }
})
