import service from '../utils/request'

export const getStatistics = () => {
  return service.get('/report/statistics')
}
