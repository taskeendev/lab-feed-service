package lab.feed.post;

import java.time.Instant;

// interface projection สำหรับ native query — Spring จับคู่จากชื่อ alias
public interface PostView {
    Long getId();
    String getAuthor();
    String getContent();
    Instant getCreatedAt();
    long getLikeCount();
    long getCommentCount();
    boolean getLikedByMe();
}
