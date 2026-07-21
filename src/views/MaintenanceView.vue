<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ClipboardList, Plus, RefreshCw, X } from '@lucide/vue'
import { api } from '../api'
import type { Station, WorkOrder } from '../types'

const orders = ref<WorkOrder[]>([])
const stations = ref<Station[]>([])
const filter = ref('ALL')
const loading = ref(false)
const showModal = ref(false)
const form = ref({ title: '', stationId: '', priority: 'NORMAL', description: '', owner: '' })
const formError = ref('')

async function load() {
  loading.value = true
  try {
    const [o, s] = await Promise.all([
      api.get<unknown, WorkOrder[]>('/work-orders'),
      api.get<unknown, Station[]>('/stations')
    ])
    orders.value = o
    stations.value = s
  } finally {
    loading.value = false
  }
}
onMounted(load)

const shown = computed(() =>
  filter.value === 'ALL' ? orders.value : orders.value.filter(o => o.status === filter.value)
)

async function submitOrder() {
  formError.value = ''
  if (!form.value.title || !form.value.stationId) {
    formError.value = '请填写标题和站点'
    return
  }
  try {
    await api.post('/work-orders', { ...form.value, stationId: Number(form.value.stationId) })
    showModal.value = false
    form.value = { title: '', stationId: '', priority: 'NORMAL', description: '', owner: '' }
    load()
  } catch (e) {
    formError.value = (e as Error).message
  }
}

function statusLabel(s: string) {
  return ({ PENDING: '待处理', IN_PROGRESS: '处理中', COMPLETED: '已完成' } as Record<string, string>)[s] || s
}
function priorityLabel(p: string) {
  return ({ HIGH: '紧急', NORMAL: '一般', LOW: '低' } as Record<string, string>)[p] || p
}
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <div class="segmented">
        <button :class="{ active: filter === 'ALL' }" @click="filter = 'ALL'">
          全部 <b>{{ orders.length }}</b>
        </button>
        <button :class="{ active: filter === 'PENDING' }" @click="filter = 'PENDING'">待处理</button>
        <button :class="{ active: filter === 'IN_PROGRESS' }" @click="filter = 'IN_PROGRESS'">处理中</button>
        <button :class="{ active: filter === 'COMPLETED' }" @click="filter = 'COMPLETED'">已完成</button>
      </div>
      <button class="secondary-button" @click="load">
        <RefreshCw :class="{ spin: loading }" :size="16" />刷新
      </button>
      <button class="primary-button" @click="showModal = true">
        <Plus :size="16" />新建工单
      </button>
    </div>

    <article class="panel">
      <div class="panel-head">
        <div>
          <span class="section-kicker">WORK ORDERS</span>
          <h3>运维工单列表</h3>
        </div>
      </div>
      <div class="order-list">
        <div v-for="o in shown" :key="o.id" class="order-card">
          <div>
            <span class="order-number">{{ o.orderCode }}</span>
            <span class="status-pill" :class="o.status.toLowerCase()" style="margin-left: 8px;">
              {{ statusLabel(o.status) }}
            </span>
          </div>
          <div class="order-body">
            <strong>{{ o.title }}</strong>
            <p>{{ o.description }}</p>
            <div class="order-meta">
              <span>站点：{{ o.stationName }}</span>
              <span>优先级：{{ priorityLabel(o.priority) }}</span>
              <span v-if="o.owner">负责人：{{ o.owner }}</span>
              <span v-if="o.dueDate">截止：{{ o.dueDate?.slice(0, 10) }}</span>
              <span>创建：{{ o.createdAt?.slice(0, 10) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!shown.length" class="empty-state">
        <ClipboardList :size="42" />
        <p>暂无工单记录</p>
      </div>
    </article>

    <Teleport to="body">
      <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-head">
            <h3>新建运维工单</h3>
            <button class="icon-btn" @click="showModal = false"><X /></button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label>工单标题</label>
              <input v-model="form.title" placeholder="请输入工单标题" />
            </div>
            <div class="form-row">
              <label>关联站点</label>
              <select v-model="form.stationId">
                <option value="">请选择站点</option>
                <option v-for="s in stations" :key="s.id" :value="s.id">{{ s.stationName }}</option>
              </select>
            </div>
            <div class="form-row">
              <label>优先级</label>
              <select v-model="form.priority">
                <option value="HIGH">紧急</option>
                <option value="NORMAL">一般</option>
                <option value="LOW">低</option>
              </select>
            </div>
            <div class="form-row">
              <label>负责人</label>
              <input v-model="form.owner" placeholder="请输入负责人姓名" />
            </div>
            <div class="form-row">
              <label>工单描述</label>
              <textarea v-model="form.description" placeholder="请输入工单详细描述" />
            </div>
            <p v-if="formError" class="form-error">{{ formError }}</p>
          </div>
          <div class="form-actions">
            <button class="secondary-button" @click="showModal = false">取消</button>
            <button class="primary-button" @click="submitOrder">提交工单</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
