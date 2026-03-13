/**
 * 天气API服务
 * 使用和风天气API（免费版）获取实时天气数据
 * 如需使用，请前往 https://dev.qweather.com/ 注册获取API Key
 */

import request from '@/utils/request'

// 和风天气API配置
// 请替换为您自己的API Key
const QWEATHER_API_KEY = 'YOUR_API_KEY_HERE'
const QWEATHER_API_BASE = 'https://devapi.qweather.com/v7'

// 默认城市（可根据需求修改或通过IP定位获取）
const DEFAULT_CITY = '101010100' // 北京

/**
 * 天气图标映射（和风天气图标码 -> 描述）
 */
const WEATHER_ICONS = {
  '100': { icon: 'sunny', desc: '晴' },
  '101': { icon: 'cloudy', desc: '多云' },
  '102': { icon: 'few-clouds', desc: '少云' },
  '103': { icon: 'partly-cloudy', desc: '晴间多云' },
  '104': { icon: 'overcast', desc: '阴' },
  '150': { icon: 'sunny-night', desc: '晴' },
  '151': { icon: 'cloudy-night', desc: '多云' },
  '300': { icon: 'shower', desc: '阵雨' },
  '301': { icon: 'heavy-shower', desc: '强阵雨' },
  '302': { icon: 'thundershower', desc: '雷阵雨' },
  '303': { icon: 'heavy-thundershower', desc: '强雷阵雨' },
  '304': { icon: 'thundershower-hail', desc: '雷阵雨伴有冰雹' },
  '305': { icon: 'light-rain', desc: '小雨' },
  '306': { icon: 'moderate-rain', desc: '中雨' },
  '307': { icon: 'heavy-rain', desc: '大雨' },
  '308': { icon: 'extreme-rain', desc: '极端降雨' },
  '309': { icon: 'drizzle', desc: '毛毛雨' },
  '310': { icon: 'storm', desc: '暴雨' },
  '311': { icon: 'heavy-storm', desc: '大暴雨' },
  '312': { icon: 'severe-storm', desc: '特大暴雨' },
  '313': { icon: 'freezing-rain', desc: '冻雨' },
  '314': { icon: 'light-rain-snow', desc: '小到中雨' },
  '315': { icon: 'moderate-rain-heavy', desc: '中到大雨' },
  '400': { icon: 'light-snow', desc: '小雪' },
  '401': { icon: 'moderate-snow', desc: '中雪' },
  '402': { icon: 'heavy-snow', desc: '大雪' },
  '403': { icon: 'snowstorm', desc: '暴雪' },
  '404': { icon: 'sleet', desc: '雨夹雪' },
  '405': { icon: 'rain-snow', desc: '雨雪天气' },
  '500': { icon: 'mist', desc: '薄雾' },
  '501': { icon: 'fog', desc: '雾' },
  '502': { icon: 'haze', desc: '霾' },
  '503': { icon: 'sand', desc: '扬沙' },
  '504': { icon: 'dust', desc: '浮尘' },
  '507': { icon: 'sandstorm', desc: '沙尘暴' },
  '508': { icon: 'severe-sandstorm', desc: '强沙尘暴' },
  '900': { icon: 'hot', desc: '热' },
  '901': { icon: 'cold', desc: '冷' },
  '999': { icon: 'unknown', desc: '未知' }
}

/**
 * 运动建议映射
 */
function getSportAdvice(temp, weather) {
  const tempNum = parseInt(temp)

  if (weather.includes('雨') || weather.includes('雪') || weather.includes('暴')) {
    return '不宜户外运动'
  }
  if (weather.includes('霾') || weather.includes('沙') || weather.includes('尘')) {
    return '建议室内运动'
  }
  if (tempNum < 5) {
    return '天气寒冷，注意保暖'
  }
  if (tempNum < 15) {
    return '凉爽舒适，适合运动'
  }
  if (tempNum < 28) {
    return '天气适宜运动'
  }
  if (tempNum < 35) {
    return '天气较热，注意防暑'
  }
  return '高温预警，避免剧烈运动'
}

/**
 * 获取天气SVG图标类型
 */
function getWeatherIconType(iconCode) {
  const code = String(iconCode)
  if (['100', '150'].includes(code)) return 'sunny'
  if (['101', '102', '103', '151'].includes(code)) return 'cloudy'
  if (['104'].includes(code)) return 'overcast'
  if (code.startsWith('3')) return 'rainy'
  if (code.startsWith('4')) return 'snowy'
  if (code.startsWith('5')) return 'foggy'
  return 'sunny'
}

/**
 * 使用和风天气API获取实时天气
 * @param {string} cityId - 城市ID（和风天气城市ID）
 * @returns {Promise<Object>} 天气数据
 */
export async function getWeatherByQWeather(cityId = DEFAULT_CITY) {
  try {
    const response = await fetch(
      `${QWEATHER_API_BASE}/weather/now?location=${cityId}&key=${QWEATHER_API_KEY}`
    )
    const data = await response.json()

    if (data.code === '200' && data.now) {
      const weather = data.now
      const iconInfo = WEATHER_ICONS[weather.icon] || WEATHER_ICONS['999']

      return {
        success: true,
        data: {
          temp: weather.temp,
          feelsLike: weather.feelsLike,
          text: weather.text,
          icon: getWeatherIconType(weather.icon),
          humidity: weather.humidity,
          windDir: weather.windDir,
          windScale: weather.windScale,
          advice: getSportAdvice(weather.temp, weather.text),
          updateTime: weather.obsTime
        }
      }
    }

    throw new Error(data.code || '获取天气失败')
  } catch (error) {
    console.error('和风天气API调用失败:', error)
    return { success: false, error: error.message }
  }
}

/**
 * 使用wttr.in免费API获取天气（通过后端代理，避免CORS）
 * @param {string} city - 城市名称（支持中文）
 * @returns {Promise<Object>} 天气数据
 */
export async function getWeatherByWttr(city = '北京') {
  try {
    // 通过后端代理接口获取天气数据
    const response = await request({
      url: '/api/weather/wttr',
      method: 'get',
      params: { city },
      timeout: 15000 // 减少超时时间到15秒，更快失败并降级
    })

    // 检查响应状态
    if (response.code === 200 && response.data && response.data.raw) {
      // 解析后端返回的原始JSON数据
      let data
      try {
        data = typeof response.data.raw === 'string' 
          ? JSON.parse(response.data.raw) 
          : response.data.raw
      } catch (parseError) {
        console.error('解析天气数据失败:', parseError)
        throw new Error('天气数据格式错误')
      }

      if (data && data.current_condition && data.current_condition[0]) {
        const current = data.current_condition[0]
        const temp = current.temp_C
        const weatherDesc = current.lang_zh && current.lang_zh[0]
          ? current.lang_zh[0].value
          : current.weatherDesc[0].value

        return {
          success: true,
          data: {
            temp: temp,
            feelsLike: current.FeelsLikeC,
            text: weatherDesc,
            icon: getWeatherIconFromWttr(current.weatherCode),
            humidity: current.humidity,
            windDir: current.winddir16Point,
            windSpeed: current.windspeedKmph,
            advice: getSportAdvice(temp, weatherDesc),
            city: response.data.city || city,
            updateTime: new Date().toLocaleTimeString()
          }
        }
      }
    }

    // 如果后端返回了错误信息，使用该信息
    if (response.code !== 200 && response.message) {
      throw new Error(response.message)
    }

    throw new Error('天气数据格式错误')
  } catch (error) {
    // 静默处理错误，不输出到控制台，直接返回失败结果
    // 调用方会处理降级逻辑
    return { success: false, error: '天气服务暂时不可用' }
  }
}

/**
 * 将wttr.in的天气代码转换为图标类型
 */
function getWeatherIconFromWttr(code) {
  const codeNum = parseInt(code)
  if ([113].includes(codeNum)) return 'sunny'
  if ([116, 119, 122].includes(codeNum)) return 'cloudy'
  if ([143, 248, 260].includes(codeNum)) return 'foggy'
  if ([176, 179, 182, 185, 263, 266, 281, 284, 293, 296, 299, 302, 305, 308, 311, 314, 317, 320, 353, 356, 359, 362, 365].includes(codeNum)) return 'rainy'
  if ([227, 230, 323, 326, 329, 332, 335, 338, 350, 368, 371, 374, 377, 386, 389, 392, 395].includes(codeNum)) return 'snowy'
  return 'sunny'
}

/**
 * 通过IP获取用户所在城市并获取天气（通过后端代理，避免CORS）
 * @returns {Promise<Object>} 天气数据
 */
export async function getWeatherByIP() {
  try {
    // 通过后端代理接口获取IP定位信息
    const ipResponse = await request({
      url: '/api/weather/ip-location',
      method: 'get',
      timeout: 10000 // 设置10秒超时
    })

    let city = '北京'
    if (ipResponse.code === 200 && ipResponse.data) {
      // 如果后端返回了原始JSON字符串，尝试解析
      if (ipResponse.data.raw) {
        try {
          const ipData = typeof ipResponse.data.raw === 'string' 
            ? JSON.parse(ipResponse.data.raw) 
            : ipResponse.data.raw
          city = ipData.city || ipResponse.data.city || '北京'
        } catch (e) {
          city = ipResponse.data.city || '北京'
        }
      } else {
        city = ipResponse.data.city || '北京'
      }
    }

    // 然后获取该城市的天气
    return await getWeatherByWttr(city)
  } catch (error) {
    // 静默处理错误，直接使用默认城市
    return await getWeatherByWttr('北京')
  }
}

/**
 * 获取天气数据的统一入口
 * 优先使用wttr.in（无需API Key），失败则返回模拟数据
 * @param {string} city - 城市名称
 * @returns {Promise<Object>} 天气数据
 */
export async function getWeather(city) {
  try {
    // 如果没有指定城市，尝试通过IP获取
    if (!city) {
      const result = await getWeatherByIP()
      if (result.success) {
        return result
      }
    }

    // 使用指定城市或默认城市
    const result = await getWeatherByWttr(city || '北京')
    if (result.success) {
      return result
    }

    // 如果API调用失败，返回模拟数据（静默降级）
    return getFallbackWeather()
  } catch (error) {
    // 静默处理错误，直接返回备用数据
    return getFallbackWeather()
  }
}

/**
 * 获取备用天气数据（当API不可用时）
 */
function getFallbackWeather() {
  const hour = new Date().getHours()
  const baseTemp = 20 + Math.floor(Math.random() * 8)
  const temp = hour >= 12 && hour <= 16 ? baseTemp + 4 : baseTemp

  return {
    success: true,
    data: {
      temp: temp.toString(),
      feelsLike: (temp - 2).toString(),
      text: '晴',
      icon: 'sunny',
      humidity: '45',
      windDir: '东南风',
      windSpeed: '12',
      advice: getSportAdvice(temp.toString(), '晴'),
      city: '本地',
      updateTime: new Date().toLocaleTimeString(),
      isFallback: true // 标记为备用数据
    }
  }
}

export default {
  getWeather,
  getWeatherByWttr,
  getWeatherByQWeather,
  getWeatherByIP
}
