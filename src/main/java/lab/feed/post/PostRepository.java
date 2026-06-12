package lab.feed.post;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // feed ทั้งหน้าในคิวรีเดียว: นับ like/comment ด้วย subquery + likedByMe ด้วย EXISTS
    // (:me เป็น NULL ตอนนิรนาม → EXISTS เป็น false เอง) — ไม่มี N+1
    @Query(value = """
            SELECT p.id AS id,
                   p.author_username AS author,
                   p.content AS content,
                   p.created_at AS createdAt,
                   (SELECT count(*) FROM likes l WHERE l.post_id = p.id) AS likeCount,
                   (SELECT count(*) FROM comments c WHERE c.post_id = p.id) AS commentCount,
                   EXISTS(SELECT 1 FROM likes lm
                          WHERE lm.post_id = p.id AND lm.username = :me) AS likedByMe
            FROM posts p
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<PostView> feed(@Param("me") String me, @Param("size") int size, @Param("offset") int offset);
}
