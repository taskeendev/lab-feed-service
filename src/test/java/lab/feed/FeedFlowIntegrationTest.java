package lab.feed;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// เทสต์กับ Postgres จริงจาก Testcontainers — token ปั้นเองด้วย secret เทสต์
// (พิสูจน์ว่า feed ตรวจ JWT เองโดยไม่ต้องมี auth-service อยู่จริง)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"jwt.secret=feed-integration-test-secret-must-be-long-42"})
@Testcontainers
class FeedFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    static final SecretKey KEY = Keys.hmacShaKeyFor(
            "feed-integration-test-secret-must-be-long-42".getBytes(StandardCharsets.UTF_8));

    @Autowired
    TestRestTemplate rest;

    static String token(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(KEY)
                .compact();
    }

    static final String ALICE = token("alice", "USER");
    static final String BOSS = token("boss", "ADMIN");

    private static HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private long createPost(String token, String content) {
        ResponseEntity<Map> res = rest.exchange("/api/posts", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", content), bearer(token)), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().get("createdAt")).isNotNull(); // กันบั๊ก @Generated ย้อนกลับ
        return ((Number) res.getBody().get("id")).longValue();
    }

    private int statusOf(String url, HttpMethod method, String token, Map<String, ?> body) {
        HttpEntity<?> entity = new HttpEntity<>(body, token != null ? bearer(token) : new HttpHeaders());
        return rest.exchange(url, method, entity, Map.class).getStatusCode().value();
    }

    @Test
    void postsCrudAndPermissions() {
        long p1 = createPost(ALICE, "first from alice");
        long p2 = createPost(ALICE, "second from alice");
        long p3 = createPost(BOSS, "from the admin");

        // นิรนามเขียนไม่ได้ / เขียนว่างไม่ได้ (ตรวจ field error)
        assertThat(statusOf("/api/posts", HttpMethod.POST, null, Map.of("content", "x"))).isEqualTo(401);
        ResponseEntity<Map> blank = rest.exchange("/api/posts", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", ""), bearer(ALICE)), Map.class);
        assertThat(blank.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((Map<String, String>) blank.getBody().get("errors")).containsKey("content");

        // นิรนามอ่านได้ เรียงใหม่→เก่า + แบ่งหน้า
        ResponseEntity<List> page0 = rest.getForEntity("/api/posts?size=2", List.class);
        assertThat(page0.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> posts = page0.getBody();
        assertThat(posts).hasSize(2);
        assertThat(((Number) posts.get(0).get("id")).longValue()).isEqualTo(p3);
        assertThat(((Number) posts.get(1).get("id")).longValue()).isEqualTo(p2);
        assertThat(posts.get(0).get("likedByMe")).isEqualTo(false);
        List<Map<String, Object>> page1 = rest.getForEntity("/api/posts?size=2&page=1", List.class).getBody();
        assertThat(((Number) page1.get(0).get("id")).longValue()).isEqualTo(p1);

        // ลบ: คนอื่น 403, เจ้าของ 204, ADMIN ลบของคนอื่นได้, ไม่มีจริง 404
        assertThat(statusOf("/api/posts/" + p3, HttpMethod.DELETE, ALICE, null)).isEqualTo(403);
        assertThat(statusOf("/api/posts/" + p1, HttpMethod.DELETE, ALICE, null)).isEqualTo(204);
        assertThat(statusOf("/api/posts/" + p2, HttpMethod.DELETE, BOSS, null)).isEqualTo(204);
        assertThat(statusOf("/api/posts/" + p3, HttpMethod.DELETE, BOSS, null)).isEqualTo(204);
        assertThat(statusOf("/api/posts/99999", HttpMethod.DELETE, ALICE, null)).isEqualTo(404);
    }

    @Test
    void commentsLikesAndCascade() {
        long post = createPost(ALICE, "post under test");

        // comment: ต้อง login, โพสต์ต้องมีจริง, เรียงเก่า→ใหม่, อ่าน public
        assertThat(statusOf("/api/posts/" + post + "/comments", HttpMethod.POST, null,
                Map.of("content", "x"))).isEqualTo(401);
        assertThat(statusOf("/api/posts/99999/comments", HttpMethod.POST, ALICE,
                Map.of("content", "x"))).isEqualTo(404);
        ResponseEntity<Map> c1 = rest.exchange("/api/posts/" + post + "/comments", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "nice post"), bearer(BOSS)), Map.class);
        assertThat(c1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(c1.getBody().get("createdAt")).isNotNull();
        long bossComment = ((Number) c1.getBody().get("id")).longValue();
        ResponseEntity<Map> c2 = rest.exchange("/api/posts/" + post + "/comments", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "thanks!"), bearer(ALICE)), Map.class);
        long aliceComment = ((Number) c2.getBody().get("id")).longValue();

        List<Map<String, Object>> comments =
                rest.getForEntity("/api/posts/" + post + "/comments", List.class).getBody();
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).get("author")).isEqualTo("boss"); // มาก่อนอยู่ก่อน
        assertThat(comments.get(1).get("author")).isEqualTo("alice");

        // like: idempotent ทั้งกดและถอน + likedByMe ตามคนถาม
        for (int i = 0; i < 3; i++) {
            assertThat(statusOf("/api/posts/" + post + "/like", HttpMethod.PUT, ALICE, null)).isEqualTo(204);
        }
        assertThat(statusOf("/api/posts/" + post + "/like", HttpMethod.PUT, BOSS, null)).isEqualTo(204);
        assertThat(statusOf("/api/posts/99999/like", HttpMethod.PUT, ALICE, null)).isEqualTo(404);

        Map<String, Object> asAlice = firstPost(bearer(ALICE));
        assertThat(((Number) asAlice.get("likeCount")).longValue()).isEqualTo(2);
        assertThat(((Number) asAlice.get("commentCount")).longValue()).isEqualTo(2);
        assertThat(asAlice.get("likedByMe")).isEqualTo(true);
        Map<String, Object> asAnon = firstPost(new HttpHeaders());
        assertThat(((Number) asAnon.get("likeCount")).longValue()).isEqualTo(2);
        assertThat(asAnon.get("likedByMe")).isEqualTo(false);

        for (int i = 0; i < 2; i++) {
            assertThat(statusOf("/api/posts/" + post + "/like", HttpMethod.DELETE, ALICE, null)).isEqualTo(204);
        }
        assertThat(((Number) firstPost(new HttpHeaders()).get("likeCount")).longValue()).isEqualTo(1);

        // ลบ comment: คนอื่น 403, ADMIN ได้, ไม่มีจริง 404
        assertThat(statusOf("/api/comments/" + bossComment, HttpMethod.DELETE, ALICE, null)).isEqualTo(403);
        assertThat(statusOf("/api/comments/" + aliceComment, HttpMethod.DELETE, BOSS, null)).isEqualTo(204);
        assertThat(statusOf("/api/comments/99999", HttpMethod.DELETE, ALICE, null)).isEqualTo(404);

        // ลบโพสต์ → comment ที่เหลือหายตาม (CASCADE) — อ่าน comments ของโพสต์นั้นต้อง 404
        assertThat(statusOf("/api/posts/" + post, HttpMethod.DELETE, ALICE, null)).isEqualTo(204);
        assertThat(rest.getForEntity("/api/posts/" + post + "/comments", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Map<String, Object> firstPost(HttpHeaders headers) {
        List<Map<String, Object>> posts = rest.exchange(
                "/api/posts", HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
        return posts.get(0);
    }
}
