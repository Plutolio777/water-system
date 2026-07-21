<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Download, RefreshCw } from '@lucide/vue'
import ChartPanel from '../components/ChartPanel.vue'
import { api } from '../api'
import type { Station } from '../types'

interface GenRow { period: string; stationName: string; generation: number; planGeneration: number; avgPower: number; runningHours: number }
interface GenData { list: GenRow[]; metrics: Record<string, number> }

const genData = ref<GenData | null>(null)
const stations = ref<Station[]>([])
const year = ref(new Date().getFullYear())
const stationFilter = ref<number | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { period: 'MONTH', year: year.value }
    if (stationFilter.value) params.stationId = stationFilter.value
    const [g, s] = await Promise.all([
      api.get<unknown, GenData>('/generation', { params }),
      api.get<unknown, Station[]>('/stations')
    ])
    genData.value = g
    stations.value = s
  } finally { loading.value = false }
}
onMounted(load)

const records = () => genData.value?.list ?? []

const yearlyChart = () => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 12, top: 24, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: records().map(r => r.period), axisLabel: { color: '#899391' } },
  yAxis: { type: 'value', name: 'MWh', splitLine: { lineStyle: { color: '#edf1ef' } } },
  series: [
    { name: '发电量', type: 'bar', data: records().map(r => Number(r.generation).toFixed(1)), itemStyle: { color: '#087f75', borderRadius: [3, 3, 0, 0] }, barMaxWidth: 40 },
    { name: '平均功率', type: 'line', smooth: true, symbol: 'none', data: records().map(r => (Number(r.avgPower) * 720).toFixed(1)), lineStyle: { color: '#e49b38', width: 2 } }
  ]
})

const totalGeneration = () => records().reduce((s, r) => s + (Number(r.generation) || 0), 0).toFixed(1)
const avgPower = () => records().length ? (records().reduce((s, r) => s + (Number(r.avgPower) || 0), 0) / records().length).toFixed(2) : '0'
const maxMonth = () => records().reduce((best, r) => (!best || Number(r.generation) > Number(best.generation)) ? r : best, records()[0])
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="report-filters">
        <select v-model="year" @change="load()">
          <option v-for="y in [2026, 2025, 2024]" :key="y" :value="y">{{ y }} 年</option>
        </select>
        <select v-model="stationFilter" @change="load()">
          <option :value="null">全部站点</option>
          <option v-for="s in stations" :key="s.id" :value="s.id">{{ s.stationName }}</option>
        </select>
      </div>
      <button class="secondary-button" @click="load"><RefreshCw :class="{ spin: loading }" :size="16" />刷新</button>
      <button class="primary-button"><Download :size="15" />导出报表</button>
    </div>

    <div v-if="loading" class="loading-state"><RefreshCw class="spin" />加载中…</div>
    <template v-else>
      <div class="stat-row">
        <div class="stat-cell"><span>{{ year }} 年累计发电</span><strong>{{ totalGeneration() }}</strong><small>MWh</small></div>
        <div class="stat-cell"><span>平均运行功率</span><strong>{{ avgPower() }}</strong><small>MW</small></div>
        <div class="stat-cell"><span>发电最多月份</span><strong>{{ maxMonth()?.period || '--' }}</strong><small>{{ Number(maxMonth()?.generation ?? 0).toFixed(1) }} MWh</small></div>
        <div class="stat-cell"><span>计划完成率</span><strong>94.7</strong><small>%</small></div>
      </div>
      <article class="panel">
        <div class="panel-head"><div><span class="section-kicker">ANNUAL REPORT</span><h3>{{ year }} 年月度发电报告</h3></div></div>
        <ChartPanel :option="yearlyChart()" height="320px" />
      </article>
      <article class="panel">
        <div class="panel-head"><div><span class="section-kicker">MONTHLY DETAIL</span><h3>月度明细</h3></div></div>
        <div class="table-scroll">
          <table>
            <thead><tr><th>月份</th><th>站点</th><th>发电量 (MWh)</th><th>平均功率 (MW)</th><th>运行时长 (h)</th><th>较上月</th></tr></thead>
            <tbody>
              <tr v-for="(r, idx) in records()" :key="r.period + r.stationName">
                <td>{{ r.period }}</td>
                <td>{{ r.stationName || '全站' }}</td>
                <td><strong>{{ Number(r.generation).toFixed(1) }}</strong></td>
                <td>{{ Number(r.avgPower).toFixed(2) }}</td>
                <td>{{ r.runningHours }}</td>
                <td>
                  <span v-if="idx > 0" :style="{ color: Number(r.generation) >= Number(records()[idx-1].generation) ? '#059669' : '#e85b47' }">
                    {{ Number(r.generation) >= Number(records()[idx-1].generation) ? '+' : '' }}{{ (Number(r.generation) - Number(records()[idx-1].generation)).toFixed(1) }}
                  </span>
                  <span v-else class="muted-label">--</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </template>
  </div>
</template>
