import http from './http'

export const reportingService = {
  synthese: () => http.get('/reporting/synthese').then((r) => r.data),
}
