package lab.feed.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // เรียงเก่า→ใหม่แบบบทสนทนา ใช้ index idx_comments_post
    List<Comment> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);
}
