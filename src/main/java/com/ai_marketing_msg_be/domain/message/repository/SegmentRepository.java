package com.ai_marketing_msg_be.domain.message.repository;

import com.ai_marketing_msg_be.domain.message.entity.Segment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SegmentRepository extends JpaRepository<Segment, Long> {

    @Query("""
            SELECT s FROM Segment s
            WHERE (:ageMin IS NULL AND s.ageMin IS NULL OR s.ageMin = :ageMin)
              AND (:ageMax IS NULL AND s.ageMax IS NULL OR s.ageMax = :ageMax)
              AND (:gender IS NULL AND s.gender IS NULL OR s.gender = :gender)
              AND (:membershipLevel IS NULL AND s.membershipLevel IS NULL OR s.membershipLevel = :membershipLevel)
              AND (:recencyMaxDays IS NULL AND s.recencyMaxDays IS NULL OR s.recencyMaxDays = :recencyMaxDays)
            """)
    List<Segment> findByFilters(
            @Param("ageMin") Integer ageMin,
            @Param("ageMax") Integer ageMax,
            @Param("gender") String gender,
            @Param("membershipLevel") String membershipLevel,
            @Param("recencyMaxDays") Integer recencyMaxDays
    );

    Optional<Segment> findBySegmentId(Long segmentId);
}