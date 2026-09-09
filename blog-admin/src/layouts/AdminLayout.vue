<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  LayoutDashboard, FileText, Tags, Images, SlidersHorizontal,
  UserRound, LogOut, Menu, X, BookOpen, MessageSquare,
} from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)
// 生产地址由构建变量注入，避免管理端部署后仍跳回开发机 localhost。
const blogWebUrl = import.meta.env.VITE_BLOG_WEB_URL || 'http://localhost:3000'

const menuItems = [
  { to: '/', label: '工作台', icon: LayoutDashboard },
  { to: '/articles', label: '文章管理', icon: FileText },
  { to: '/categories', label: '栏目管理', icon: Tags },
  { to: '/media', label: '媒体库', icon: Images },
  { to: '/comments', label: '评论管理', icon: MessageSquare },
  { to: '/site', label: '首页配置', icon: SlidersHorizontal },
  { to: '/about', label: '关于页配置', icon: BookOpen },
  { to: '/account', label: '账号设置', icon: UserRound },
]

const pageTitle = computed(() => route.meta.title || '管理后台')

async function logout() {
  await auth.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="admin-shell">
    <div v-if="mobileOpen" class="mobile-mask" @click="mobileOpen = false" />
    <aside class="admin-sidebar" :class="{ 'is-open': mobileOpen }">
      <div class="admin-brand">
        <div class="brand-mark">C</div>
        <div>
          <strong>Charles Blog</strong>
          <span>CONTENT STUDIO</span>
        </div>
        <button class="sidebar-close" aria-label="关闭菜单" @click="mobileOpen = false"><X :size="20" /></button>
      </div>
      <nav class="admin-nav">
        <router-link
          v-for="item in menuItems" :key="item.to" :to="item.to"
          :class="{ active: item.to === '/' ? route.path === '/' : route.path.startsWith(item.to) }"
          @click="mobileOpen = false"
        >
          <component :is="item.icon" :size="19" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-user">
        <div class="user-avatar">{{ auth.user?.displayName?.slice(0, 1) || '管' }}</div>
        <div class="user-copy"><strong>{{ auth.user?.displayName }}</strong><span>{{ auth.user?.username }}</span></div>
        <button aria-label="退出登录" @click="logout"><LogOut :size="18" /></button>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar">
        <button class="mobile-menu" aria-label="打开菜单" @click="mobileOpen = true"><Menu :size="21" /></button>
        <div><span class="eyebrow">CHARLES BLOG</span><h1>{{ pageTitle }}</h1></div>
        <a :href="blogWebUrl" target="_blank" rel="noreferrer">查看网站</a>
      </header>
      <div class="admin-content"><router-view /></div>
    </main>
  </div>
</template>
