<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { Activity, Droplets, Gauge, Zap } from '@lucide/vue'
import ChartPanel from '../components/ChartPanel.vue'
import { api } from '../api'
import type { Alarm, SeriesPoint, Station } from '../types'

interface Overview { metrics: Record<string, number>; stations: Station[]; series: SeriesPoint[]; alarms: Alarm[] }
const data = ref<Overview | null>(null)
const now = ref(new Date())
let timer: ReturnType<typeof setInterval>
async function load() { try { data.value = await api.get<unknown, Overview>('/overview') } catch { /* retry */ } }
onMounted(() => { load(); timer = setInterval(() => { load(); now.value = new Date() }, 30000) })
onUnmounted(() => clearInterval(timer))
const powerOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis', backgroundColor: '#0d2b2a', borderColor: '#1e4a48', textStyle: { color: '#c8dedd' } },
  grid: { left: 14, right: 14, top: 20, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: data.value?.series.map(i => i.time), boundaryGap: false, axisLine: { lineStyle: { color: '#1e4a48' } }, axisLabel: { color: '#4a7a78', interval: 5 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#0f3533' } }, axisLabel: { color: '#4a7a78' } },
  series: [{ type: 'line', smooth: true, symbol: 'none', data: data.value?.series.map(i => i.power), lineStyle: { width: 3, color: '#3ecfbf' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(62,207,191,.35)' }, { offset: 1, color: 'rgba(62,207,191,0)' }] } } }],
}))
function statusLabel(s: string) { return s === 'RUNNING' ? '正常' : '关注' }
function alarmLevelLabel(l: string) { return ({ HIGH: '高危', MEDIUM: '中', LOW: '低' } as Record<string, string>)[l] || l }
</script>

<template>
  <div class="big-screen">
    <header class="bs-header">
      <div class="bs-logo"><Droplets :size="28" /><div><strong>清源水能</strong><small>QINGYUAN HYDRO DIGITAL TWIN</small></div></div>
      <div class="bs-title"><span>微型水能源数字管理系统</span><h1>流域运行态势大屏</h1></div>
      <div class="bs-clock">
        <strong>{{ now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) }}</strong>
        <span>{{ now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short' }) }}</span>
      </div>
    </header>
    <div v-if="data" class="bs-body">
      <div class="bs-left">
        <div class="bs-kpi-grid">
          <div class="bs-kpi"><Zap /><div><strong>{{ data.metrics.currentPower }}</strong><span>MW 当前功率</span></div></div>
          <div class="bs-kpi"><Gauge /><div><strong>{{ data.metrics.todayGeneration }}</strong><span>MWh 今日发电</span></div></div>
          <div class="bs-kpi"><Activity /><div><strong>{{ data.metrics.onlineRate }}%</strong><span>设备在线率</span></div></div>
          <div class="bs-kpi" :class="data.metrics.activeAlarms > 0 ? 'kpi-alarm' : ''">
            <Droplets /><div><strong>{{ data.metrics.activeAlarms }}</strong><span>待处理告警</span></div>
          </div>
        </div>
        <div class="bs-panel"><div class="bs-panel-title">24H 功率负荷曲线</div><ChartPanel :option="powerOption" height="240px" /></div>
      </div>
      <div class="bs-center">
        <div class="bs-panel station-status">
          <div class="bs-panel-title">站点运行状态</div>
          <div v-for="s in data.stations" :key="s.id" class="bs-station">
            <div class="bs-station-led" :class="s.status.toLowerCase()" />
            <div class="bs-station-info"><strong>{{ s.stationName }}</strong><small>{{ s.riverName }}</small></div>
            <div class="bs-station-vals"><span>{{ s.currentPower }} MW</span><span>{{ s.waterLevel || '--' }} m</span></div>
            <span class="bs-station-status" :class="s.status.toLowerCase()">{{ statusLabel(s.status) }}</span>
          </div>
        </div>
        <div class="bs-panel">
          <div class="bs-panel-title">最新告警事件</div>
          <div v-for="a in data.alarms" :key="a.id" class="bs-alarm">
            <span class="bs-alarm-level" :class="a.level.toLowerCase()">{{ alarmLevelLabel(a.level) }}</span>
            <div><strong>{{ a.title }}</strong><small>{{ a.stationName }} · {{ a.occurredAt?.slice(11, 16) }}</small></div>
            <span :class="['bs-alarm-state', a.status.toLowerCase()]">{{ a.status === 'ACTIVE' ? '待处理' : '已恢复' }}</span>
          </div>
        </div>
      </div>
      <div class="bs-right">
        <div class="bs-panel">
          <div class="bs-panel-title">装机容量分布</div>
          <div class="capacity-bars">
            <div v-for="s in data.stations" :key="s.id" class="cap-bar">
              <span>{{ s.stationCode }}</span>
              <div class="cap-track"><div class="cap-fill" :style="{ width: (s.installedCapacity / 36.5 * 100) + '%' }" /></div>
              <strong>{{ s.installedCapacity }} MW</strong>
            </div>
          </div>
        </div>
        <div class="bs-panel">
          <div class="bs-panel-title">本月发电概览</div>
          <div class="bs-month-stats">
            <div><span>本月累计</span><strong>{{ Number(data.metrics.monthGeneration || 0).toLocaleString() }} MWh</strong></div>
            <div><span>年度累计</span><strong>{{ Number(data.metrics.yearGeneration || 0).toLocaleString() }} MWh</strong></div>
            <div><span>计划完成率</span><strong>94.7%</strong></div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="bs-loading"><Activity class="spin" /><span>正在接入流域数据…</span></div>
  </div>
</template>
