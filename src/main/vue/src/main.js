import {createApp} from 'vue'
import BootstrapVue3 from 'bootstrap-vue-3'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css'
import axios from 'axios'
import mitt from 'mitt';
import BranchMgr from './BranchMgr.vue'

const app = createApp(BranchMgr)
const emitter = mitt();
app.config.globalProperties.emitter = emitter;
app.config.globalProperties.axios = axios;
app.use(BootstrapVue3)
app.mount('#app')
