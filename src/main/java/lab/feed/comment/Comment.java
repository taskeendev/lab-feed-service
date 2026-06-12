package lab.feed.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Generated;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_username", nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String content;

    // DB เป็นคนใส่ now() — @Generated ให้ Hibernate อ่านค่ากลับมาหลัง insert
    @Generated
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Comment() {
    }

    public Comment(Long postId, String authorUsername, String content) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
