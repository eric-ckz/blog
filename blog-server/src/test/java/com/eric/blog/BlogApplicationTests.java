package com.eric.blog;

import com.eric.blog.service.ArticleService;
import com.eric.blog.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Flyway、MyBatis-Plus 和核心 Service 能在测试环境完整启动。 */
@ActiveProfiles("test")
@SpringBootTest
class BlogApplicationTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Test
    void contextLoadsSeedData() {
        assertThat(articleService.count()).isGreaterThanOrEqualTo(30);
        assertThat(categoryService.listPublicCategories()).hasSize(5);
    }
}
