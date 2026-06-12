package lab.feed.like;

import jakarta.transaction.Transactional;
import lab.feed.error.NotFoundException;
import lab.feed.post.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final LikeRepository likes;
    private final PostRepository posts;

    public LikeService(LikeRepository likes, PostRepository posts) {
        this.likes = likes;
        this.posts = posts;
    }

    @Transactional
    public void like(Long postId, String me) {
        if (!posts.existsById(postId)) {
            throw new NotFoundException("post not found");
        }
        likes.like(postId, me);
    }

    @Transactional
    public void unlike(Long postId, String me) {
        if (!posts.existsById(postId)) {
            throw new NotFoundException("post not found");
        }
        likes.unlike(postId, me);
    }
}
