/**
 * 地图逆地理（后端代理腾讯位置服务）
 */
import { get } from '@/utils/request'

export interface ReverseGeocodeResult {
  address: string
  title?: string | null
  latitude: number
  longitude: number
}

export function reverseGeocode(lat: number, lng: number) {
  return get<ReverseGeocodeResult>('/map/reverse-geocode', { lat, lng })
}
