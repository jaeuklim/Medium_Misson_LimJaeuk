package com.ll.medium.domain.post;

import com.ll.medium.domain.DataNotFoundException;
import com.ll.medium.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 30, Sort.by(sorts));
        return this.postRepository.findAll(pageable);
    }

    public Post getPost(Integer id) {
        Optional<Post> post = this.postRepository.findById(id);
        if (post.isPresent()) {
            return post.get();
        } else {
            throw new DataNotFoundException("post not found");
        }
    }

    public void create(String subject, String content, SiteUser user) {
        Post q = new Post();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        this.postRepository.save(q);
    }

    public void modify(Post post, String subject, String content) {
        post.setSubject(subject);
        post.setContent(content);
        post.setModifyDate(LocalDateTime.now());
        this.postRepository.save(post);
    }

    public void delete(Post post) {
        this.postRepository.delete(post);
    }

    public void vote(Post post, SiteUser siteUser) {
        post.getVoter().add(siteUser);
        this.postRepository.save(post);
    }

    public void canCellike(Post post, SiteUser siteUser) {
        post.getVoter().remove(siteUser);
        this.postRepository.save(post);
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
}

