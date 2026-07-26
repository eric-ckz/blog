import { defineConfig, devices } from '@playwright/test'

const backendEnv = {
  ...process.env,
  BLOG_ADMIN_USERNAME: 'e2e-admin',
  BLOG_ADMIN_PASSWORD: 'e2e-password-123',
  BLOG_ADMIN_DISPLAY_NAME: '端到端测试管理员',
  BLOG_JWT_SECRET: 'e2e-secret-that-is-longer-than-thirty-two-characters',
  BLOG_CORS_ALLOWED_ORIGINS: 'http://127.0.0.1:3000,http://127.0.0.1:3100',
  SPRING_DATASOURCE_URL: 'jdbc:h2:mem:blog_admin_e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1',
}

export default defineConfig({
  testDir: './tests/e2e',
  use: { baseURL: 'http://127.0.0.1:3100', trace: 'on-first-retry' },
  webServer: [
    {
      command: 'mvn -f ../blog-server/pom.xml spring-boot:run',
      url: 'http://127.0.0.1:8080/api/site/home',
      timeout: 120_000,
      reuseExistingServer: true,
      env: backendEnv,
    },
    {
      command: 'npm run dev -- --host 127.0.0.1',
      url: 'http://127.0.0.1:3100',
      reuseExistingServer: true,
    },
  ],
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
