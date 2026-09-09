<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Coffee, LockKeyhole, UserRound } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('欢迎回来')
    router.replace(route.query.redirect || '/')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <div class="login-story">
        <div class="login-logo"><Coffee :size="25" /></div>
        <p class="eyebrow">A PERSONAL JOURNAL SINCE 2020</p>
        <h1>让每一篇文字<br />都有被认真安放的位置</h1>
        <p>Eric Blog 内容管理后台</p>
        <blockquote>认真生活，诚实记录。</blockquote>
      </div>
      <div class="login-form">
        <div><span class="eyebrow">WELCOME BACK</span><h2>登录管理后台</h2><p>使用初始化管理员账号继续</p></div>
        <el-form label-position="top" @submit.prevent="submit">
          <el-form-item label="管理员账号">
            <el-input v-model="form.username" size="large" autocomplete="username" placeholder="请输入账号">
              <template #prefix><UserRound :size="17" /></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" size="large" type="password" show-password autocomplete="current-password" placeholder="请输入密码" @keyup.enter="submit">
              <template #prefix><LockKeyhole :size="17" /></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="submit">进入内容工作室</el-button>
        </el-form>
        <small>Access Token 仅保存在当前页面内存中</small>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:radial-gradient(circle at 15% 15%,rgba(129,139,255,.24),transparent 30rem),radial-gradient(circle at 90% 90%,rgba(112,211,187,.18),transparent 30rem),#f2f4f9}.login-card{width:min(920px,100%);min-height:570px;display:grid;grid-template-columns:1.05fr .95fr;overflow:hidden;border:1px solid rgba(39,45,68,.09);border-radius:28px;background:rgba(255,255,255,.85);box-shadow:0 35px 90px rgba(31,37,66,.17)}.login-story{position:relative;padding:64px;color:#edf0ff;background:linear-gradient(150deg,#22283f,#34375b 55%,#5354a0)}.login-story:after{content:"";position:absolute;right:-80px;bottom:-110px;width:300px;height:300px;border:1px solid rgba(255,255,255,.12);border-radius:50%}.login-logo{width:52px;height:52px;display:grid;place-items:center;margin-bottom:58px;border-radius:17px;background:rgba(255,255,255,.13)}.login-story h1{margin:12px 0 20px;font:500 36px/1.42 Georgia,"Songti SC",serif}.login-story>p:not(.eyebrow){color:#aeb4cc}.login-story blockquote{position:absolute;left:64px;bottom:55px;margin:0;color:#aeb4cc;font:italic 14px Georgia,serif}.login-form{padding:68px 58px;display:flex;flex-direction:column;justify-content:center}.login-form h2{margin:8px 0 7px;font:600 29px Georgia,"Songti SC",serif}.login-form>div>p{margin:0 0 32px;color:#8a8fa0;font-size:13px}.login-form small{display:block;margin-top:22px;color:#9ba0af;text-align:center}@media(max-width:760px){.login-card{grid-template-columns:1fr}.login-story{display:none}.login-form{padding:48px 28px;min-height:520px}}
</style>
