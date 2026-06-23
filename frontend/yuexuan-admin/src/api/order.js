import api from './index'

export const getOrders  = (params) => api.get('/platform/orders', { params })
export const shipOrder  = (id) => api.post(`/m/orders/${id}/delivery`)
