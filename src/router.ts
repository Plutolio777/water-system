import { createRouter, createWebHistory } from 'vue-router'
import AppShell from './components/AppShell.vue'
import LoginView from './views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    {
      path: '/', component: AppShell, redirect: '/overview', children: [
        { path: 'overview', component: () => import('./views/OverviewView.vue'), meta: { title: '\u7ad9\u70b9\u603b\u89c8', subtitle: '\u6d41\u57df\u8fd0\u884c\u6001\u52bf\u4e0e\u6838\u5fc3\u6307\u6807' } },
        { path: 'monitoring', component: () => import('./views/MonitoringView.vue'), meta: { title: '\u6570\u636e\u76d1\u6d4b', subtitle: '\u673a\u7ec4\u5b9e\u65f6\u6570\u636e\u4e0e\u8fd0\u884c\u8d8b\u52bf' } },
        { path: 'generation', component: () => import('./views/GenerationView.vue'), meta: { title: '\u53d1\u7535\u5206\u6790', subtitle: '\u65e5\u3001\u6708\u3001\u5e74\u53d1\u7535\u91cf\u7edf\u8ba1' } },
        { path: 'reports', component: () => import('./views/ReportsView.vue'), meta: { title: '\u6570\u636e\u62a5\u8868', subtitle: '\u7ad9\u70b9\u6548\u80fd\u4e0e\u8ba1\u5212\u5b8c\u6210\u5206\u6790' } },
        { path: 'devices', component: () => import('./views/DevicesView.vue'), meta: { title: '\u8bbe\u5907\u7ba1\u7406', subtitle: '\u5168\u7ad9\u8bbe\u5907\u53f0\u8d26\u4e0e\u68c0\u4fee\u5468\u671f' } },
        { path: 'alarms', component: () => import('./views/AlarmsView.vue'), meta: { title: '\u544a\u8b66\u7ba1\u7406', subtitle: '\u5f02\u5e38\u4e8b\u4ef6\u5904\u7f6e\u4e0e\u95ed\u73af\u8ffd\u8e2a' } },
        { path: 'maintenance', component: () => import('./views/MaintenanceView.vue'), meta: { title: '\u8fd0\u7ef4\u7ba1\u7406', subtitle: '\u5de5\u5355\u8ba1\u5212\u4e0e\u68c0\u4fee\u5386\u5386' } },
      ],
    },
    { path: '/big-screen', component: () => import('./views/BigScreenView.vue') },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('water-token')
  if (!to.meta.public && !token) return '/login'
  if (to.path === '/login' && token) return '/overview'
})

export default router
