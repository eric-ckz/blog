import { test, expect } from '@playwright/test'

function collectBrowserErrors(page) {
  const errors = []
  page.on('console', (message) => { if (message.type() === 'error') errors.push(message.text()) })
  page.on('pageerror', (error) => errors.push(error.message))
  return errors
}

test('login page renders', async ({ page }) => {
  const browserErrors = collectBrowserErrors(page)
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: '登录管理后台' })).toBeVisible()
  await expect(page.getByRole('button', { name: '进入内容工作室' })).toBeVisible()
  expect(browserErrors).toEqual([])
})

test('administrator can login and create an article', async ({ page }) => {
  const browserErrors = collectBrowserErrors(page)
  const title = `Playwright 测试文章 ${Date.now()}`
  await page.goto('/login')
  await page.getByPlaceholder('请输入账号').fill('e2e-admin')
  await page.getByPlaceholder('请输入密码').fill('e2e-password-123')
  await page.getByRole('button', { name: '进入内容工作室' }).click()

  await expect(page.getByRole('heading', { name: '内容概览' })).toBeVisible()
  await page.getByRole('link', { name: '文章管理' }).click()
  await expect(page.getByRole('heading', { name: '文章管理', level: 2 })).toBeVisible()
  await page.getByRole('button', { name: '新建文章' }).click()

  await page.getByPlaceholder('写下一个值得重读的标题').fill(title)
  await page.getByRole('textbox', { name: '摘要' }).fill('这是一篇由管理端端到端测试创建的草稿。')
  const categoryField = page.locator('.side-form .el-form-item').filter({ hasText: '所属栏目' })
  await categoryField.locator('.el-select').click()
  await page.getByRole('option', { name: '生活感悟' }).click()
  await page.locator('.ProseMirror').fill('管理端富文本编辑器端到端测试正文。')
  await page.getByRole('button', { name: '保存文章' }).click()

  await expect(page.getByRole('heading', { name: '文章管理', level: 2 })).toBeVisible()
  await expect(page.getByText(title, { exact: true })).toBeVisible()
  expect(browserErrors).toEqual([])
})
