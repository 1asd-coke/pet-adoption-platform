<template>
  <div class="pet-detail-page" v-loading="loading">
    <div class="page-container" v-if="pet">
      <section class="hero-section">
        <div class="hero-grid">
          <PetImageCarousel :images="petImages" :pet-name="pet?.name" />
          <PetInfoCard :pet="pet" :is-favorited="isFavorited" @toggle-favorite="toggleFavorite">
            <template #actions>
              <button
                v-if="!userStore.isAdmin"
                class="adopt-btn"
                :disabled="pet.status && pet.status !== 'available'"
                @click="openAdoptDialog"
              >
                <span>{{ !pet.status || pet.status === 'available' ? '立即申请领养' : statusText }}</span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
              </button>
              <div v-else class="admin-tip">管理员账号无法申请领养</div>
            </template>
          </PetInfoCard>
        </div>
      </section>

      <CommentSection
        :pet-id="pet.id"
        :comments="comments"
        :loading="commentLoading"
        :is-logged-in="userStore.isLoggedIn"
        :is-admin="userStore.isAdmin"
        :current-user-id="userStore.userInfo?.id"
        @load-comments="loadComments"
        @submit-comment="submitComment"
        @delete-comment="removeComment"
      />
    </div>

    <AdoptFormDialog
      v-model:visible="showAdoptDialog"
      :pet-id="pet?.id"
      :pet-name="pet?.name || ''"
      :loading="adoptLoading"
      :initial-form="adoptInitialForm"
      @submit="submitAdopt"
    >
      <template #pet-image>
        <img v-if="currentImage" :src="currentImage" />
      </template>
    </AdoptFormDialog>

    <LoginPrompt
      v-model:visible="showLoginPrompt"
      :title="loginPromptMsg.title"
      :message="loginPromptMsg.message"
      :confirm-text="loginPromptMsg.confirmText"
      @confirm="loginPromptMsg.onConfirm && loginPromptMsg.onConfirm()"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPet } from '@/api/pet'
import { applyAdoption } from '@/api/adopt'
import { listComments, addComment, deleteComment } from '@/api/comment'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import PetImageCarousel from '@/components/pet/PetImageCarousel.vue'
import PetInfoCard from '@/components/pet/PetInfoCard.vue'
import CommentSection from '@/components/pet/CommentSection.vue'
import AdoptFormDialog from '@/components/pet/AdoptFormDialog.vue'
import LoginPrompt from '@/components/LoginPrompt.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const pet = ref(null)
const currentImage = ref('')
const isFavorited = ref(false)
const showAdoptDialog = ref(false)
const adoptLoading = ref(false)
const comments = ref([])
const commentLoading = ref(false)
const showLoginPrompt = ref(false)
const loginPromptMsg = ref({ title: '', message: '', onConfirm: null })

const adoptInitialForm = computed(() => {
  const user = userStore.userInfo
  return { phone: user?.phone || '', address: user?.address || '', reason: '' }
})

const statusMap = { available: '待领养', adopting: '领养中', adopted: '已领养', offline: '已下架' }
const statusText = computed(() => {
  const s = pet.value?.status
  if (!s) return '待领养'
  return statusMap[s] || '待领养'
})

const petImages = computed(() => {
  if (!pet.value) return []
  const imgs = []
  if (pet.value.imageUrls?.length) imgs.push(...pet.value.imageUrls)
  else if (pet.value.cover) imgs.push(pet.value.cover)
  return imgs.length ? imgs : ['https://via.placeholder.com/400x300?text=No+Image']
})

async function loadPet() {
  loading.value = true
  try {
    const res = await getPet(route.params.id)
    pet.value = res.data
    currentImage.value = petImages.value[0]
  } finally { loading.value = false }
}

async function loadFavStatus() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await checkFavorite(route.params.id)
    isFavorited.value = res.data ?? false
  } catch {}
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    promptLogin({ title: '请先登录', message: '收藏功能需要登录账号，是否先去登录？', confirmText: '去登录', onConfirm: () => router.push('/login') })
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(route.params.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch {}
}

function promptLogin({ title, message, confirmText, onConfirm }) {
  loginPromptMsg.value = { title, message, confirmText, onConfirm }
  showLoginPrompt.value = true
}

function openAdoptDialog() {
  if (!userStore.isLoggedIn) {
    promptLogin({ title: '请先登录', message: '领养申请需要登录账号，是否先去登录？', confirmText: '去登录', onConfirm: () => router.push('/login') })
    return
  }
  const user = userStore.userInfo
  if (!user?.phone && !user?.address) {
    promptLogin({ title: '信息不完整', message: '领养需要填写联系电话和地址，是否先去个人中心完善资料？', confirmText: '去完善', onConfirm: () => router.push('/profile') })
  } else {
    showAdoptDialog.value = true
  }
}

async function submitAdopt(data) {
  adoptLoading.value = true
  try {
    await applyAdoption({ petId: pet.value.id, ...data })
    ElMessage.success('申请提交成功，请等待审核')
    showAdoptDialog.value = false
  } finally { adoptLoading.value = false }
}

async function loadComments() {
  commentLoading.value = true
  try {
    const res = await listComments(route.params.id, { page: 1, size: 100 })
    comments.value = res.data?.records || res.data || []
  } finally { commentLoading.value = false }
}

async function submitComment(data) {
  try {
    await addComment({ petId: Number(route.params.id), ...data })
    ElMessage.success(data.parentId ? '回复成功' : '评论成功')
    loadComments()
  } catch {}
}

async function removeComment(id) {
  try {
    await deleteComment(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch {}
}

onMounted(() => {
  loadPet()
  loadFavStatus()
  loadComments().then(() => {
    const hash = route.hash
    if (hash && hash.startsWith('#comment-')) {
      nextTick(() => {
        const target = document.querySelector(hash)
        if (target) target.scrollIntoView({ behavior: 'smooth', block: 'center' })
      })
    }
  })
})
</script>

<style scoped>
.pet-detail-page {
  min-height: 100vh;
  background: rgba(255, 255, 255, 0.3);
}
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 60px;
}
.hero-section { margin-bottom: 32px; }
.hero-grid {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 24px;
  align-items: start;
}
.adopt-btn {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%;
  background: var(--brand-primary);
  color: #fff;
  border: none;
  padding: 14px 24px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.05em;
  cursor: pointer;
  box-shadow: 0 4px 16px var(--shadow-primary-30);
  transition: all 0.25s ease;
}
.adopt-btn:hover:not(:disabled) {
  background: var(--brand-primary-active);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--shadow-primary-40);
}
.adopt-btn svg { width: 18px; height: 18px; transition: transform 0.25s ease; }
.adopt-btn:hover:not(:disabled) svg { transform: translateX(4px); }
.adopt-btn:disabled {
  background: #d4d4dc;
  cursor: not-allowed;
  box-shadow: none;
}
.admin-tip {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
  padding: 14px;
  background: rgba(120, 120, 120, 0.06);
  border-radius: 14px;
}
@media (max-width: 768px) {
  .hero-grid { grid-template-columns: 1fr; gap: 16px; }
}
</style>
