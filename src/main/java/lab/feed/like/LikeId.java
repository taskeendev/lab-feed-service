package lab.feed.like;

import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable {

    private Long postId;
    private String username;

    public LikeId() {
    }

    public LikeId(Long postId, String username) {
        this.postId = postId;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LikeId other
                && Objects.equals(postId, other.postId)
                && Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, username);
    }
}
