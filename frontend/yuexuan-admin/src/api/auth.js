import api from './index'
export const login = (username, password) =>
  api.post('/a/login', { username, password })
