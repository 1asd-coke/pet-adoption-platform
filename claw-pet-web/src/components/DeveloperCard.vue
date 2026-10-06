<template>
  <div class="dev-card" :class="{ 'is-collapsed': collapsed }">
    <!-- 收起状态只剩这个头像，点一下就展开 -->
    <button class="dev-head" type="button" :title="collapsed ? '查看开发者信息' : '收起'" @click="collapsed = !collapsed">
      <img v-if="dev.avatar" :src="dev.avatar" :alt="dev.name" class="dev-avatar" />
      <span v-else class="dev-avatar dev-avatar-text">{{ dev.name.slice(0, 1) }}</span>
      <span class="dev-head-text">
        <b>{{ dev.name }}</b>
        <em>{{ dev.desc }}</em>
      </span>
      <svg class="dev-chevron" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>

    <div v-show="!collapsed" class="dev-body">
      <ul class="dev-list">
        <li v-for="row in visibleItems" :key="row.label">
          <span class="dev-label">{{ row.label }}</span>
          <a v-if="row.href" class="dev-value dev-link" :href="row.href" target="_blank" rel="noopener noreferrer">{{ row.value }}</a>
          <span v-else class="dev-value">{{ row.value }}</span>
        </li>
      </ul>
      <p class="dev-foot">如有问题，欢迎联系作者</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { developer } from '@/config/developer'

/**
 * 开发者信息卡片（登录页右上角）
 *
 * ⚠️ 名片内容不在这里改，去 src/config/developer.js —— 只改那一个文件。
 *    这里只负责渲染。
 */
const dev = reactive(developer)

/**
 * 默认收起，不是偷懒 —— 是实测出来的：
 * 登录态下品牌面板滑到右边，展开的 232×130 卡片会和插画、正文重叠
 * （实测 .dev-card 1120~1352 × 20~150，而 .content-inner 从 y=126 开始）。
 * 收起后卡片只有 20~70 高，正好在正文上方，两种状态都不挡。
 */
const collapsed = ref(true)

// 没填的项（占位里带「填」）自动隐藏，避免半成品露出来
const visibleItems = computed(() =>
  dev.items.filter(it => it.value && !String(it.value).includes('填'))
)
</script>

<style scoped>
.dev-card {
  position: fixed;
  /* 右上角，让开已有的主题切换按钮（它在 right:20、宽 40） */
  top: 20px;
  right: 72px;
  z-index: 99;
  width: 232px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 28px var(--shadow-primary);
  color: var(--text-primary);
  font-size: 12px;
  transition: width 0.25s ease, padding 0.25s ease;
}

/* 收起时只剩头像那一小块，不挡页面 */
.dev-card.is-collapsed {
  width: auto;
  padding: 8px 10px;
}

.dev-head {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
}

.dev-avatar {
  flex: none;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  border: 1.5px solid var(--brand-primary);
}

.dev-avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--brand-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.dev-head-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.dev-head-text b {
  font-size: 12px;
  font-weight: 600;
  color: var(--brand-primary);
  line-height: 1.4;
}

.dev-head-text em {
  font-style: normal;
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dev-chevron {
  flex: none;
  color: var(--text-secondary);
  transition: transform 0.25s ease;
}

.dev-card.is-collapsed .dev-chevron {
  transform: rotate(-90deg);
}

.dev-body {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.dev-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.dev-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0;
}

.dev-label {
  flex: none;
  width: 40px;
  color: var(--text-secondary);
  font-size: 11px;
}

.dev-value {
  flex: 1;
  min-width: 0;
  font-size: 11px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dev-link {
  color: var(--brand-primary);
  text-decoration: none;
}

.dev-link:hover {
  text-decoration: underline;
}

.dev-foot {
  margin: 8px 0 0;
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
  letter-spacing: 0.5px;
}
</style>
