import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './styles/theme.css'
import './styles/admin.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { applyTheme, getSavedTheme } from '@/utils/themes'

const app = createApp(App)

// 启动时加载已保存的主题
applyTheme(getSavedTheme())
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
