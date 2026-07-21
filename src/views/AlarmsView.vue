<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Bell, CheckCheck, RefreshCw, ShieldAlert } from '@lucide/vue'
import { api } from '../api'
import type { Alarm } from '../types'

const alarms = ref<Alarm[]>([])
const filter = ref('ALL')
const loading = ref(false)
async function load() { loading.value = true; try { alarms.value = await api.get<unknown, Alarm[]>('/alarms') } finally { loading.value = false } }
onMounted(load)
const shown = computed(() => filter.value === 'ALL' ? alarms.value : alarms.value.filter(a => a.status === filter.value))
const activeCount = computed(() => alarms.value.filter(a => a.status === 'ACTIVE').length)
async function resolve(alarm: Alarm) {
  if (alarm.status !== 'ACTIVE') return
  await api.patch(`/alarms/${alarm.id}/resolve`)
  alarm.status = 'RESOLVED'
}
function levelText(l: string) { return ({ HIGH: '高危', MEDIUM: '中', LOW: '低' } as Record<string, string>)[l] || l }
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="segmented">
        <button :class="{ active: filter === 'ALL' }" @click="filter = 'ALL'">全部 <b>{{ alarms.length }}</b></button>
        <button :class="{ active: filter === 'ACTIVE' }" @click="filter = 'ACTIVE'">待处理 <b class="badge-red">{{ activeCount }}</b></button>
        <button :class="{ active: filter === 'RESOLVED' }" @click="filter = 'RESOLVED'">已恢复</button>
      </div>
      <button class="secondary-button" @click="load"><RefreshCw :class="{ spin: loading }" :size="16" />刷新</button>
    </div>
    <div v-if="activeCount > 0" class="alarm-banner">
      <ShieldAlert /><strong>当前有 {{ activeCount }} 条告警待处理</strong>，请及时确认并安排值班人员排查。
    </div>
    <article class="panel">
      <div class="panel-head"><div><span class="section-kicker">EVENT LOG</span><h3>告警事件列表</h3></div></div>
      <div class="table-scroll">
        <table class="alarm-table">
          <thead>
            <tr><th>级别</th><th>告警内容</th><th>站点 / 设备</th><th>当前值</th><th>阈值</th><th>发生时间</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="a in shown" :key="a.id" :class="{ resolved: a.status === 'RESOLVED' }">
              <td><span class="alarm-level" :class="a.level.toLowerCase()">{{ levelText(a.level) }}</span></td>
              <td><strong>{{ a.title }}</strong></td>
              <td><div><span>{{ a.stationName }}</span><small>{{ a.deviceName }}</small></div></td>
              <td>{{ a.currentValue }}</td>
              <td>{{ a.thresholdValue }}</td>
              <td>{{ a.occurredAt }}</td>
              <td><span class="status-pill" :class="a.status.toLowerCase()">{{ a.status === 'ACTIVE' ? '待处理' : '已恢复' }}</span></td>
              <td>
                <button v-if="a.status === 'ACTIVE'" class="action-btn" @click="resolve(a)"><CheckCheck :size="15" />确认</button>
                <span v-else class="handled-by">{{ a.handler }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
    <article v-if="!alarms.length" class="panel empty-state"><Bell :size="42" /><p>暂无告警记录</p></article>
  </div>
</template>
