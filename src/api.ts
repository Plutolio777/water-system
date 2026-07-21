import axios from 'axios'

export const api = axios.create({ baseURL: '/api', timeout: 12000 })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('water-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response.data.data,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
      localStorage.removeItem('water-token')
      localStorage.removeItem('water-user')
      window.location.href = '/login'
    }
    return Promise.reject(new Error(error.response?.data?.message || '网络连接失败'))
  },
)
