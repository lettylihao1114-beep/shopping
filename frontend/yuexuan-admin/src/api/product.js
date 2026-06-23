import api from './index'

export const getProducts    = (params) => api.get('/admin/products', { params })
export const getProduct     = (pid) => api.get(`/admin/products/${pid}`)
export const createProduct  = (data) => api.post('/admin/products', data)
export const updateProduct  = (pid, data) => api.put(`/admin/products/${pid}`, data)
export const deleteProduct  = (pid) => api.delete(`/admin/products/${pid}`)
