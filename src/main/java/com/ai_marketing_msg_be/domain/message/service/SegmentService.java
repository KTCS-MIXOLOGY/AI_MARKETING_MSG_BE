package com.ai_marketing_msg_be.domain.message.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerCountResponse;
import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.service.CustomerService;
import com.ai_marketing_msg_be.domain.message.entity.Segment;
import com.ai_marketing_msg_be.domain.message.repository.SegmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final CustomerService customerService;

    @Transactional
    public Segment findOrCreateSegment(SegmentFilterRequest filter) {
        log.info("Finding or creating segment with filter: {}", filter);

        Integer ageMin = filter.getAgeRange() != null ? filter.getAgeRange().getMin() : null;
        Integer ageMax = filter.getAgeRange() != null ? filter.getAgeRange().getMax() : null;

        List<Segment> existingSegments = segmentRepository.findByFilters(
                ageMin,
                ageMax,
                filter.getGender(),
                filter.getMembershipLevel(),
                filter.getRecencyMaxDays()
        );

        for (Segment segment : existingSegments) {
            if (isRegionsEqual(segment.getRegions(), filter.getRegions())) {
                log.info("Found existing segment: segmentId={}", segment.getSegmentId());
                return segment;
            }
        }

        log.info("Creating new segment");

        CustomerCountResponse countResponse = customerService.getSegmentCount(filter);
        int customerCount = countResponse.getTotalCount().intValue();

        Segment newSegment = Segment.builder()
                .ageMin(ageMin)
                .ageMax(ageMax)
                .gender(filter.getGender())
                .regions(filter.getRegions())
                .membershipLevel(filter.getMembershipLevel())
                .recencyMaxDays(filter.getRecencyMaxDays())
                .targetCustomerCount(customerCount)
                .build();

        Segment savedSegment = segmentRepository.save(newSegment);
        log.info("New segment created: segmentId={}, customerCount={}",
                savedSegment.getSegmentId(), customerCount);

        return savedSegment;
    }

    @Transactional(readOnly = true)
    public Segment getSegment(Long segmentId) {
        return segmentRepository.findBySegmentId(segmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEGMENT_NOT_FOUND));
    }

    private boolean isRegionsEqual(List<String> regions1, List<String> regions2) {
        if (regions1 == null && regions2 == null) {
            return true;
        }
        if (regions1 == null || regions2 == null) {
            return false;
        }
        if (regions1.size() != regions2.size()) {
            return false;
        }

        return regions1.containsAll(regions2) && regions2.containsAll(regions1);
    }
}