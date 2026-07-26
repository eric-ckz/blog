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
        </nav>
      </transition>
    </div>
  </header>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Menu, X } from '@lucide/vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const mobileOpen = ref(false)
const headerPanel = ref(null)

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
