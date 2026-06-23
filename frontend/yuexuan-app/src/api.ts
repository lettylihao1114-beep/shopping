import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Day08: 自动附加 JWT Bearer Token（优先），回退到 token=123 查询参数
api.interceptors.request.use(config => {
  const jwt = sessionStorage.getItem('yuexuan_token')
  if (jwt) {
    config.headers.Authorization = `Bearer ${jwt}`
  } else {
    // 回退：旧 token 参数方式
    config.params = { ...config.params, token: '123' }
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      sessionStorage.removeItem('yuexuan_token')
      if (window.location.hash !== '#/login') window.location.hash = '#/login'
    }
    console.error('API 错误:', err.message)
    return Promise.reject(err)
  }
)

export interface Product {
  pid: number; name: string; price: number; image: string
  category: string; description: string; stock: number
  rating: number; reviewCount: number
}

export interface Order {
  id: number; orderNo: string; pid: number; productName: string; productPrice: number
  uid: number; username: string; number: number; totalAmount: number
  status: string; receiverName: string; receiverPhone: string; address: string
  handledByPort: string; createTime: string; payTime: string
}

export interface OrderSubmitItem { pid: number; qty: number }

export interface OrderSubmitRequest {
  confirmToken: string       // Day08 防重令牌（从 confirm 获取）
  items: OrderSubmitItem[]
  receiverName: string
  receiverPhone: string
  address: string
}

export interface ConfirmVO {
  items: ConfirmItem[]
  totalAmount: number
  deliveryFee: number
  payAmount: number
  confirmToken: string
}

export interface ConfirmItem {
  pid: number; name: string; price: number; qty: number
  subtotal: number; stock: number; inStock: boolean
}

export interface PayInfo {
  payId: number; orderId: number; orderNo: string
  userId: number; amount: number; status: string
  createTime: string; payTime: string
}

const unwrap = <T>(res: any) => res.data.data

// ==================== 商品 ====================
export const getProducts = (category?: string, keyword?: string) =>
  api.get('/admin/products', { params: { category, keyword } }).then(unwrap)

export const getProductById = (pid: number) =>
  api.get<Product>(`/admin/products/${pid}`).then(res => res.data)

// ==================== 订单 ====================
export const getOrders = (status?: string) =>
  api.get('/a/orders', { params: status ? { status } : {} }).then(unwrap)

/** Day08 订单确认（第一步：获取预览 + 防重token） */
export const confirmOrder = (req: { items: OrderSubmitItem[], receiverName: string, receiverPhone: string, address: string }) =>
  api.post<{ code: number; data: ConfirmVO }>('/a/orders/confirm', req).then(unwrap)

/** Day08 提交订单（第二步：带 confirmToken） */
export const submitOrder = (req: OrderSubmitRequest) =>
  api.post('/a/orders/submit', req).then(unwrap)

/** Day08 支付（通过支付微服务）*/
export const payByService = (orderId: number) =>
  api.post('/a/payment/pay', { orderId }).then(unwrap)

/** 取消订单 */
export const cancelOrder = (id: number) =>
  api.put(`/a/orders/${id}/cancel`).then(unwrap)

/** Day08 发货 */
export const shipOrder = (id: number) =>
  api.put(`/a/orders/${id}/ship`).then(unwrap)

/** Day08 确认收货 */
export const receiveOrder = (id: number) =>
  api.put(`/a/orders/${id}/receive`).then(unwrap)

// ==================== 鉴权 ====================
export const login = (username: string, password: string) =>
  api.post('/a/login', { username, password }).then(unwrap)

export const register = (username: string, password: string, phone: string) =>
  api.post('/a/register', { username, password, phone }).then(unwrap)

// ==================== 兼容旧接口 ====================
/** @deprecated 使用 confirmOrder + submitOrder 两步式替代 */
export const createOrder = (pid: number, uid: number = 1) =>
  api.get('/a/orders/save', { params: { pid, uid } }).then(unwrap)
