<template>
  <header class="site-header">
    <div ref="headerPanel" class="header-panel glass-panel">
      <router-link class="brand" to="/" aria-label="大叔的文字与光影首页">
        <span class="brand-mark">文</span>
        <span class="brand-copy">
          <strong>大叔的文字与光影</strong>
          <small>WORDS · LIGHT · JOURNEYS</small>
        </span>
      </router-link>

      <nav class="desktop-nav" aria-label="主导航">
        <router-link
          v-for="link in navLinks"
          :key="link.path"
          :to="link.path"
          :class="['nav-link', { active: isActive(link.path) }]"
        >
          {{ link.label }}
        </router-link>
      </nav>

      <div class="header-auth">
        <template v-if="auth.authenticated">
          <router-link class="auth-link" to="/profile">
            <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" class="auth-avatar" alt="头像" />
            <span v-else class="auth-avatar auth-avatar-fallback">{{ initial }}</span>
            <span class="auth-name">{{ auth.user?.displayName || auth.user?.username }}</span>
          </router-link>
          <button class="auth-link auth-logout" type="button" @click="logout">退出</button>
        </template>
        <template v-else>
          <router-link class="auth-link" to="/login">登录</router-link>
          <router-link class="auth-link auth-register" to="/register">注册</router-link>
        </template>
      </div>

      <button
        class="icon-button mobile-menu-button"
        type="button"
        :aria-expanded="mobileOpen"
        aria-label="菜单"
        @click="mobileOpen = !mobileOpen"
      >
        <X v-if="mobileOpen" :size="23" />
        <Menu v-else :size="23" />
      </button>

      <transition name="menu">
        <nav v-if="mobileOpen" class="mobile-nav glass-panel" aria-label="移动端主导航">
          <router-link
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            :class="['mobile-nav-link', { active: isActive(link.path) }]"
          >
            {{ link.label }}
          </router-link>
          <template v-if="auth.authenticated">
            <router-link class="mobile-nav-link" to="/profile">个人中心</router-link>
            <button class="mobile-nav-link" type="button" @click="logout">退出登录</button>
          </template>
          <template v-else>
            <router-link class="mobile-nav-link" to="/login">登录</router-link>
            <router-link class="mobile-nav-link" to="/register">注册</router-link>
          </template>
        </nav>
      </transition>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Menu, X } from '@lucide/vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)
const headerPanel = ref(null)

const initial = computed(() => (auth.user?.displayName || auth.user?.username || '文').slice(0, 1))

const navLinks = [
  { path: '/', label: '首页' },
  { path: '/articles', label: '文章' },
  { path: '/gallery', label: '光影集' },
  { path: '/archive', label: '时光' },
  { path: '/about', label: '关于' },
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  if (path === '/articles') return route.path.startsWith('/article')
  return route.path.startsWith(path)
}

function onPointerDown(event) {
  if (mobileOpen.value && !headerPanel.value?.contains(event.target)) mobileOpen.value = false
}

function onKeyDown(event) {
  if (event.key === 'Escape') mobileOpen.value = false
}

async function logout() {
  await auth.logout()
  mobileOpen.value = false
  router.push('/')
}

watch(() => route.fullPath, () => { mobileOpen.value = false })

onMounted(() => {
  document.addEventListener('pointerdown', onPointerDown)
  document.addEventListener('keydown', onKeyDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onPointerDown)
  document.removeEventListener('keydown', onKeyDown)
})
</script>
