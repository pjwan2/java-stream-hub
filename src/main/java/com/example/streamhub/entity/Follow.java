package com.example.streamhub.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_follows_follower_followed_type",
                columnNames = {"follower_id", "followed_id", "type"}
        )
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private AppUser follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followed_id", nullable = false)
    private AppUser followed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowType type;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;



    public Follow(AppUser follower, AppUser followed, FollowType type) {
        if (follower == followed) {
            throw new IllegalArgumentException("用户不能关注自己");
        }
        this.follower = follower;
        this.followed = followed;
        this.type = type;
        this.createdAt = Instant.now();
    }

}
