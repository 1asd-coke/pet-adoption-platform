<template>
  <div class="dashboard-page" v-loading="loading">
    <!-- 顶部 4 大指标 -->
    <div class="stats-grid">
      <div v-for="(stat, i) in statsCards" :key="stat.label" class="stat-block" :style="{ animationDelay: i * 0.06 + 's' }">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20" v-html="stat.icon"></svg>
        </div>
        <div class="stat-content">
          <div class="stat-number" :style="{ background: stat.gradient, '-webkit-background-clip': 'text', '-webkit-text-fill-color': 'transparent' }">
            <CountUp :end="stat.value" />
          </div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-glow" :style="{ background: stat.glow }"></div>
      </div>
    </div>

    <!-- 主图区：8 列趋势 + 4 列分类 -->
    <div class="charts-grid">
      <div class="glass-card chart-main">
        <div class="card-head">
          <div class="head-text">
            <h3>领养趋势</h3>
            <span>近 12 月</span>
          </div>
          <span class="head-icon">📈</span>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <div class="glass-card chart-side">
        <div class="card-head">
          <div class="head-text">
            <h3>宠物分类</h3>
            <span>5 类分布</span>
          </div>
          <span class="head-icon">📊</span>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 底图区：8 列申请列表 + 4 列状态进度 -->
    <div class="charts-grid charts-grid-compact">
      <div class="glass-card chart-main">
        <div class="card-head">
          <div class="head-text">
            <h3>最近领养申请</h3>
            <span>最新动态</span>
          </div>
          <span class="head-icon">🕐</span>
        </div>
        <div v-if="recentApps.length" class="app-list">
          <div v-for="app in recentApps" :key="app.id" class="app-item">
            <div class="app-avatar" :style="{ background: getAvatarColor(app.userName) }">
              {{ app.userName?.charAt(0) }}
            </div>
            <div class="app-main">
              <div class="app-name">
                <span class="name">{{ app.userName }}</span>
                <span class="arrow">→</span>
                <span class="pet-name">{{ app.petName }}</span>
              </div>
              <div class="app-time">{{ app.createdAt || '-' }}</div>
            </div>
            <div class="app-status-wrap">
              <span class="app-status" :class="`status-${app.status}`">
                {{ app.status === 'pending' ? '待审核' : app.status === 'approved' ? '已通过' : '已拒绝' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无申请</div>
      </div>

      <div class="glass-card chart-side">
        <div class="card-head">
          <div class="head-text">
            <h3>申请审核状态</h3>
            <span>待审核 / 已通过 / 已拒绝</span>
          </div>
          <span class="head-icon">📋</span>
        </div>
        <div ref="statusChartRef" class="chart-container"></div>
        <div v-if="!chartHasStatusData" class="status-summary">暂无审核记录</div>
      </div>
    </div>

    <!-- 系统管理 -->
    <div class="glass-card system-card">
      <div class="card-head">
        <div class="head-text">
          <h3>系统管理</h3>
          <span>维护与管理工具</span>
        </div>
        <span class="dev-badge">仅开发时使用</span>
        <span class="head-icon">⚙</span>
      </div>
      <div class="system-actions">
        <button class="action-btn danger" :disabled="resetting" @click="handleReset">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="flex-shrink:0"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
          {{ resetting ? '重置中...' : '重置数据' }}
        </button>
        <button class="action-btn" :disabled="reloading" @click="handleReload">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="flex-shrink:0"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
          {{ reloading ? '加载中...' : '加载演示数据' }}
        </button>
        <button class="action-btn" :disabled="cleaning" @click="handleCleanup">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16" style="flex-shrink:0"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          {{ cleaning ? '清理中...' : '清理孤儿文件' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, h } from 'vue'
import { getDashboardStats } from '@/api/stats'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'
import * as echarts from 'echarts'

// 简单的 CountUp 数字递增组件
const CountUp = {
  props: ['end'],
  setup(props) {
    const display = ref(0)
    let timer
    nextTick(() => {
      const dur = 800
      const start = performance.now()
      function tick(now) {
        const p = Math.min((now - start) / dur, 1)
        const eased = 1 - Math.pow(1 - p, 3)
        display.value = Math.round((props.end || 0) * eased)
        if (p < 1) timer = requestAnimationFrame(tick)
      }
      timer = requestAnimationFrame(tick)
    })
    onUnmounted(() => cancelAnimationFrame(timer))
    return () => h('span', display.value)
  }
}

// 读取 CSS 变量（用于 ECharts 颜色绑定主题）
function cssVar(name, fallback = '#d4789e') {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

const loading = ref(false)
const resetting = ref(false)
const reloading = ref(false)
const cleaning = ref(false)
const statsCards = ref([
  { label: '宠物总数', value: 0, color: 'var(--text-primary)', gradient: 'linear-gradient(135deg, var(--brand-primary-light), var(--brand-primary))', glow: 'radial-gradient(circle, var(--shadow-primary-hover), transparent)', icon: '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="12" cy="8" r="3"/><path d="M6 21v-1a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v1"/>' },
  { label: '待领养', value: 0, color: '#378add', gradient: 'linear-gradient(135deg, #70b4ff, #378add)', glow: 'radial-gradient(circle, rgba(55,138,221,0.15), transparent)', icon: '<circle cx="12" cy="5" r="2"/><path d="M12 10c-3 0-6 2-6 6v2c0 1 1 2 2 2h8c1 0 2-1 2-2v-2c0-4-3-6-6-6z"/>' },
  { label: '已领养', value: 0, color: '#34c759', gradient: 'linear-gradient(135deg, #5dd879, #34c759)', glow: 'radial-gradient(circle, rgba(52,199,89,0.15), transparent)', icon: '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>' },
  { label: '注册用户', value: 0, color: '#ff9500', gradient: 'linear-gradient(135deg, #ffb84d, #ff9500)', glow: 'radial-gradient(circle, rgba(255,149,0,0.15), transparent)', icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>' },
  { label: '待审核', value: 0, color: '#ff5e8a', gradient: 'linear-gradient(135deg, #ff8aaa, #ff5e8a)', glow: 'radial-gradient(circle, rgba(255,94,138,0.18), transparent)', icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="15" y2="17"/>' }
])
const recentApps = ref([])
const chartHasStatusData = ref(true)

const trendChartRef = ref(null)
const categoryChartRef = ref(null)
const statusChartRef = ref(null)
let trendChart = null
let categoryChart = null
let statusChart = null

const chartColors = ['#2a2a2a', '#34c759', '#ff9500', '#5b9bd5', '#b088c9']
const statusColors = { pending: '#ff9500', approved: '#34c759', rejected: '#ff5e8a' }
const chartColorPairs = [
  { main: '#2a2a2a', light: '#5a5a6a' },
  { main: '#34c759', light: '#72b892' },
  { main: '#ff9500', light: '#ffb872' },
  { main: '#5b9bd5', light: '#85b6e0' },
  { main: '#b088c9', light: '#c8a8db' }
]

const avatarColors = ['var(--brand-primary)', '#34c759', '#ff9500', '#378add', 'var(--brand-primary-light)']
function getAvatarColor(name) {
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

async function loadStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    if (!res.data) return
    const s = res.data.summary || {}
    statsCards.value[0].value = s.petCount ?? 0
    statsCards.value[1].value = s.availableCount ?? 0
    statsCards.value[2].value = s.adoptedCount ?? 0
    statsCards.value[3].value = s.userCount ?? 0
    statsCards.value[4].value = res.data.statusDistribution?.pending ?? 0
    recentApps.value = res.data.recentApplications || []
    nextTick(() => {
      renderTrendChart(res.data.trendData || [])
      renderCategoryChart(res.data.categoryDistribution || [])
      renderStatusChart(res.data.statusDistribution || {})
    })
  } finally { loading.value = false }
}

function renderTrendChart(data) {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: 'var(--border-light)', textStyle: { color: 'var(--text-primary)' } },
    legend: { data: ['申请数', '通过数'], bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { color: 'var(--text-secondary)' } },
    grid: { left: 40, right: 16, top: 16, bottom: 36 },
    xAxis: { type: 'category', data: data.map(d => d.month), axisLine: { lineStyle: { color: 'var(--border-light)' } }, axisLabel: { fontSize: 11, color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0eaf6' } }, axisLabel: { fontSize: 11, color: 'var(--text-secondary)' } },
    series: [
      { name: '申请数', type: 'line', smooth: true, data: data.map(d => d.applyCount), lineStyle: { color: 'var(--brand-primary)', width: 2.5 }, itemStyle: { color: 'var(--brand-primary)' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: getComputedStyle(document.documentElement).getPropertyValue('--shadow-primary-30').trim() }, { offset: 1, color: 'rgba(255,255,255,0.02)' }]) }, symbol: 'circle', symbolSize: 6 },
      { name: '通过数', type: 'line', smooth: true, data: data.map(d => d.approvedCount), lineStyle: { color: '#34c759', width: 2.5 }, itemStyle: { color: '#34c759' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(52,199,89,0.3)' }, { offset: 1, color: 'rgba(52,199,89,0.02)' }]) }, symbol: 'circle', symbolSize: 6 }
    ]
  })
}

function renderCategoryChart(data) {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    animation: true,
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    animationDelay: (idx) => idx * 80,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(20,20,30,0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 12 },
      padding: [10, 14],
      formatter: (p) => `<b>${p.name}</b><br/>${p.value} 只 · ${p.percent}%`
    },
    legend: {
      bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8,
      textStyle: { color: 'var(--text-secondary)', fontSize: 12 },
      itemGap: 14
    },
    series: [{
      type: 'pie',
      radius: ['46%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2,
        shadowBlur: 12,
        shadowColor: 'rgba(0,0,0,0.06)'
      },
      label: {
        show: true,
        position: 'outside',
        formatter: '{b}\n{d}%',
        fontSize: 11,
        color: '#4a4a5a',
        lineHeight: 14
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 12,
        smooth: 0.2,
        lineStyle: { width: 1, color: 'rgba(0,0,0,0.15)' }
      },
      emphasis: {
        scale: true,
        scaleSize: 8,
        itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.18)' },
        label: { fontWeight: 600 }
      },
      animationType: 'expansion',
      animationDuration: 1200,
      data: data.map((d, i) => {
        const pair = chartColorPairs[i % chartColorPairs.length]
        return {
          name: d.name, value: d.count,
          itemStyle: {
            color: {
              type: 'linear', x: 0, y: 0, x2: 1, y2: 1,
              colorStops: [
                { offset: 0, color: pair.light },
                { offset: 1, color: pair.main }
              ]
            }
          }
        }
      })
    }]
  })
}

function renderStatusChart(data) {
  if (!statusChartRef.value) return
  const statusPairs = {
    pending: { main: '#ff9500', light: '#ffb872' },
    approved: { main: '#34c759', light: '#72b892' },
    rejected: { main: '#ff5e8a', light: '#ff8aaa' }
  }
  const items = [
    { name: '待审核', value: data.pending || 0, key: 'pending' },
    { name: '已通过', value: data.approved || 0, key: 'approved' },
    { name: '已拒绝', value: data.rejected || 0, key: 'rejected' }
  ].filter(i => i.value > 0)
  chartHasStatusData.value = items.length > 0
  if (!chartHasStatusData.value) return
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    animation: true,
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    animationDelay: (idx) => idx * 80,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(20,20,30,0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#fff', fontSize: 12 },
      padding: [10, 14],
      formatter: (p) => `<b>${p.name}</b><br/>${p.value} 单 · ${p.percent}%`
    },
    legend: {
      bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8,
      textStyle: { color: 'var(--text-secondary)', fontSize: 12 },
      itemGap: 14
    },
    series: [{
      type: 'pie',
      radius: ['46%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2,
        shadowBlur: 12,
        shadowColor: 'rgba(0,0,0,0.06)'
      },
      label: {
        show: true,
        position: 'outside',
        formatter: '{b}\n{d}%',
        fontSize: 11,
        color: '#4a4a5a',
        lineHeight: 14
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 12,
        smooth: 0.2,
        lineStyle: { width: 1, color: 'rgba(0,0,0,0.15)' }
      },
      emphasis: {
        scale: true,
        scaleSize: 8,
        itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.18)' },
        label: { fontWeight: 600 }
      },
      animationType: 'expansion',
      animationDuration: 1200,
      data: items.map(i => {
        const pair = statusPairs[i.key]
        return {
          name: i.name, value: i.value,
          itemStyle: {
            color: {
              type: 'linear', x: 0, y: 0, x2: 1, y2: 1,
              colorStops: [
                { offset: 0, color: pair.light },
                { offset: 1, color: pair.main }
              ]
            }
          }
        }
      })
    }]
  })
}

function handleResize() {
  trendChart?.resize(); categoryChart?.resize()
}

async function verifyPassword() {
  try {
    const { value } = await ElMessageBox.prompt(
      '<div style="color: var(--text-secondary); font-size: 12px; margin-top: 4px;">敏感操作需要确认管理员身份，请输入登录密码</div>',
      '身份验证',
      {
        confirmButtonText: '验证',
        cancelButtonText: '取消',
        type: 'warning',
        inputType: 'password',
        inputPlaceholder: '请输入管理员密码',
        inputValidator: (val) => (val && val.length >= 6) || '密码至少 6 位',
        dangerouslyUseHTMLString: true
      }
    )
    // 调用后端校验密码
    await request.post('/api/auth/verify-password', { password: value })
    return true
  } catch {
    return false
  }
}

async function handleReset() {
  if (!(await verifyPassword())) return
  try {
    await ElMessageBox.confirm(
      '确定要重置数据吗？此操作将清空所有宠物数据及业务数据。<br><b>不可恢复</b>。',
      '重置数据',
      { confirmButtonText: '确认重置', cancelButtonText: '取消', type: 'warning', dangerouslyUseHTMLString: true }
    )
    resetting.value = true
    await request.post('/api/admin/cleanup/reset-all')
    ElMessage.success('已清空所有数据')
    nextTick(() => loadStats())
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('重置失败')
  } finally {
    resetting.value = false
  }
}

async function handleReload() {
  if (!(await verifyPassword())) return
  try {
    await ElMessageBox.confirm(
      '将从 SQL 文件导入分类和宠物演示数据。<br>点击确定后选择 .sql 文件。',
      '加载演示数据',
      {
        confirmButtonText: '选择文件',
        cancelButtonText: '取消',
        type: 'info',
        dangerouslyUseHTMLString: true
      }
    ).catch(() => null)
    // 弹出文件选择器
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '.sql'
    input.onchange = async (e) => {
      const file = e.target.files[0]
      if (!file) return
      reloading.value = true
      try {
        const form = new FormData()
        form.append('file', file)
        await request.post('/api/admin/cleanup/reload-demo', form, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        ElMessage.success('演示数据已导入')
        nextTick(() => loadStats())
      } catch (err) {
        ElMessage.error('导入失败: ' + (err.message || '未知错误'))
      } finally {
        reloading.value = false
      }
    }
    input.click()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('加载失败')
  }
}

async function handleCleanup() {
  if (!(await verifyPassword())) return
  cleaning.value = true
  try {
    // 第一步：预演。只看清单不真删。
    // 后端在这里还有一道安全阀：一条被引用图片都查不到时直接跳过，
    // 避免连错库之后把整个 upload 目录当"孤儿"清空。
    const preview = await request.post('/api/admin/cleanup/orphans?dryRun=true')
    const d = preview.data || {}
    if (!d.deletedCount) {
      ElMessage.success(preview.message || '没有发现孤儿文件')
      return
    }
    const files = d.files || []
    const shown = files.slice(0, 10).map(f => `· ${f}`).join('<br>')
    const more = files.length > 10 ? `<br>… 另有 ${files.length - 10} 个` : ''
    await ElMessageBox.confirm(
      `将删除 <b>${d.deletedCount}</b> 个未被数据库引用的文件，共 <b>${d.freedDisplay}</b>。<br><br>` +
      `<div style="max-height: 220px; overflow: auto; font-size: 12px;">${shown}${more}</div>`,
      '确认清理孤儿文件',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning', dangerouslyUseHTMLString: true }
    )
    // 第二步：确认后才真正删除
    const done = await request.post('/api/admin/cleanup/orphans')
    ElMessage.success(`清理完成，删除了 ${done.data?.deletedCount || 0} 个文件，释放 ${done.data?.freedDisplay || '0 B'}`)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清理失败')
  } finally {
    cleaning.value = false
  }
}

onMounted(() => { loadStats(); window.addEventListener('resize', handleResize) })
onUnmounted(() => {
  trendChart?.dispose(); categoryChart?.dispose(); statusChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: 16px; }

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.stat-block {
  position: relative;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 18px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 16px rgba(120, 90, 200, 0.05);
  opacity: 0;
  transform: translateY(20px);
  animation: statIn 0.5s ease forwards;
  transition: all 0.3s ease;
  overflow: hidden;
}
.stat-block:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px var(--shadow-primary);
  background: rgba(255, 255, 255, 0.7);
}
@keyframes statIn {
  to { opacity: 1; transform: translateY(0); }
}

.stat-icon {
  width: 44px; height: 44px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-content { flex: 1; min-width: 0; }
.stat-number {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  font-weight: 500;
}

.stat-glow {
  position: absolute;
  bottom: -20px; right: -20px;
  width: 80px; height: 80px;
  border-radius: 50%;
  opacity: 0.6;
  pointer-events: none;
}

/* ===== Glass Card ===== */
.glass-card {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 6px 20px var(--surface-primary);
  animation: cardIn 0.5s ease 0.2s both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 图表网格: 8/4 分栏 ===== */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}
.charts-grid-compact { grid-template-columns: 1fr 1.2fr; }
.chart-main { display: flex; flex-direction: column; }
.chart-side { display: flex; flex-direction: column; }

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--border-light);
}
.head-text h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px;
  letter-spacing: -0.01em;
}
.head-text span {
  font-size: 11px;
  color: var(--text-tertiary);
  letter-spacing: 0.05em;
}
.head-icon {
  width: 40px; height: 40px;
  background: var(--surface-primary);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}

.dev-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
  background: #fef3c7;
  border: 1px solid #fde68a;
  padding: 3px 10px;
  border-radius: 10px;
  letter-spacing: 0.2px;
  align-self: center;
  white-space: nowrap;
}
.dev-badge::before {
  content: '';
  width: 6px; height: 6px;
  background: #d97706;
  border-radius: 50%;
  display: inline-block;
}

.chart-container { width: 100%; height: 280px; }
.status-summary {
  text-align: center;
  padding: 60px 0 40px;
  color: var(--text-tertiary);
  font-size: 13px;
}

/* ===== 最近申请列表 ===== */
.app-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.app-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 0.5px solid var(--border-light);
  transition: all 0.2s;
  position: relative;
  overflow: hidden;
}
.app-item::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: var(--brand-primary);
  opacity: 0.6;
  transition: opacity 0.2s;
}
.app-item:hover { background: #fff; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.app-item:hover::before { opacity: 1; }

.app-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}
.app-main { flex: 1; min-width: 0; }
.app-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.app-name .name { font-weight: 600; }
.app-name .arrow { color: var(--text-tertiary); font-size: 12px; }
.app-name .pet-name {
  color: var(--brand-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
  font-variant-numeric: tabular-nums;
}

.app-status-wrap { flex-shrink: 0; }
.app-status {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
}
.app-status::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}
.status-pending { background: rgba(255, 149, 0, 0.1); color: #ff9500; }
.status-approved { background: rgba(52, 199, 89, 0.1); color: #34c759; }
.status-rejected { background: rgba(255, 94, 138, 0.1); color: #ff5e8a; }

.empty-tip {
  text-align: center;
  padding: 50px 0;
  color: var(--text-tertiary);
  font-size: 13px;
}

/* ===== System ===== */
.system-card { margin-top: 4px; }
.system-actions {
  display: flex;
  gap: 12px;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--border-light);
  cursor: pointer;
  font-family: inherit;
  background: rgba(255, 255, 255, 0.6);
  color: var(--text-primary);
  transition: all 0.2s;
  justify-content: center;
}
.action-btn:hover:not(:disabled) {
  background: var(--surface-primary-strong);
  border-color: var(--brand-primary);
  color: var(--brand-primary);
}
.action-btn.danger {
  background: rgba(255, 94, 138, 0.06);
  color: #ff5e8a;
  border-color: rgba(255, 94, 138, 0.2);
}
.action-btn.danger:hover:not(:disabled) {
  background: #ff5e8a;
  color: #fff;
  border-color: #ff5e8a;
}
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}
</style>
