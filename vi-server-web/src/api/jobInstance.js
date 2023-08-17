import request from '@/utils/request'

export function page(params) {
  return request({
    url: '/jobInstance/page',
    method: 'get',
    params: params
  })
}

export function getLog(params) {
  return request({
    url: '/jobInstance/getLog',
    method: 'get',
    params: params
  })
}

export function cancel(data) {
  return request({
    url: '/jobInstance/cancel',
    method: 'post',
    data
  })
}

export function remove(data) {
  return request({
    url: '/jobInstance/remove',
    method: 'post',
    data
  })
}
