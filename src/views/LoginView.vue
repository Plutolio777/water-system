<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Droplets, Eye, EyeOff, LockKeyhole, UserRound, Waves, Zap } from '@lucide/vue'
import { useAuthStore } from '../stores/auth'

const username = ref('admin')
const password = ref('admin123')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const router = useRouter()
const auth = useAuthStore()

async function submit() {
  error.value = ''
  loading.value = true
  try { await auth.login(username.value, password.value); router.push('/overview') }
  catch (exception) { error.value = (exception as Error).message }
  finally { loading.value = false }
}
</script>

<template>
  <main class="login-page">
    <section class="login-visual">
      <div class="contour contour-one" /><div class="contour contour-two" /><div class="contour contour-three" />
      <div class="login-brand">
        <span><Droplets /></span>
        <div><strong>清源水能</strong><small>QINGYUAN HYDRO</small></div>
      </div>
      <div class="visual-copy">
        <p>MICRO HYDRO · DIGITAL TWIN</p>
        <h1>让每一方水，<br>清晰抵达每一度电。</h1>
        <div class="visual-stats">
          <div><Waves /><span>实时入库流量<strong>91.6 m³/s</strong></span></div>
          <div><Zap /><span>当前并网功率<strong>20.77 MW</strong></span></div>
        </div>
      </div>
      <div class="flow-line"><i v-for="n in 7" :key="n" /></div>
    </section>
    <section class="login-panel">
      <form @submit.prevent="submit">
        <div class="mobile-logo"><Droplets /><strong>清源水能</strong></div>
        <p class="eyebrow">水电站数字控制台</p>
        <h2>欢迎回来</h2>
        <p class="form-intro">登录后查看流域站点的实时运行状况。</p>
        <label>账号<div class="input-wrap"><UserRound /><input v-model="username" autocomplete="username" placeholder="请输入账号" /></div></label>
        <label>密码<div class="input-wrap"><LockKeyhole /><input v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="请输入密码" /><button type="button" @click="showPassword=!showPassword"><EyeOff v-if="showPassword" /><Eye v-else /></button></div></label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button login-button" :disabled="loading">
          <span>{{ loading ? '正在连接站点…' : '进入管理系统' }}</span><ArrowRight />
        </button>
        <div class="demo-account"><span>演示账号</span><code>admin</code><i>/</i><code>admin123</code></div>
      </form>
      <p class="copyright">© 2026 清源微型水能源数字管理系统</p>
    </section>
  </main>
</template>
