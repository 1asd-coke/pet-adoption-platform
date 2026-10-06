<template>
  <section class="comments-section">
    <div class="comments-head">
      <h2>留言评论</h2>
      <span class="comment-count">{{ comments.length }} 条</span>
    </div>

    <div class="comment-input" v-if="isLoggedIn">
      <div v-if="replyToId" class="reply-hint">
        回复 <b>@{{ replyToName }}</b>
        <button class="reply-cancel" @click="cancelReply">取消</button>
      </div>
      <textarea
        v-model="newComment"
        :placeholder="replyToId ? '输入回复内容...' : '说点什么吧...'"
        class="comment-textarea"
        rows="3"
      ></textarea>
      <div class="comment-actions">
        <button class="submit-btn" @click="handleSubmit">{{ replyToId ? '回复' : '发表评论' }}</button>
      </div>
    </div>
    <div v-else class="comment-login-tip">
      请<router-link to="/login">登录</router-link>后发表评论
    </div>

    <div class="comments-list" v-loading="loading">
      <div v-for="comment in comments" :key="comment.id" class="comment-item" :id="'comment-' + comment.id">
        <div class="comment-bubble">
          <div class="avatar">
            <img v-if="comment.userAvatar" :src="comment.userAvatar" class="avatar-img" />
            <span v-else :style="{ background: getAvatarColor(comment.userName || comment.username) }">{{ (comment.userName || comment.username || 'U').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="comment-body">
            <div class="comment-meta-row">
              <span class="comment-user">{{ comment.userName || comment.username }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-ops">
              <button v-if="isLoggedIn" class="op-link" @click="startReply(comment)">回复</button>
              <button
                v-if="isAdmin || currentUserId === comment.userId"
                class="op-link op-danger"
                @click="$emit('delete-comment', comment.id)"
              >删除</button>
            </div>
          </div>
        </div>
        <div v-if="comment.replies?.length" class="comment-replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="comment-item reply-item" :id="'comment-' + reply.id">
            <div class="comment-bubble">
              <div class="avatar avatar-sm">
                <img v-if="reply.userAvatar" :src="reply.userAvatar" class="avatar-img" />
                <span v-else :style="{ background: getAvatarColor(reply.userName || reply.username) }">{{ (reply.userName || reply.username || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <div class="comment-body">
                <div class="comment-meta-row">
                  <span class="comment-user">{{ reply.userName || reply.username }}</span>
                  <span v-if="reply.replyToUserName" class="reply-to-tag">@{{ reply.replyToUserName }}</span>
                  <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="comment-text">{{ reply.content }}</div>
                <div class="comment-ops">
                  <button v-if="isLoggedIn" class="op-link" @click="startReply(comment, reply)">回复</button>
                  <button
                    v-if="isAdmin || currentUserId === reply.userId"
                    class="op-link op-danger"
                    @click="$emit('delete-comment', reply.id)"
                  >删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && !comments.length" class="empty-tip">暂无评论</div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  petId: { type: [Number, String], required: true },
  comments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  isLoggedIn: { type: Boolean, default: false },
  isAdmin: { type: Boolean, default: false },
  currentUserId: { type: [Number, String], default: null }
})

const emit = defineEmits(['load-comments', 'submit-comment', 'delete-comment'])

const newComment = ref('')
const replyToId = ref(null)
const replyToName = ref('')
const replyToUserId = ref(null)

function startReply(comment, reply) {
  const target = reply || comment
  replyToId.value = comment.id
  replyToName.value = target.userName || target.username
  replyToUserId.value = target.userId
}

function cancelReply() {
  replyToId.value = null
  replyToName.value = ''
  replyToUserId.value = null
  newComment.value = ''
}

function handleSubmit() {
  if (!newComment.value.trim()) return
  emit('submit-comment', {
    content: newComment.value.trim(),
    parentId: replyToId.value || 0,
    replyToUserId: replyToUserId.value || null
  })
  newComment.value = ''
  cancelReply()
}

const avatarColors = ['var(--brand-primary)', '#34c759', '#ff9500', 'var(--brand-primary-light)', '#378add', '#993c1d']
function getAvatarColor(name) {
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.comments-section {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px var(--surface-primary);
  animation: slideInUp 0.5s ease 0.2s both;
}
.comments-head {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 24px;
}
.comments-head h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}
.comment-count {
  font-size: 13px;
  color: var(--text-secondary);
  background: #f0eaf6;
  padding: 3px 10px;
  border-radius: 8px;
}

.comment-input { margin-bottom: 28px; }
.reply-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #f0eaf6;
  border-radius: 8px;
  display: flex; align-items: center; gap: 8px;
}
.reply-cancel {
  margin-left: auto;
  border: none; background: none; color: var(--text-secondary); cursor: pointer;
  font-size: 12px;
}
.comment-textarea {
  width: 100%;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  font-family: inherit;
  color: var(--text-primary);
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}
.comment-textarea:focus { border-color: var(--brand-primary); }
.comment-actions { display: flex; justify-content: flex-end; margin-top: 12px; }
.submit-btn {
  background: var(--brand-primary); color: #fff;
  border: none;
  padding: 8px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.submit-btn:hover { background: #6b4eb5; }
.comment-login-tip { text-align: center; padding: 18px; color: var(--text-secondary); }
.comment-login-tip a { color: var(--brand-primary); font-weight: 500; }

.comments-list { display: flex; flex-direction: column; gap: 18px; }
.comment-bubble {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}
.avatar-sm { width: 32px; height: 32px; font-size: 13px; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
.comment-body { flex: 1; }
.comment-meta-row {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 4px;
}
.comment-user {
  font-size: 14px; font-weight: 600; color: var(--text-primary);
}
.comment-time {
  font-size: 12px; color: #aeaeb2;
}
.comment-text {
  font-size: 14px; color: #4a4a5a;
  line-height: 1.7;
}
.comment-ops {
  display: flex; gap: 12px;
  margin-top: 6px;
}
.op-link {
  background: none; border: none;
  color: var(--text-secondary); font-size: 12px;
  cursor: pointer; padding: 0;
  transition: color 0.2s;
}
.op-link:hover { color: var(--brand-primary); }
.op-link.op-danger:hover { color: #ff5e8a; }

.comment-replies {
  margin-left: 52px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-light);
}
.reply-item { margin-top: 12px; }
.reply-to-tag {
  font-size: 12px;
  color: var(--brand-primary);
  font-weight: 500;
}

.empty-tip { text-align: center; padding: 40px; color: #aeaeb2; font-size: 14px; }

@keyframes slideInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>
