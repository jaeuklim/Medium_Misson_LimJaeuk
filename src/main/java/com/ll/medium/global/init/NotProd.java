package com.ll.medium.global.init;

import com.ll.medium.domain.post.PostService;
import com.ll.medium.domain.user.SiteUser;
import com.ll.medium.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Configuration
@Profile("!prod")
@Slf4j
@RequiredArgsConstructor
public class NotProd {
    @Autowired
    @Lazy
    private NotProd self;
    private final UserService userService;
    private final PostService postService;

    @Bean
    @Order(3)
    public ApplicationRunner initNotProd() {
        return args -> {
//            if (userService. getUser("user1")) return;

            self.work1();
        };
    }

    @Transactional
    public void work1() {
        SiteUser User1 = userService.create("user1", "1234", true);
        SiteUser User2 = userService.create("user2", "1234",false);
        SiteUser User3 = userService.create("user3", "1234",true);
        SiteUser User4 = userService.create("user4", "1234",false);

        postService.create("제목 1", "내용 1", true, true, User1);
        postService.create("제목 2", "내용 2", true, false, User1);
        postService.create("제목 3", "내용 3", true,false, User3);
        postService.create("제목 4", "내용 4", true, true, User3);

        postService.create("제목 5", "내용 5", true, false, User2);
        postService.create("제목 6", "내용 6", true, false, User2);

        for(int i = 5; i<=100; i++){
            userService.create("user" + i, "1234", true);
            i++;
            userService.create("user" + i, "1234", false);
        }

        IntStream.rangeClosed(7, 50).forEach(i -> {
            postService.create("제목 " + i, "내용 " + i, true, true, User4);
        });

        for(int i = 51; i<=100; i++){
            postService.create("제목 " + i, "내용 " + i, true, true, User1); i++;
            postService.create("제목 " + i, "내용 " + i, true, false, User2); i++;
            postService.create("제목 " + i, "내용 " + i, true, true, User3); i++;
            postService.create("제목 " + i, "내용 " + i, true, false, User4);;

        }

//        postService.like(memberUser2, post1);
//        postService.like(memberUser3, post1);
//        postService.like(memberUser4, post1);
//
//        postService.like(memberUser2, post2);
//        postService.like(memberUser3, post2);
//
//        postService.like(memberUser2, post3);
    }
}
