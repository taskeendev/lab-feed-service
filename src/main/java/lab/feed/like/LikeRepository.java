package lab.feed.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    // ON CONFLICT DO NOTHING = กด like ซ้ำกี่ครั้งก็ผลเหมือนเดิม (idempotent)
    @Modifying
    @Query(value = "INSERT INTO likes (post_id, username) VALUES (:postId, :me) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void like(@Param("postId") Long postId, @Param("me") String me);

    @Modifying
    @Query(value = "DELETE FROM likes WHERE post_id = :postId AND username = :me", nativeQuery = true)
    void unlike(@Param("postId") Long postId, @Param("me") String me);
}
