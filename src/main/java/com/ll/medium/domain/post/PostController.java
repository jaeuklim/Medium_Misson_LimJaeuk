package com.ll.medium.domain.post;

import com.ll.medium.domain.comment.CommentForm;
import com.ll.medium.domain.user.SiteUser;
import com.ll.medium.domain.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;

@Slf4j
@RequestMapping("/post")
@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Post> paging = this.postService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "post_list";
    }

    @GetMapping(value = "/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm, Principal principal, Authentication authentication) {
        Post post = this.postService.getPost(id);
        postService.increaseViewCount(post);

        boolean isVoted = false;
        boolean postisPaid = post.isPaid();
        boolean userisPaid = false;
        boolean isAuthor = false;

        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            userisPaid = authorities.contains(new SimpleGrantedAuthority("ROLE_PAID"));
        }

        if ( principal != null ) {
            isVoted = postService.isVotedUser(principal.getName(), post.getVoters());
            isAuthor = principal.getName().equals(post.getAuthor().getUsername()); // 게시글 작성자와 현재 사용자를 비교
        }

//        log.info("isVoted = {}", isVoted);
        log.info("postisPaid 는 {}", postisPaid);
        log.info("userisPaid 는 {}", userisPaid);

        model.addAttribute("post", post);
        model.addAttribute("isVoted", isVoted);
        model.addAttribute("postisPaid", postisPaid);
        model.addAttribute("userisPaid", userisPaid || isAuthor);

        return "post_detail";
    }

    @PostMapping("/{id}")
    public String viewPost(@PathVariable("id") Integer id, Model model) {
        Post post = postService.getPost(id);
        postService.increaseViewCount(post);
        model.addAttribute("post", post);
        return "post_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String postCreate(PostForm postForm) {
        return "post_form";
    }

    @PostAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String postCreate(@Valid PostForm postForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "post_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.postService.create(postForm.getSubject(), postForm.getContent(), postForm.getIsPublished(), postForm.getIsPaid(), siteUser);
        return "redirect:/post/list";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/modify")
    public String postModify(PostForm postForm, @PathVariable("id") Integer id, Principal principal) {
        Post post = this.postService.getPost(id);
        if(!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        postForm.setSubject(post.getSubject());
        postForm.setContent(post.getContent());
        postForm.setIsPublished(post.isPublished());
        return "modify_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/modify")
    public String postModify(@Valid PostForm postForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "modify_form";
        }
        Post post = this.postService.getPost(id);
        if (!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.postService.modify(post, postForm.getSubject(), postForm.getContent(), postForm.getIsPublished());

        return String.format("redirect:/post/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/delete")
    public String postDelete(Principal principal, @PathVariable("id") Integer id) {
        Post post = this.postService.getPost(id);
        if (!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.postService.delete(post);
        return "redirect:/";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public String postVote(Model model, Principal principal, @PathVariable("id") Integer id) {
        Post post = this.postService.getPost(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        boolean hasVoted = this.postService.hasVoted(post, siteUser);
        model.addAttribute("hasVoted", hasVoted);

        return String.format("redirect:/post/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/cancelLike")
    public String cancelVote(Model model, Principal principal, @PathVariable("id") Integer id) {
        Post post = this.postService.getPost(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        boolean hasVoted = this.postService.canCellike(post, siteUser);
        model.addAttribute("hasVoted", hasVoted);
        return String.format("redirect:/post/%s", id);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myList")
    public String myList(Model model, Principal principal , @RequestParam(value="page", defaultValue="0") int page) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Page<Post> paging = this.postService.getListByAuthor(siteUser, page);
        model.addAttribute("paging", paging);
        return "post_list";
    }
}
