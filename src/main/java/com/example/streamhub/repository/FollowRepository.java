package com.example.streamhub.repository;

import com.example.streamhub.entity.Follow;
import com.example.streamhub.entity.FollowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowedIdAndType(Long followerId, Long followedId, FollowType type);

    List<Follow> findByFollowerId(Long followerId);

    @Query("""
            select f.followed.id, count(f.id)
            from Follow f
            where f.type = :type
            group by f.followed.id
            order by count(f.id) desc
            """)
    List<Object[]> rankStreamerIds(@Param("type") FollowType type);
}
