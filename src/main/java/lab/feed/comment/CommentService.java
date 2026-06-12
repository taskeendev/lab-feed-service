package lab.feed.comment;

import java.util.List;
import lab.feed.error.ForbiddenException;
import lab.feed.error.NotFoundException;
import lab.feed.post.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository comments;
    private final PostRepository posts;

    public CommentService(CommentRepository comments, PostRepository posts) {
        this.comments = comments;
        this.posts = posts;
    }

    public Comment add(Long postId, String author, String content) {
        if (!posts.existsById(postId)) {
            throw new NotFoundException("post not found");
        }
        return comments.save(new Comment(postId, author, content));
    }

    public List<Comment> list(Long postId) {
        if (!posts.existsById(postId)) {
            throw new NotFoundException("post not found");
        }
        return comments.findByPostIdOrderByCreatedAtAscIdAsc(postId);
    }

    public void delete(Long id, String me, boolean isAdmin) {
        Comment comment = comments.findById(id)
                .orElseThrow(() -> new NotFoundException("comment not found"));
        if (!comment.getAuthorUsername().equals(me) && !isAdmin) {
            throw new ForbiddenException("not your comment");
        }
        comments.delete(comment);
    }
}
