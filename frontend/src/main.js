import { createApp } from 'vue'
import App from './App.vue'
import router from './router' 
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/responsive.css'

// 创建应用实例 [cite: 131]
const app = createApp(App)

// 必须先挂载插件，最后 mount
app.use(router) 
app.use(ElementPlus)
app.mount('#app')