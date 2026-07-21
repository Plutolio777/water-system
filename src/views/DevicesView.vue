<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CheckCircle2, Settings2, TriangleAlert, Wrench } from '@lucide/vue'
import { api } from '../api'
import type { Device, Station } from '../types'

const devices = ref<Device[]>([])
const stations = ref<Station[]>([])
const stationFilter = ref('')
const search = ref('')

onMounted(async () => {
  const [d, s] = await Promise.all([api.get<unknown, Device[]>('/devices'), api.get<unknown, Station[]>('/stations')])
  devices.value = d; stations.value = s
})

const filtered = computed(() =>
  devices.value.filter(d => {
    const matchStation = !stationFilter.value || String(d.stationId) === stationFilter.value
    const matchSearch = !search.value || d.deviceName.includes(search.value) || d.deviceCode.includes(search.value)
    return matchStation && matchSearch
  })
)
function statusLabel(s: string) { return ({ RUNNING: '运行中', WARNING: '预警', ONLINE: '在线', OFFLINE: '离线' } as Record<string, string>)[s] || s }
function urgency(date: string) {
  if (!date) return 'ok'
  const diff = Math.ceil((new Date(date).getTime() - Date.now()) / 86400000)
  if (diff <= 7) return 'urgent'
  if (diff <= 30) return 'soon'
  return 'ok'
}
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="segmented">
        <button :class="{ active: stationFilter === '' }" @click="stationFilter = ''">全部</button>
        <button v-for="s in stations" :key="s.id" :class="{ active: stationFilter === String(s.id) }" @click="stationFilter = String(s.id)">{{ s.stationCode }}</button>
      </div>
      <input v-model="search" class="search-input" placeholder="搜索设备名称或编号" />
    </div>
    <div class="device-grid">
      <article v-for="d in filtered" :key="d.id" class="device-card" :class="d.status.toLowerCase()">
        <div class="device-card-head">
          <div class="device-icon"><Settings2 /></div>
          <div>
            <span class="device-code">{{ d.deviceCode }}</span>
            <h3>{{ d.deviceName }}</h3>
            <small>{{ d.stationName }} · {{ d.model }}</small>
          </div>
          <span class="status-pill" :class="d.status.toLowerCase()">{{ statusLabel(d.status) }}</span>
        </div>
        <div class="device-metrics">
          <div><span>额定功率</span><strong>{{ d.ratedPower }} MW</strong></div>
          <div><span>当前功率</span><strong>{{ d.currentPower }} MW</strong></div>
          <div><span>累计运行</span><strong>{{ d.runningHours.toLocaleString() }} h</strong></div>
        </div>
        <div class="device-footer" :class="urgency(d.nextMaintenanceDate)">
          <CheckCircle2 v-if="urgency(d.nextMaintenanceDate) === 'ok'" :size="15" />
          <Wrench v-else-if="urgency(d.nextMaintenanceDate) === 'soon'" :size="15" />
          <TriangleAlert v-else :size="15" />
          <span>下次检修：{{ d.nextMaintenanceDate || '未设定' }}</span>
        </div>
      </article>
    </div>
    <div v-if="!filtered.length" class="empty-state"><Settings2 :size="42" /><p>暂无匹配设备</p></div>
  </div>
</template>
