package lab.feed.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import lab.feed.comment.Comment;
import lab.feed.comment.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

    public record CreateCommentRequest(@NotBlank @Size(max = 500) String content) {}

    public record CommentResponse(Long id, String author, String content, Instant createdAt) {

        static CommentResponse from(Comment c) {
            return new CommentResponse(c.getId(), c.getAuthorUsername(), c.getContent(), c.getCreatedAt());
        }
    }

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // อ่าน comment เปิด public เหมือนตัวโพสต์ (อยู่ใต้ GET /api/posts/** ที่ permitAll)
    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> list(@PathVariable Long postId) {
        return commentService.list(postId).stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/api/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Principal principal) {
        return CommentResponse.from(commentService.add(postId, principal.getName(), request.content()));
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.delete(id, auth.getName(), isAdmin);
    }
}
