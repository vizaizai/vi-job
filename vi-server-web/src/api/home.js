import request from '@/utils/request'

export function baseCount() {
  return request({
    url: '/home/baseCount',
    method: 'get'
  })
}
