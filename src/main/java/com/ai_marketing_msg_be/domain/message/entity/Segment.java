package com.ai_marketing_msg_be.domain.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "고객세그먼트")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Segment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "segment_id")
    private Long segmentId;

    @Column(name = "segment_name", length = 100)
    private String segmentName;

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Column(name = "gender", length = 10)
    private String gender;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "regions", columnDefinition = "JSON")
    private List<String> regions;

    @Column(name = "membership_level", length = 20)
    private String membershipLevel;

    @Column(name = "recency_max_days")
    private Integer recencyMaxDays;

    @Column(name = "target_customer_count")
    private Integer targetCustomerCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasAgeFilter() {
        return this.ageMin != null || this.ageMax != null;
    }

    public boolean hasGenderFilter() {
        return this.gender != null && !this.gender.isEmpty();
    }

    public boolean hasRegionFilter() {
        return this.regions != null && !this.regions.isEmpty();
    }

    public boolean hasMembershipFilter() {
        return this.membershipLevel != null && !this.membershipLevel.isEmpty();
    }

    public boolean hasRecencyFilter() {
        return this.recencyMaxDays != null && this.recencyMaxDays > 0;
    }

    public int getActiveFilterCount() {
        int count = 0;
        if (hasAgeFilter()) {
            count++;
        }
        if (hasGenderFilter()) {
            count++;
        }
        if (hasRegionFilter()) {
            count++;
        }
        if (hasMembershipFilter()) {
            count++;
        }
        if (hasRecencyFilter()) {
            count++;
        }
        return count;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        if (hasAgeFilter()) {
            sb.append(String.format("%d~%d세",
                    ageMin != null ? ageMin : 0,
                    ageMax != null ? ageMax : 100));
        }
        if (hasGenderFilter()) {
            sb.append(sb.length() > 0 ? ", " : "");
            sb.append(gender.equals("MALE") ? "남성" : "여성");
        }
        if (hasMembershipFilter()) {
            sb.append(sb.length() > 0 ? ", " : "");
            sb.append(membershipLevel).append(" 등급");
        }
        if (hasRegionFilter()) {
            sb.append(sb.length() > 0 ? ", " : "");
            sb.append(String.join("/", regions));
        }
        if (hasRecencyFilter()) {
            sb.append(sb.length() > 0 ? ", " : "");
            sb.append("최근 ").append(recencyMaxDays).append("일 이내 구매");
        }

        return sb.length() > 0 ? sb.toString() : "전체 고객";
    }

    public void updateTargetCount(Integer count) {
        this.targetCustomerCount = count;
    }
}