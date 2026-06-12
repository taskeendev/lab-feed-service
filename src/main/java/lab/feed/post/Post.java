package lab.feed.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_username", nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Post() {
    }

    public Post(String authorUsername, String content) {
        this.authorUsername = authorUsername;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getAuthorUsername() { return authorUsername; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
