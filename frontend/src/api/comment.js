import request from './request'

export function getComments(articleId) {
  return request.get(`/api/comments/article/${articleId}`)
}

export function createComment(data) {
  return request.post('/api/comments/create', data)
}

export function deleteComment(id) {
  return request.delete(`/api/comments/${id}/delete`)
}
