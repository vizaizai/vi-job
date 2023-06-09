import request from '@/utils/request'

export function page(params) {
  return request({
    url: '/worker/page',
    method: 'get',
    params: params
  })
}
export function nodes(workerId) {
  return request({
    url: '/worker/nodes',
    method: 'get',
    params: { workerId }
  })
}
export function saveOrUpdate(data) {
  return request({
    url: '/worker/saveOrUpdate',
    method: 'post',
    data
  })
}

export function remove(data) {
  return request({
    url: '/worker/remove',
    method: 'post',
    data
  })
}
