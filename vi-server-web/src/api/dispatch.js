import request from '@/utils/request'

export function page(params) {
  return request({
    url: '/dispatch/page',
    method: 'get',
    params: params
  })
}

export function getLog(params) {
  return request({
    url: '/dispatch/getLog',
    method: 'get',
    params: params
  })
}

export function cancel(data) {
  return request({
    url: '/dispatch/cancel',
    method: 'post',
    data
  })
}

export function remove(data) {
  return request({
    url: '/dispatch/remove',
    method: 'post',
    data
  })
}
