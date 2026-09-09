import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/articles',
    name: 'Articles',
    meta: { title: '全部文章' },
    component: () => import('@/views/ArticlesView.vue'),
  },
  {
    path: '/gallery',
    name: 'Gallery',
    meta: { title: '光影集' },
    component: () => import('@/views/GalleryView.vue'),
  },
  {
    path: '/archive',
    name: 'Archive',
    meta: { title: '时光档案' },
    component: () => import('@/views/ArchiveView.vue'),
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetailView.vue'),
  },
  {
    path: '/about',
    name: 'About',
    meta: { title: '关于我' },
    component: () => import('@/views/AboutView.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    meta: { title: '登录' },
    component: () => import('@/views/LoginView.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    meta: { title: '注册' },
    component: () => import('@/views/RegisterView.vue'),
  },
  {
    path: '/profile',
    name: 'Profile',
    meta: { title: '个人中心' },
    component: () => import('@/views/ProfileView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router

router.afterEach((to) => {
  document.title = `${to.meta.title || '首页'} · 大叔的文字与光影`
})
