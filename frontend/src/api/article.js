import request from './request'

export function getArticleList(category) {
  const params = category ? { category } : {}
  return request.get('/api/articles/list', { params })
}

export function getArticleDetail(id) {
  return request.get(`/api/articles/${id}/detail`)
}

export function getUserArticles(userId) {
  return request.get(`/api/articles/user/${userId}`)
}

export function createArticle(data) {
  return request.post('/api/articles/create', data)
}

export function updateArticle(id, data) {
  return request.put(`/api/articles/${id}/update`, data)
}

export function deleteArticle(id) {
  return request.delete(`/api/articles/${id}/delete`)
}
