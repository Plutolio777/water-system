<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Activity, RefreshCw } from '@lucide/vue'
import ChartPanel from '../components/ChartPanel.vue'
import { api } from '../api'
import type { Station } from '../types'

interface MonitorReading {
  device: { id: number; deviceCode: string; deviceName: string; status: string }
  latest: {
    id: number; stationId: number; deviceId: number
    power: number; voltage: number; currentValue: number; flowRate: number
    waterLevel: number; waterHead: number; efficiency: number
    bearingTemperature: number; vibration: number; recordedAt: string
  } | null
}
interface MonitorData {
  readings: MonitorReading[]
  series: { time: string; power: number; flow: number }[]
}

const monitorData = ref<MonitorData | null>(null)
const stations = ref<Station[]>([])
const selectedStation = ref<number | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const params = selectedStation.value ? { stationId: selectedStation.value } : {}
    const [m, s] = await Promise.all([
      api.get<unknown, MonitorData>('/monitoring', { params }),
      api.get<unknown, Station[]>('/stations')
    ])
    monitorData.value = m
    stations.value = s
  } finally { loading.value = false }
}
onMounted(load)

const latestReading = () => monitorData.value?.readings.find(r => r.latest)?.latest ?? null

const powerChartOption = () => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 12, top: 24, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: monitorData.value?.series.map(r => r.time) ?? [], axisLabel: { color: '#899391', interval: 2 } },
  yAxis: { type: 'value', name: 'MW', splitLine: { lineStyle: { color: '#edf1ef' } } },
  series: [{ type: 'line', smooth: true, symbol: 'none', data: monitorData.value?.series.map(r => r.power) ?? [], lineStyle: { width: 2.5, color: '#087f75' }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(8,127,117,.2)' }, { offset: 1, color: 'rgba(8,127,117,0)' }] } } }]
})

const flowChartOption = () => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 12, top: 24, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: monitorData.value?.series.map(r => r.time) ?? [], axisLabel: { color: '#899391', interval: 2 } },
  yAxis: { type: 'value', name: 'm3/s', splitLine: { lineStyle: { color: '#edf1ef' } } },
  series: [{ type: 'bar', data: monitorData.value?.series.map(r => r.flow) ?? [], itemStyle: { color: '#e49b38', borderRadius: [3, 3, 0, 0] } }]
})
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="segmented">
        <button :class="{ active: selectedStation === null }" @click="selectedStation = null; load()">全部站点</button>
        <button v-for="s in stations" :key="s.id" :class="{ active: selectedStation === s.id }" @click="selectedStation = s.id; load()">{{ s.stationCode }}</button>
      </div>
      <button class="secondary-button" @click="load"><RefreshCw :class="{ spin: loading }" :size="16" />刷新</button>
    </div>

    <div v-if="loading" class="loading-state"><Activity class="spin" />正在加载监测数据…</div>
    <template v-else-if="monitorData">
      <article v-if="latestReading()" class="panel">
        <div class="panel-head"><div><span class="section-kicker">REAL-TIME READINGS</span><h3>最新实测数据</h3></div><span class="muted-label">{{ latestReading()?.recordedAt }}</span></div>
        <div class="reading-grid">
          <div class="reading-card"><div class="label">有功功率</div><div class="value">{{ latestReading()?.power }}<span class="unit">MW</span></div></div>
          <div class="reading-card"><div class="label">入库流量</div><div class="value">{{ latestReading()?.flowRate }}<span class="unit">m3/s</span></div></div>
          <div class="reading-card"><div class="label">水位</div><div class="value">{{ latestReading()?.waterLevel }}<span class="unit">m</span></div></div>
          <div class="reading-card"><div class="label">水头</div><div class="value">{{ latestReading()?.waterHead }}<span class="unit">m</span></div></div>
          <div class="reading-card"><div class="label">效率</div><div class="value">{{ latestReading()?.efficiency }}<span class="unit">%</span></div></div>
          <div class="reading-card"><div class="label">振动值</div><div class="value">{{ latestReading()?.vibration }}<span class="unit">mm/s</span></div></div>
          <div class="reading-card"><div class="label">轴承温度</div><div class="value">{{ latestReading()?.bearingTemperature }}<span class="unit">℃</span></div></div>
          <div class="reading-card"><div class="label">电压</div><div class="value">{{ latestReading()?.voltage }}<span class="unit">kV</span></div></div>
        </div>
      </article>
      <div class="gen-grid">
        <article class="panel">
          <div class="panel-head"><div><span class="section-kicker">24H POWER</span><h3>有功功率趋势</h3></div></div>
          <ChartPanel :option="powerChartOption()" height="260px" />
        </article>
        <article class="panel">
          <div class="panel-head"><div><span class="section-kicker">24H FLOW</span><h3>入库流量趋势</h3></div></div>
          <ChartPanel :option="flowChartOption()" height="260px" />
        </article>
      </div>
      <article class="panel">
        <div class="panel-head"><div><span class="section-kicker">DEVICE STATUS</span><h3>设备运行状态</h3></div></div>
        <div class="reading-grid">
          <div v-for="row in monitorData.readings" :key="row.device.id" class="reading-card">
            <div class="label">{{ row.device.deviceCode }}</div>
            <div class="value" style="font-size:1rem">{{ row.device.deviceName }}</div>
            <div style="margin-top:6px">
              <span class="status-pill" :class="row.device.status.toLowerCase()">{{ row.device.status }}</span>
              <span v-if="row.latest" style="margin-left:8px;color:#657170;font-size:.85rem">{{ row.latest.power }} MW</span>
            </div>
          </div>
        </div>
      </article>
    </template>
  </div>
</template>
