import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function getInfo(token) {
  return request({
    url: '/user/info',
    method: 'get',
    params: { token }
  })
}

export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}
export function addSysUser(data) {
  return request({
    url: '/user/addSysUser',
    method: 'post',
    data
  })
}

export function page(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params: params
  })
}

export function remove(data) {
  return request({
    url: '/user/remove',
    method: 'post',
    data
  })
}
