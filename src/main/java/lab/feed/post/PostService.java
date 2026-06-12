package lab.feed.post;

import java.util.List;
import lab.feed.error.ForbiddenException;
import lab.feed.error.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository posts;

    public PostService(PostRepository posts) {
        this.posts = posts;
    }

    public Post create(String author, String content) {
        return posts.save(new Post(author, content));
    }

    public List<PostView> feed(String me, int page, int size) {
        int capped = Math.min(Math.max(size, 1), 50);
        return posts.feed(me, capped, Math.max(page, 0) * capped);
    }

    // ลบได้เฉพาะเจ้าของหรือ ADMIN — กติกาเดียว ใช้ซ้ำกับ comment ภายหลัง
    public void delete(Long id, String me, boolean isAdmin) {
        Post post = posts.findById(id)
                .orElseThrow(() -> new NotFoundException("post not found"));
        if (!post.getAuthorUsername().equals(me) && !isAdmin) {
            throw new ForbiddenException("not your post");
        }
        posts.delete(post);
    }
}
