import request from '@/utils/request'

export function page(params) {
  return request({
    url: '/job/page',
    method: 'get',
    params: params
  })
}

export function add(data) {
  return request({
    url: '/job/add',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/job/update',
    method: 'post',
    data
  })
}

export function remove(data) {
  return request({
    url: '/job/remove',
    method: 'post',
    data
  })
}

export function updateStatus(data) {
  return request({
    url: '/job/updateStatus',
    method: 'post',
    data
  })
}

export function run(data) {
  return request({
    url: '/job/run',
    method: 'post',
    data
  })
}
