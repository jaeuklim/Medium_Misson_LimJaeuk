package com.ll.medium.domain.post;

import com.ll.medium.domain.DataNotFoundException;
import com.ll.medium.domain.comment.Comment;
import com.ll.medium.domain.user.SiteUser;
import com.ll.medium.domain.user.UserService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PostService {

    private final UserService userService;
    private final PostRepository postRepository;

    private Specification<Post> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Post> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거
                Join<Post, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Post, Comment> a = q.join("commentList", JoinType.LEFT);
                Join<Comment, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자
            }
        };
    }

    public Page<Post> getList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 30, Sort.by(sorts));
        Specification<Post> spec = search(kw);
        spec = Specification.where(spec).and(isPublished());
        return this.postRepository.findAll(spec, pageable);
    }

    public Post getPost(Integer id) {
        Optional<Post> post = this.postRepository.findById(id);
        if (post.isPresent()) {
            return post.get();
        } else {
            throw new DataNotFoundException("post not found");
        }
    }

    public void create(String subject, String content, boolean isPublished, boolean isPaid, SiteUser user) {
        Post q = new Post();
        q.setSubject(subject);
        q.setContent(content);
        q.setPublished(isPublished);
        q.setPaid(isPaid);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        this.postRepository.save(q);
    }

    public void modify(Post post, String subject, String content, Boolean isPublished) {
        post.setSubject(subject);
        post.setContent(content);
        post.setPublished(isPublished);
        post.setModifyDate(LocalDateTime.now());
        this.postRepository.save(post);
    }

    public void delete(Post post) {
        this.postRepository.delete(post);
    }

    public boolean hasVoted(Post post, SiteUser user) {
        post.getVoters().add(user);
        this.postRepository.save(post);
        return user.getVotedPosts().contains(post);
    }

    public boolean isVotedUser(String username, Set<SiteUser> voters) {
        SiteUser findUser = userService.getUser(username);
        return voters.contains(findUser);
    }

//    public void vote(Post post, SiteUser siteUser) {
//        post.getVoter().add(siteUser);
//        this.postRepository.save(post);
//    }
//
    public boolean canCellike(Post post, SiteUser siteUser) {
        post.getVoters().remove(siteUser);
        this.postRepository.save(post);
        return siteUser.getVotedPosts().contains(post);
    }

    @Transactional
    public void increaseViewCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        System.out.println("increaseViewCount called, view count: " + post.getViewCount());

    }

    public Page<Post> getListByAuthor(SiteUser author, int page) {
        // author가 작성한 글만을 가져옵니다.
        return postRepository.findByAuthor(author, PageRequest.of(page, 10));
    }

    private Specification<Post> isPublished() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublished"));
    }
}

