/**
 * utils/themes.js - 主题定义
 * 定义 10 套预设主题，并提供 applyTheme / getSavedTheme 工具函数
 */

// 通用 RGBA 变量
const mk = (r, g, b) => ({
  '--surface-primary': `rgba(${r}, ${g}, ${b}, 0.08)`,
  '--surface-primary-strong': `rgba(${r}, ${g}, ${b}, 0.15)`,
  '--shadow-primary': `rgba(${r}, ${g}, ${b}, 0.12)`,
  '--shadow-primary-hover': `rgba(${r}, ${g}, ${b}, 0.15)`,
  '--shadow-primary-20': `rgba(${r}, ${g}, ${b}, 0.2)`,
  '--shadow-primary-25': `rgba(${r}, ${g}, ${b}, 0.25)`,
  '--shadow-primary-30': `rgba(${r}, ${g}, ${b}, 0.3)`,
  '--shadow-primary-40': `rgba(${r}, ${g}, ${b}, 0.4)`,
  '--shadow-primary-50': `rgba(${r}, ${g}, ${b}, 0.5)`,
  '--shadow-primary-08': `rgba(${r}, ${g}, ${b}, 0.08)`
})

function hexToRgb(hex) {
  const h = hex.replace('#', '')
  return { r: parseInt(h.substring(0, 2), 16), g: parseInt(h.substring(2, 4), 16), b: parseInt(h.substring(4, 6), 16) }
}

function lighten(hex, p) {
  const { r, g, b } = hexToRgb(hex); const f = 1 + p / 100; const c = v => Math.min(255, Math.round(v * f))
  return `#${[r, g, b].map(v => c(v).toString(16).padStart(2, '0')).join('')}`
}

function darken(hex, p) {
  const { r, g, b } = hexToRgb(hex); const f = 1 - p / 100; const c = v => Math.max(0, Math.round(v * f))
  return `#${[r, g, b].map(v => c(v).toString(16).padStart(2, '0')).join('')}`
}

function elLightSteps(hex) {
  const { r, g, b } = hexToRgb(hex)
  const rv = {}
  const ratios = [3, 5, 7, 8, 9, 10]
  ratios.forEach(n => {
    const ratio = 1 - (n - 3) / 8
    const m = v => Math.round(v + (255 - v) * (1 - ratio))
    rv[`--el-color-primary-light-${n}`] = `#${[r, g, b].map(c => m(c).toString(16).padStart(2, '0')).join('')}`
  })
  return rv
}

const themes = {}

// Helper: build full theme object
function t(name, r, g, b, primary, light, opts = {}) {
  const eSteps = elLightSteps(primary)
  const bg = opts.bgPage || lighten(primary, 85)
  const textPrimary = opts.textPrimary || (r + g + b < 400 ? '#f5f5f5' : '#1a1a1a')
  const textSecondary = opts.textSecondary || (r + g + b < 400 ? '#b0b0b0' : '#6b6b6b')
  const border = opts.border || (r + g + b < 400 ? '#444' : '#e0e0e0')
  const borderLight = opts.borderLight || (r + g + b < 400 ? '#333' : '#e5e5e5')
  const bgGradient = opts.bodyBg || `linear-gradient(135deg, ${light} 0%, ${bg} 50%, ${light} 100%)`

  themes[name] = {
    ...(opts.displayName ? { name: opts.displayName } : {}),
    colors: {
      ...mk(r, g, b),
      '--brand-primary': primary,
      '--brand-primary-hover': opts.hover || lighten(primary, 8),
      '--brand-primary-active': opts.active || darken(primary, 8),
      '--brand-primary-light': light,
      '--accent-gold': opts.accentGold || lighten(primary, 25),
      '--accent-gold-light': light,
      '--bg-page': bg,
      '--el-color-primary': primary,
      '--el-color-primary-dark-2': opts.dark2 || darken(primary, 10),
      '--el-bg-color': bg,
      ...eSteps,
      '--el-button-bg-color': primary,
      '--el-button-border-color': primary,
      '--el-button-hover-bg-color': opts.hover || lighten(primary, 8),
      '--body-bg': bgGradient,
      '--text-primary': textPrimary,
      '--text-secondary': textSecondary,
      '--border-color': border,
      '--border-light': borderLight
    }
  }
}

t('orange', 232, 149, 106, '#e8956a', '#fef0e8', { displayName: '暖橙', bgPage: '#fdf4ee', textPrimary: '#2D2421', textSecondary: '#7D7272', border: '#DDD0C8', borderLight: '#EDE2DA' })
t('caramel', 160, 98, 58, '#a0623a', '#fdf6f0', { displayName: '焦糖', bgPage: '#fdf8f3', textPrimary: '#2D241D', textSecondary: '#7D7268', border: '#DDD0C4', borderLight: '#EEE6DE' })
t('sakura', 232, 138, 160, '#e88aa0', '#fef0f4', { displayName: '樱花粉', bgPage: '#fef5f8', textPrimary: '#2D1D22', textSecondary: '#7D686E', border: '#DDC8CE', borderLight: '#EEE0E4' })
t('morandi', 200, 152, 170, '#c898aa', '#fdf6f0', { displayName: '莫兰迪粉', bgPage: '#faf6f3', textPrimary: '#2D2428', textSecondary: '#7D7276', border: '#D8D0D2', borderLight: '#EEE8EA' })
t('green', 95, 168, 127, '#5fa87f', '#e8f5ee', { displayName: '自然绿', bgPage: '#f0f7f3', textPrimary: '#1D2B24', textSecondary: '#6B7C72', border: '#C8D4CC', borderLight: '#DCEAE0' })
t('forest', 58, 122, 92, '#3a7a5c', '#e8f0ec', { displayName: '墨绿', bgPage: '#f0f5f2', textPrimary: '#1A2A24', textSecondary: '#5A6F66', border: '#C0CCC5', borderLight: '#D5DED8' })
t('blue', 91, 155, 213, '#5b9bd5', '#e8f0fa', { displayName: '海洋蓝', bgPage: '#eef4fa', textPrimary: '#1D2B3A', textSecondary: '#6B7887', border: '#C8D4DE', borderLight: '#DCE6EE' })
t('business', 44, 82, 130, '#2c5282', '#eef4fa', { displayName: '商务蓝', bgPage: '#f5f7fa', textPrimary: '#1a2332', textSecondary: '#606a78', border: '#d0d6de', borderLight: '#e5e9ee' })
t('white', 42, 42, 42, '#2a2a2a', '#f5f5f5', { displayName: '简约白', bgPage: '#fafafa', textPrimary: '#1a1a1a', textSecondary: '#6b6b6b', border: '#e0e0e0', borderLight: '#e5e5e5' })
t('lavender', 150, 95, 210, '#9665d2', '#ede4f7', { displayName: '薰衣草紫', bgPage: '#f3edf8', textPrimary: '#1F1830', textSecondary: '#726580', border: '#D2C5DE', borderLight: '#E4DCE8' })

export function applyTheme(key) {
  const theme = themes[key]
  if (!theme) return
  const root = document.documentElement
  for (const [prop, val] of Object.entries(theme.colors)) {
    root.style.setProperty(prop, val)
  }
  localStorage.setItem('claw-pet-theme', key)
}

export function getSavedTheme() {
  const t = localStorage.getItem('claw-pet-theme')
  return themes[t] ? t : 'white'
}

export default themes
