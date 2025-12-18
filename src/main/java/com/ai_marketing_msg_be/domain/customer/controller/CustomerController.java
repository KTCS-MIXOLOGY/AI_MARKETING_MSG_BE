package com.ai_marketing_msg_be.domain.customer.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerCountResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerDetailResponse;
import com.ai_marketing_msg_be.domain.customer.dto.CustomerSearchListResponse;
import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import com.ai_marketing_msg_be.domain.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/executor")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @PostMapping("/segments/count")
    public ApiResponse<CustomerCountResponse> getSegmentCount(
            @Valid @RequestBody SegmentFilterRequest request,
            HttpServletRequest httpRequest) {
        log.info("Segment count request: {}", request);
        CustomerCountResponse response = customerService.getSegmentCount(request);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/customers/search")
    public ApiResponse<CustomerSearchListResponse> searchCustomers(
            @RequestParam String searchType,
            @RequestParam String searchValue,
            HttpServletRequest httpRequest) {
        log.info("Customer search request: type={}, value={}", searchType, searchValue);
        CustomerSearchListResponse response = customerService.searchCustomers(searchType, searchValue);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @PathVariable Long customerId,
            HttpServletRequest httpRequest) {
        log.info("Customer detail request: customerId={}", customerId);
        CustomerDetailResponse response = customerService.getCustomerDetail(customerId);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}