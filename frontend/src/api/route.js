import axios from 'axios'

export function planRoute(start, end) {
  return axios.get('/api/route', {
    params: {
      slon: start[0],
      slat: start[1],
      elon: end[0],
      elat: end[1]
    }
  })
}
