import { expect, test } from '@playwright/test'

const routes = ['/', '/articles', '/gallery', '/archive', '/about', '/article/0']

for (const route of routes) {
  test(`${route} renders without horizontal overflow`, async ({ page }) => {
    const browserErrors = []
    page.on('console', (message) => { if (message.type() === 'error') browserErrors.push(message.text()) })
    page.on('pageerror', (error) => browserErrors.push(error.message))
    await page.goto(route)
    await expect(page.locator('.site-header')).toBeVisible()
    await expect(page.locator('main.site-main')).toBeVisible()
    await expect(page.getByRole('status')).toHaveCount(0)
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
    expect(overflow).toBe(false)
    expect(browserErrors).toEqual([])
  })
}
