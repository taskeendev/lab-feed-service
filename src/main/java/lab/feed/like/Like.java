package lab.feed.like;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

// แถวละหนึ่ง like — PK ประกอบ (post_id, username) กันกดซ้ำที่ระดับ DB
@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
public class Like {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    private String username;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Like() {
    }
}
