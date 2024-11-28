package com.rsmanager.repository.local;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokVideoDetails;

@Repository
public interface TikTokVideoDetailsRepository extends JpaRepository<TiktokVideoDetails, String> {
    
    @Query("SELECT MAX(t.updatedAt) FROM TiktokVideoDetails t")
    Instant findMaxUpdateAt();

    // @Query("""
    //     SELECT new com.rsmanager.dto.traffic.TikTokVideoDetailsDTO(
    //         t.tiktokVideoId, t.authorId, t.videoDesc, t.categoryType, t.collectCount, t.commentCount,
    //         t.diggCount, t.playCount, t.repostCount, t.shareCount, t.createdAt, t.updatedAt)
    //        FROM TiktokVideoDetails t WHERE t.authorId = :authorId
    //        """)
    // List<TiktokVideoDetailsDTO> findByAuthorId(String authorId);
}
