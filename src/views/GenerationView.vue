<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RefreshCw } from '@lucide/vue'
import ChartPanel from '../components/ChartPanel.vue'
import { api } from '../api'
import type { Station } from '../types'

interface GenRow { period: string; stationName: string; generation: number; planGeneration: number; avgPower: number; runningHours: number; rainfall: number }
interface GenData { list: GenRow[]; metrics: Record<string, number> }

const genData = ref<GenData | null>(null)
const stations = ref<Station[]>([])
const period = ref<'DAY' | 'MONTH' | 'YEAR'>('DAY')
const stationFilter = ref<number | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { period: period.value }
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

const barOption = () => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 12, top: 24, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: records().map(r => r.period), axisLabel: { color: '#899391', interval: period.value === 'DAY' ? 4 : 0 } },
  yAxis: { type: 'value', name: 'MWh', splitLine: { lineStyle: { color: '#edf1ef' } } },
  series: [{ type: 'bar', data: records().map(r => r.generation), itemStyle: { color: '#087f75', borderRadius: [3, 3, 0, 0] }, barMaxWidth: 40 }]
})

const totalGeneration = () => records().reduce((s, r) => s + (Number(r.generation) || 0), 0).toFixed(1)
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="segmented">
        <button :class="{ active: period === 'DAY' }" @click="period = 'DAY'; load()">日发电</button>
        <button :class="{ active: period === 'MONTH' }" @click="period = 'MONTH'; load()">月发电</button>
        <button :class="{ active: period === 'YEAR' }" @click="period = 'YEAR'; load()">年发电</button>
      </div>
      <div class="segmented">
        <button :class="{ active: stationFilter === null }" @click="stationFilter = null; load()">全部</button>
        <button v-for="s in stations" :key="s.id" :class="{ active: stationFilter === s.id }" @click="stationFilter = s.id; load()">{{ s.stationCode }}</button>
      </div>
      <button class="secondary-button" @click="load"><RefreshCw :class="{ spin: loading }" :size="16" />刷新</button>
    </div>

    <div v-if="loading" class="loading-state"><RefreshCw class="spin" />加载中…</div>
    <template v-else>
      <article class="panel">
        <div class="panel-head">
          <div><span class="section-kicker">GENERATION ANALYSIS</span><h3>发电量统计</h3></div>
          <span class="muted-label">合计 {{ totalGeneration() }} MWh</span>
        </div>
        <ChartPanel :option="barOption()" height="360px" />
      </article>
      <article class="panel">
        <div class="panel-head"><div><span class="section-kicker">DETAIL TABLE</span><h3>明细数据</h3></div></div>
        <div class="table-scroll">
          <table>
            <thead><tr><th>时段</th><th>站点</th><th>发电量 (MWh)</th><th>平均功率 (MW)</th><th>运行时长 (h)</th></tr></thead>
            <tbody>
              <tr v-for="r in records()" :key="r.period + r.stationName">
                <td>{{ r.period }}</td>
                <td>{{ r.stationName }}</td>
                <td><strong>{{ Number(r.generation).toFixed(1) }}</strong></td>
                <td>{{ Number(r.avgPower).toFixed(2) }}</td>
                <td>{{ r.runningHours }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </template>
  </div>
</template>
