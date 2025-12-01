package com.ai_marketing_msg_be.domain.customer.repository;

import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.customer.entity.Gender;
import com.ai_marketing_msg_be.domain.customer.entity.MembershipLevel;
import com.ai_marketing_msg_be.domain.customer.entity.Region;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    public static Specification<Customer> withFilters(SegmentFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getAgeRange() != null) {
                if (filter.getAgeRange().getMin() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("age"), filter.getAgeRange().getMin()));
                }
                if (filter.getAgeRange().getMax() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("age"), filter.getAgeRange().getMax()));
                }
            }

            // 성별 필터
            if (filter.getGender() != null) {
                try {
                    Gender gender = Gender.valueOf(filter.getGender().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
                } catch (IllegalArgumentException e) {
                    // 잘못된 성별 값은 무시
                }
            }

            // 지역 필터 (복수 선택)
            if (filter.getRegions() != null && !filter.getRegions().isEmpty()) {
                List<Region> regions = new ArrayList<>();
                for (String regionStr : filter.getRegions()) {
                    try {
                        regions.add(Region.valueOf(regionStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // 잘못된 지역 값은 무시
                    }
                }
                if (!regions.isEmpty()) {
                    predicates.add(root.get("region").in(regions));
                }
            }

            // 멤버십 등급 필터
            if (filter.getMembershipLevel() != null) {
                try {
                    MembershipLevel level = MembershipLevel.valueOf(filter.getMembershipLevel().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("membershipLevel"), level));
                } catch (IllegalArgumentException e) {
                    // 잘못된 등급 값은 무시
                }
            }

            // 최근 구매일 필터 (recencyMaxDays)
            if (filter.getRecencyMaxDays() != null && filter.getRecencyMaxDays() > 0) {
                LocalDateTime cutoffDate = LocalDateTime.now().minusDays(filter.getRecencyMaxDays());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("lastPurchaseDate"), cutoffDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}