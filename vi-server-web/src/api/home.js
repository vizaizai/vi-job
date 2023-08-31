import request from '@/utils/request'

export function baseCount() {
  return request({
    url: '/home/baseCount',
    method: 'get'
  })
}

export function listWaitingJobs() {
  return request({
    url: '/home/listWaitingJobs',
    method: 'get'
  })
}

export function clusters() {
  return request({
    url: '/home/clusters',
    method: 'get'
  })
}
