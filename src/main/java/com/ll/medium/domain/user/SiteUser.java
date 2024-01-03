package com.ll.medium.domain.user;

import com.ll.medium.domain.post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Getter
@Setter
@Entity
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private boolean isPaid; //유료회원 여부

    @ManyToMany
    @JoinTable(
            name = "vote",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> votedPosts = new HashSet<>();

    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));

        if (List.of("system", "admin").contains(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (isPaid) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PAID"));
        }

        return authorities;
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
