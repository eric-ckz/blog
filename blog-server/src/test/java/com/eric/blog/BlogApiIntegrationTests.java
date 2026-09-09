package com.eric.blog;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通过随机端口访问真实 Spring Security、Controller、Service 与数据库，验证浏览器实际会经历的完整链路。
 * 使用 JDK HttpClient 而不是模拟 Controller，避免遗漏 Cookie 轮换、过滤器顺序和统一错误响应。
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlogApiIntegrationTests {

    private static final String REFRESH_COOKIE = "blog_refresh_token";

    @LocalServerPort
    private int port;

    private CookieManager cookieManager;
    private HttpClient client;

    @BeforeEach
    void createBrowserLikeClient() {
        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    void publicApiUsesStableEnvelopeAndFrontendCompatibleShapes() throws Exception {
        HttpResponse<String> home = request("GET", "/api/web/site/home", null, null);
        assertThat(home.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(home.body(), "$.code")).isEqualTo("20000");
        assertThat(JsonPath.<Number>read(home.body(), "$.data.stats.articles").intValue()).isEqualTo(528);
        assertThat(JsonPath.<java.util.List<?>>read(home.body(), "$.data.heroSlides")).hasSize(3);
        assertThat(JsonPath.<java.util.List<?>>read(home.body(), "$.data.featured")).hasSize(4);

        HttpResponse<String> articles = request("GET", "/api/web/articles?page=1&pageSize=6", null, null);
        assertThat(articles.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<Number>read(articles.body(), "$.data.total").longValue()).isGreaterThanOrEqualTo(24);
        assertThat(JsonPath.<java.util.List<?>>read(articles.body(), "$.data.items")).hasSize(6);
        assertThat(JsonPath.<Boolean>read(articles.body(), "$.data.hasMore")).isTrue();

        HttpResponse<String> archive = request("GET", "/api/web/archive", null, null);
        assertThat(archive.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<Number>read(archive.body(), "$.data[0].months[0].count").intValue()).isPositive();
        assertThat(JsonPath.<java.util.List<?>>read(archive.body(), "$.data[0].months[0].items")).isNotEmpty();

        HttpResponse<String> notFound = request("GET", "/api/common/articles/999999999", null, null);
        assertThat(notFound.statusCode()).isEqualTo(404);
        assertThat(JsonPath.<String>read(notFound.body(), "$.code")).isEqualTo("40400");
    }

    @Test
    void loginRefreshCrudUploadAndLogoutFormACompleteAdminFlow() throws Exception {
        HttpResponse<String> anonymous = request("GET", "/api/admin/dashboard", null, null);
        assertThat(anonymous.statusCode()).isEqualTo(401);
        assertThat(JsonPath.<String>read(anonymous.body(), "$.code")).isEqualTo("40100");

        HttpResponse<String> wrongPassword = request("POST", "/api/admin/auth/login",
                "{\"username\":\"test-admin\",\"password\":\"wrong-password\"}", null);
        assertThat(wrongPassword.statusCode()).isEqualTo(401);

        HttpResponse<String> login = request("POST", "/api/admin/auth/login",
                "{\"username\":\"test-admin\",\"password\":\"test-password-123\"}", null);
        assertThat(login.statusCode()).isEqualTo(200);
        String firstAccessToken = JsonPath.read(login.body(), "$.data.accessToken");
        String firstRefreshToken = refreshCookieValue();
        assertThat(firstAccessToken).isNotBlank();
        assertThat(firstRefreshToken).isNotBlank();

        HttpResponse<String> profile = request("GET", "/api/admin/auth/me", null, firstAccessToken);
        assertThat(profile.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(profile.body(), "$.data.username")).isEqualTo("test-admin");

        HttpResponse<String> refresh = request("POST", "/api/admin/auth/refresh", null, null);
        assertThat(refresh.statusCode()).isEqualTo(200);
        String accessToken = JsonPath.read(refresh.body(), "$.data.accessToken");
        String currentRefreshToken = refreshCookieValue();
        assertThat(accessToken).isNotEqualTo(firstAccessToken);
        assertThat(currentRefreshToken).isNotEqualTo(firstRefreshToken);

        // 旧 Refresh Token 已被原子撤销，即使攻击者保存了旧 Cookie 也不能再次换取会话。
        HttpResponse<String> replay = rawRefresh(firstRefreshToken);
        assertThat(replay.statusCode()).isEqualTo(401);

        HttpResponse<String> invalidUpload = upload("not-an-image".getBytes(StandardCharsets.UTF_8),
                "fake.jpg", "image/jpeg", accessToken);
        assertThat(invalidUpload.statusCode()).isEqualTo(422);

        byte[] png = createPng();
        HttpResponse<String> uploaded = upload(png, "cover.png", "image/png", accessToken);
        assertThat(uploaded.statusCode()).withFailMessage(uploaded.body()).isEqualTo(200);
        String mediaId = JsonPath.read(uploaded.body(), "$.data.id");

        String createPayload = """
                {
                  "title":"集成测试文章",
                  "summary":"验证管理端文章新增、清理、查询和删除链路。",
                  "categoryId":"1005",
                  "coverMediaId":"%s",
                  "contentHtml":"<p>安全正文</p><script>alert(1)</script>",
                  "status":"PUBLISHED",
                  "publishedAt":"2026-07-26T12:00:00",
                  "readMinutes":3
                }
                """.formatted(mediaId);
        HttpResponse<String> created = request("POST", "/api/admin/articles", createPayload, accessToken);
        assertThat(created.statusCode()).isEqualTo(200);
        String articleId = JsonPath.read(created.body(), "$.data.id");

        HttpResponse<String> detail = request("GET", "/api/common/articles/" + articleId, null, null);
        assertThat(detail.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(detail.body(), "$.data.contentHtml"))
                .contains("安全正文")
                .doesNotContain("script");

        HttpResponse<String> referencedDelete = request("DELETE", "/api/admin/media/" + mediaId, null, accessToken);
        assertThat(referencedDelete.statusCode()).isEqualTo(409);

        HttpResponse<String> deletedArticle = request("DELETE", "/api/admin/articles/" + articleId, null, accessToken);
        assertThat(deletedArticle.statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/common/articles/" + articleId, null, null).statusCode()).isEqualTo(404);

        HttpResponse<String> deletedMedia = request("DELETE", "/api/admin/media/" + mediaId, null, accessToken);
        assertThat(deletedMedia.statusCode()).isEqualTo(200);

        HttpResponse<String> logout = request("POST", "/api/admin/auth/logout", null, null);
        assertThat(logout.statusCode()).isEqualTo(200);
        assertThat(rawRefresh(currentRefreshToken).statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> request(String method, String path, String json, String accessToken) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(10));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        if (json != null) {
            builder.header("Content-Type", "application/json; charset=UTF-8")
                    .method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> upload(byte[] content, String filename, String contentType, String accessToken)
            throws Exception {
        String boundary = "----EricBlogBoundary" + System.nanoTime();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(content);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(uri("/api/admin/media"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> rawRefresh(String refreshToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/admin/auth/refresh"))
                .timeout(Duration.ofSeconds(10))
                .header("Cookie", REFRESH_COOKIE + "=" + refreshToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private byte[] createPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x6B7A99);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private String refreshCookieValue() {
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(cookie -> REFRESH_COOKIE.equals(cookie.getName()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("响应没有写入 Refresh Token Cookie"));
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
