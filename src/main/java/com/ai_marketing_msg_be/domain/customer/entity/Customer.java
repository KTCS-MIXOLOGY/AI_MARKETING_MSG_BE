package com.ai_marketing_msg_be.domain.customer.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "고객")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phone", length = 30, nullable = false)
    private String phone;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "VARCHAR(10)")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", columnDefinition = "VARCHAR(20)")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level", columnDefinition = "VARCHAR(20)")
    private MembershipLevel membershipLevel;

    @Column(name = "current_plan", length = 100)
    private String currentPlan;

    @Column(name = "current_device", length = 100)
    private String currentDevice;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "avg_data_usage_gb", precision = 10, scale = 2)
    private BigDecimal avgDataUsageGb;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;

    public Integer getRecencyDays() {
        if (this.lastPurchaseDate == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                this.lastPurchaseDate.toLocalDate(),
                LocalDate.now()
        );
    }

    public boolean isContractExpiringSoon(int daysThreshold) {
        if (this.contractEndDate == null) {
            return false;
        }
        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(),
                this.contractEndDate
        );
        return daysUntilExpiry > 0 && daysUntilExpiry <= daysThreshold;
    }

    public boolean isHighDataUser(BigDecimal threshold) {
        if (this.avgDataUsageGb == null) {
            return false;
        }
        return this.avgDataUsageGb.compareTo(threshold) >= 0;
    }
}