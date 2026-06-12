package lab.feed.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import lab.feed.post.Post;
import lab.feed.post.PostService;
import lab.feed.post.PostView;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    public record CreatePostRequest(@NotBlank @Size(max = 500) String content) {}

    public record PostResponse(
            Long id, String author, String content, Instant createdAt,
            long likeCount, long commentCount, boolean likedByMe) {

        static PostResponse fresh(Post post) {
            return new PostResponse(post.getId(), post.getAuthorUsername(), post.getContent(),
                    post.getCreatedAt(), 0, 0, false);
        }

        static PostResponse from(PostView v) {
            return new PostResponse(v.getId(), v.getAuthor(), v.getContent(), v.getCreatedAt(),
                    v.getLikeCount(), v.getCommentCount(), v.getLikedByMe());
        }
    }

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // อ่านได้ทั้งนิรนามและคน login (principal เป็น null ได้ — likedByMe จะ false)
    @GetMapping
    public List<PostResponse> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String me = principal != null ? principal.getName() : null;
        return postService.feed(me, page, size).stream().map(PostResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @Valid @RequestBody CreatePostRequest request, Principal principal) {
        return PostResponse.fresh(postService.create(principal.getName(), request.content()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.delete(id, auth.getName(), isAdmin);
    }
}
