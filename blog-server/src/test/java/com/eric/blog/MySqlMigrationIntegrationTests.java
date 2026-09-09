package com.eric.blog;

import com.eric.blog.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在本机 Docker 可用时，用真实 MySQL 8.4 执行全部 Flyway 脚本。
 * disabledWithoutDocker 让没有 Docker 的开发机跳过本测试，而不会掩盖其他单元与 H2 集成测试结果。
 */
@ActiveProfiles("test")
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class MySqlMigrationIntegrationTests {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("eric_blog_test")
            .withUsername("blog_test")
            .withPassword("blog_test_password");

    @DynamicPropertySource
    static void useContainerDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    private ArticleService articleService;

    @Test
    void flywaySeedsTheSameContentOnMySql() {
        assertThat(articleService.count()).isGreaterThanOrEqualTo(30);
    }
}
