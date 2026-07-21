<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Activity, BarChart3, BellRing, ChevronRight, ClipboardList, Droplets, Gauge, LayoutDashboard, LogOut, Menu, MonitorUp, Search, Settings2, X, Zap } from '@lucide/vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)
const title = computed(() => String(route.meta.title || '清源水能'))
const subtitle = computed(() => String(route.meta.subtitle || '微型水能源数字管理系统'))
const menus = [
  { to: '/overview', label: '站点总览', icon: LayoutDashboard },
  { to: '/monitoring', label: '数据监测', icon: Activity },
  { to: '/generation', label: '发电分析', icon: Zap },
  { to: '/reports', label: '数据报表', icon: BarChart3 },
  { to: '/devices', label: '设备管理', icon: Settings2 },
  { to: '/alarms', label: '告警管理', icon: BellRing, badge: 2 },
  { to: '/maintenance', label: '运维管理', icon: ClipboardList },
]
function leave() { auth.logout(); router.push('/login') }
</script>

<template>
  <div class="app-shell">
    <div v-if="mobileOpen" class="scrim" @click="mobileOpen = false" />
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand">
        <div class="brand-mark"><Droplets :size="24" /></div>
        <div><strong>清源水能</strong><span>QINGYUAN HYDRO</span></div>
        <button class="icon-btn mobile-close" aria-label="关闭菜单" @click="mobileOpen=false"><X /></button>
      </div>
      <div class="basin-status"><span class="live-dot" />流域设备在线 <strong>98.6%</strong></div>
      <nav>
        <RouterLink v-for="item in menus" :key="item.to" :to="item.to" @click="mobileOpen=false">
          <component :is="item.icon" :size="19" /><span>{{ item.label }}</span>
          <b v-if="item.badge">{{ item.badge }}</b>
          <ChevronRight v-else class="chevron" :size="15" />
        </RouterLink>
      </nav>
      <div class="sidebar-foot">
        <RouterLink to="/big-screen" class="big-screen-link"><MonitorUp :size="19" /><span>站点数据大屏</span></RouterLink>
        <div class="user-card">
          <div class="avatar">{{ auth.user?.realName?.slice(-1) || '管' }}</div>
          <div>
            <strong>{{ auth.user?.realName || '系统管理员' }}</strong>
            <span>{{ auth.user?.role === 'ADMIN' ? '管理员' : '值班员' }}</span>
          </div>
          <button class="icon-btn" title="退出登录" @click="leave"><LogOut :size="17" /></button>
        </div>
      </div>
    </aside>
    <main>
      <header class="topbar">
        <button class="icon-btn menu-button" aria-label="打开菜单" @click="mobileOpen=true"><Menu /></button>
        <div class="page-heading"><p>{{ subtitle }}</p><h1>{{ title }}</h1></div>
        <div class="top-actions">
          <label class="search"><Search :size="17" /><input placeholder="搜索站点、设备或告警" /></label>
          <div class="clock">
            <span>系统时间</span>
            <strong>{{ new Date().toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' }) }}</strong>
          </div>
          <button class="icon-btn alert-button" title="告警"><BellRing :size="19" /><i /></button>
        </div>
      </header>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>
