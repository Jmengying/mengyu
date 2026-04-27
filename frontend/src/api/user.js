import request from './request'

export function getUserInfo() {
  return request.get('/api/user/info')
}

export function getUserById(id) {
  return request.get(`/api/user/${id}`)
}

export function updateUserInfo(data) {
  return request.put('/api/user/update', data)
}

export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 600000,
    onUploadProgress: onProgress
  })
}
