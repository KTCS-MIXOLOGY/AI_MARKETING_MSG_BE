package com.ai_marketing_msg_be.domain.customer.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerCountResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerDetailResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerSearchListResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerSearchResponse;
import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.ai_marketing_msg_be.domain.customer.entity.SearchType;
import com.ai_marketing_msg_be.domain.customer.repository.CustomerRepository;
import com.ai_marketing_msg_be.domain.customer.repository.CustomerSpecification;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomerCountResponse getSegmentCount(SegmentFilterRequest filter) {
        log.info("Getting segment count with filters: {}", filter);

        Specification<Customer> spec = CustomerSpecification.withFilters(filter);
        long totalCount = customerRepository.count(spec);

        log.info("Segment count result: {} customers", totalCount);

        return CustomerCountResponse.of(totalCount, filter);
    }

    @Transactional(readOnly = true)
    public CustomerSearchListResponse searchCustomers(String searchType, String searchValue) {
        log.info("Searching customers: type={}, value={}", searchType, searchValue);

        SearchType type = parseSearchType(searchType);
        List<Customer> customers = switch (type) {
            case ID -> searchById(searchValue);
            case PHONE -> searchByPhone(searchValue);
            case NAME -> searchByName(searchValue);
        };

        List<CustomerSearchResponse> responses = customers.stream()
                .map(CustomerSearchResponse::from)
                .collect(Collectors.toList());

        log.info("Search result: {} customers found", responses.size());

        return CustomerSearchListResponse.of(responses);
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long customerId) {
        log.info("Getting customer detail: customerId={}", customerId);

        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        log.info("Customer detail found: customerId={}, name={}", customerId, customer.getName());

        return CustomerDetailResponse.from(customer);
    }

    @Transactional
    public int countBySegmentFilter(SegmentFilterRequest filter) {
        log.info("Counting customers by segment filter: {}", filter);

        Specification<Customer> spec = CustomerSpecification.withFilters(filter);
        long count = customerRepository.count(spec);

        log.info("Customer count result: {} customers", count);

        return (int) count;
    }


    private SearchType parseSearchType(String searchTypeStr) {
        try {
            return SearchType.valueOf(searchTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid search type: {}", searchTypeStr);
            throw new BusinessException(ErrorCode.INVALID_SEARCH_TYPE);
        }
    }

    private List<Customer> searchById(String id) {
        try {
            Long customerId = Long.parseLong(id);
            return customerRepository.findByCustomerId(customerId)
                    .map(List::of)
                    .orElse(List.of());
        } catch (NumberFormatException e) {
            log.warn("Invalid customer ID format: {}", id);
            return List.of();
        }
    }

    private List<Customer> searchByPhone(String phone) {
        return customerRepository.findByPhoneContaining(phone);
    }

    private List<Customer> searchByName(String name) {
        return customerRepository.findByNameContaining(name);
    }
}