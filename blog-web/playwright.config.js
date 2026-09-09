import { defineConfig, devices } from '@playwright/test'

const backendEnv = {
  ...process.env,
  BLOG_ADMIN_USERNAME: 'e2e-admin',
  BLOG_ADMIN_PASSWORD: 'e2e-password-123',
  BLOG_ADMIN_DISPLAY_NAME: '端到端测试管理员',
  BLOG_JWT_SECRET: 'e2e-secret-that-is-longer-than-thirty-two-characters',
  BLOG_CORS_ALLOWED_ORIGINS: 'http://127.0.0.1:3000,http://127.0.0.1:3100',
  SPRING_DATASOURCE_URL: 'jdbc:h2:mem:blog_web_e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1',
}

export default defineConfig({
  testDir: './tests/e2e',
  use: {
    baseURL: 'http://127.0.0.1:3000',
    trace: 'retain-on-failure',
  },
  webServer: [
    {
      // 端到端测试强制使用真实 Java API，避免 mock 模式掩盖响应契约差异。
      command: 'mvn -f ../blog-server/pom.xml spring-boot:run',
      url: 'http://127.0.0.1:8080/api/web/site/home',
      timeout: 120_000,
      reuseExistingServer: true,
      env: backendEnv,
    },
    {
      command: 'npm run dev -- --host 127.0.0.1 --port 3000',
      url: 'http://127.0.0.1:3000',
      reuseExistingServer: true,
      env: { ...process.env, VITE_DATA_MODE: 'api', VITE_API_BASE_URL: '/api' },
    },
  ],
  projects: [
    { name: 'desktop', use: { ...devices['Desktop Chrome'], viewport: { width: 1440, height: 900 } } },
    { name: 'tablet', use: { ...devices['Desktop Chrome'], viewport: { width: 1024, height: 768 } } },
    { name: 'mobile', use: { ...devices['Desktop Chrome'], viewport: { width: 390, height: 844 } } },
  ],
})
