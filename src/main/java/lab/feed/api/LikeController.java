package lab.feed.api;

import java.security.Principal;
import lab.feed.like.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// PUT/DELETE เพราะ like เป็นสถานะ ไม่ใช่เหตุการณ์ — ยิงซ้ำได้ผลเหมือนเดิม
@RestController
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PutMapping("/api/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long postId, Principal principal) {
        likeService.like(postId, principal.getName());
    }

    @DeleteMapping("/api/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long postId, Principal principal) {
        likeService.unlike(postId, principal.getName());
    }
}
