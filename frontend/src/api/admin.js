import request from './request'

export function getAdminUsers() {
  return request.get('/api/admin/users')
}

export function adminDeleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}

export function getAdminArticles() {
  return request.get('/api/admin/articles')
}

export function adminDeleteArticle(id) {
  return request.delete(`/api/admin/articles/${id}`)
}
