import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  // 使用 Vite 注入的 BASE_URL，生产环境刷新 /admin/articles 等路由时仍能正确匹配。
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '工作台' } },
        { path: 'articles', name: 'articles', component: () => import('@/views/ArticlesView.vue'), meta: { title: '文章管理' } },
        { path: 'articles/new', name: 'article-new', component: () => import('@/views/ArticleEditView.vue'), meta: { title: '新建文章' } },
        { path: 'articles/:id/edit', name: 'article-edit', component: () => import('@/views/ArticleEditView.vue'), meta: { title: '编辑文章' } },
        { path: 'categories', name: 'categories', component: () => import('@/views/CategoriesView.vue'), meta: { title: '栏目管理' } },
        { path: 'media', name: 'media', component: () => import('@/views/MediaView.vue'), meta: { title: '媒体库' } },
        { path: 'comments', name: 'comments', component: () => import('@/views/CommentsView.vue'), meta: { title: '评论管理' } },
        { path: 'site', name: 'site', component: () => import('@/views/SiteSettingsView.vue'), meta: { title: '首页配置' } },
        { path: 'about', name: 'about', component: () => import('@/views/AboutSettingsView.vue'), meta: { title: '关于页配置' } },
        { path: 'account', name: 'account', component: () => import('@/views/AccountView.vue'), meta: { title: '账号设置' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

/**
 * 受保护页面优先尝试 Refresh Cookie 恢复会话，失败才跳转登录页。
 * 登录页不主动刷新：首次访问本就没有 Cookie，无意义的 401 会污染浏览器控制台。
 */
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    if (to.name === 'login' && auth.authenticated) return { name: 'dashboard' }
    return true
  }
  return await auth.ensureSession() ? true : { name: 'login', query: { redirect: to.fullPath } }
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '管理后台'} · Charles Blog`
})

export default router
