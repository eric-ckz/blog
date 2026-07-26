<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { authApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const saving = ref(false)
const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

async function submit() {
  if (form.newPassword !== form.confirmPassword) return ElMessage.warning('两次输入的新密码不一致')
  saving.value = true
  try { await authApi.changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword }); ElMessage.success('密码已修改，请重新登录'); await auth.logout(); router.replace('/login') } catch (error) { ElMessage.error(error.message) } finally { saving.value = false }
}
</script>

<template>
  <PageHeader title="账号设置" description="修改密码后，所有设备上的 Refresh Token 都会失效" />
  <div class="account-grid"><section class="panel profile-panel"><div class="large-avatar">{{ auth.user?.displayName?.slice(0,1) }}</div><h3>{{ auth.user?.displayName }}</h3><p>{{ auth.user?.username }}</p><el-tag type="success">唯一管理员</el-tag></section><section class="panel"><h3>修改密码</h3><el-form label-position="top" style="max-width:460px"><el-form-item label="当前密码"><el-input v-model="form.currentPassword" type="password" show-password /></el-form-item><el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password placeholder="至少 10 位" /></el-form-item><el-form-item label="确认新密码"><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item><el-button type="primary" :loading="saving" @click="submit">修改密码</el-button></el-form></section></div>
</template>

<style scoped>.account-grid{display:grid;grid-template-columns:280px 1fr;gap:18px}.panel h3{margin:0 0 22px;font:600 18px Georgia,"Songti SC",serif}.profile-panel{text-align:center}.large-avatar{width:78px;height:78px;display:grid;place-items:center;margin:10px auto 18px;border-radius:24px;color:#fff;font:600 30px Georgia,serif;background:linear-gradient(145deg,#6972e5,#a86bc5)}.profile-panel h3{margin-bottom:7px}.profile-panel p{color:#8b90a0;font-size:13px}@media(max-width:700px){.account-grid{grid-template-columns:1fr}}</style>
