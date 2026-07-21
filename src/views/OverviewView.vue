<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { AlertTriangle, ArrowDownRight, ArrowUpRight, CloudRain, Droplet, Gauge, Radio, RefreshCw, Waves, Zap } from '@lucide/vue'
import ChartPanel from '../components/ChartPanel.vue'
import { api } from '../api'
import type { Alarm, SeriesPoint, Station } from '../types'

interface Overview { metrics: Record<string, number>; stations: Station[]; series: SeriesPoint[]; alarms: Alarm[]; weather: Record<string, number | string> }
const data = ref<Overview | null>(null)
const loading = ref(true)
async function load() { loading.value = true; try { data.value = await api.get<unknown, Overview>('/overview') } finally { loading.value = false } }
onMounted(load)
const powerOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 12, top: 24, bottom: 8, containLabel: true },
  legend: { right: 4, top: 0, data: ['并网功率', '入库流量'], textStyle: { color: '#657170' } },
  xAxis: { type: 'category', boundaryGap: false, data: data.value?.series.map(i => i.time), axisLine: { lineStyle: { color: '#dbe3e1' } }, axisLabel: { color: '#899391', interval: 3 } },
  yAxis: [
    { type: 'value', name: 'MW', splitLine: { lineStyle: { color: '#edf1ef' } } },
    { type: 'value', name: 'm³/s', splitLine: { show: false } }
  ],
  series: [
    { name: '并网功率', type: 'line', smooth: true, symbol: 'none', data: data.value?.series.map(i => i.power), lineStyle: { width: 3, color: '#087f75' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(8,127,117,.28)' }, { offset: 1, color: 'rgba(8,127,117,0)' }] } } },
    { name: '入库流量', type: 'line', smooth: true, symbol: 'none', yAxisIndex: 1, data: data.value?.series.map(i => i.flow), lineStyle: { width: 2, color: '#e49b38' } },
  ],
}))
const metricCards = computed(() => [
  { label: '当前并网功率', value: data.value?.metrics.currentPower || 0, unit: 'MW', icon: Zap, trend: '+4.8%', good: true },
  { label: '今日发电量', value: data.value?.metrics.todayGeneration || 0, unit: 'MWh', icon: Gauge, trend: '+6.2%', good: true },
  { label: '本月累计发电', value: data.value?.metrics.monthGeneration || 0, unit: 'MWh', icon: Waves, trend: '94.7%', good: true },
  { label: '设备在线率', value: data.value?.metrics.onlineRate || 0, unit: '%', icon: Radio, trend: '-0.3%', good: false },
])
function statusText(value: string) { return value === 'RUNNING' ? '平稳运行' : '需关注' }
function alarmLevel(value: string) { return value === 'HIGH' ? '高' : value === 'MEDIUM' ? '中' : '低' }
</script>

<template>
  <div v-if="loading" class="loading-state"><RefreshCw class="spin" />正在接入流域数据</div>
  <div v-else-if="data" class="overview-view">
    <div class="metric-grid">
      <article v-for="card in metricCards" :key="card.label" class="metric-card">
        <div class="metric-icon"><component :is="card.icon" /></div>
        <div class="metric-copy">
          <span>{{ card.label }}</span>
          <strong>{{ Number(card.value).toLocaleString() }}<small>{{ card.unit }}</small></strong>
        </div>
        <div class="metric-trend" :class="{ down: !card.good }">
          <ArrowUpRight v-if="card.good" /><ArrowDownRight v-else />{{ card.trend }}
        </div>
      </article>
    </div>
    <div class="dashboard-grid">
      <article class="panel chart-panel wide">
        <div class="panel-head">
          <div><span class="section-kicker">24H LOAD CURVE</span><h3>流域功率负荷</h3></div>
          <div class="live-label"><i />实时刷新</div>
        </div>
        <ChartPanel :option="powerOption" height="305px" />
      </article>
      <article class="panel weather-panel">
        <div class="weather-top">
          <div>
            <span>清源河流域</span>
            <strong>{{ data.weather.temperature }}°</strong>
            <p>{{ data.weather.condition }}</p>
          </div>
          <CloudRain :size="56" />
        </div>
        <div class="weather-data">
          <div><Droplet /><span>24h 降水<strong>{{ data.weather.rainfall }} mm</strong></span></div>
          <div><Waves /><span>预测来水<strong>{{ data.weather.inflowForecast }} m³/s</strong></span></div>
        </div>
        <div class="weather-note">预计 16:00 后来水量上升，当前库容可安全承接。</div>
      </article>
    </div>
    <div class="dashboard-grid lower">
      <article class="panel station-panel wide">
        <div class="panel-head">
          <div><span class="section-kicker">STATION PULSE</span><h3>站点运行态势</h3></div>
          <RouterLink to="/monitoring" class="text-link">查看实时监测</RouterLink>
        </div>
        <div class="station-list">
          <div v-for="station in data.stations" :key="station.id" class="station-row">
            <span class="station-code">{{ station.stationCode }}</span>
            <div class="station-name">
              <strong>{{ station.stationName }}</strong>
              <small>{{ station.riverName }} · {{ station.manager }}</small>
            </div>
            <div class="station-data">
              <span>实时功率<strong>{{ station.currentPower }} MW</strong></span>
              <span>当前水位<strong>{{ station.waterLevel || '--' }} m</strong></span>
              <span>入库流量<strong>{{ station.inflow || '--' }} m³/s</strong></span>
            </div>
            <span class="status-pill" :class="station.status.toLowerCase()">{{ statusText(station.status) }}</span>
          </div>
        </div>
      </article>
      <article class="panel alarm-preview">
        <div class="panel-head">
          <div><span class="section-kicker">LATEST EVENTS</span><h3>最新告警</h3></div>
          <RouterLink to="/alarms" class="text-link">全部告警</RouterLink>
        </div>
        <div class="alarm-mini-list">
          <div v-for="alarm in data.alarms" :key="alarm.id" class="alarm-mini">
            <span class="alarm-level" :class="alarm.level.toLowerCase()">{{ alarmLevel(alarm.level) }}</span>
            <div>
              <strong>{{ alarm.title }}</strong>
              <small>{{ alarm.stationName }} · {{ alarm.occurredAt.slice(11,16) }}</small>
            </div>
            <span :class="['alarm-state', alarm.status.toLowerCase()]">{{ alarm.status === 'ACTIVE' ? '待处理' : '已恢复' }}</span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
