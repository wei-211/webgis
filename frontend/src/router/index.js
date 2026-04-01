import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import MapView from '../views/MapView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/map', name: 'MapView', component: MapView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router


//{
//  path: '/',
//  component: () => import('@/views/MapView.vue')
//}
