package com.ai_marketing_msg_be.domain.product.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.domain.product.dto.*;
import com.ai_marketing_msg_be.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 관리 REST API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회
     */
    @GetMapping("/products")
    @Operation(summary = "상품 목록 조회", description = "모든 상품 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProductList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        log.info("GET /products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ProductDto> response = productService.getProductList(pageable);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/products/{productId}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(
            @Parameter(description = "상품 ID", example = "100")
            @PathVariable Long productId,
            HttpServletRequest request) {

        log.info("GET /products/{} - productId: {}", productId, productId);
        ProductDetailDto response = productService.getProductDetail(productId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 상품 생성
     */
    @PostMapping("/admin/products")
    @Operation(summary = "상품 생성", description = "새로운 상품을 생성합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            HttpServletRequest httpRequest) {

        log.info("POST /admin/products - name: {}", request.getName());
        CreateProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created(response, httpRequest.getRequestURI()));
    }

    /**
     * 상품 수정
     */
    @PutMapping("/admin/products/{productId}")
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateProduct(
            @Parameter(description = "상품 ID", example = "100")
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request,
            HttpServletRequest httpRequest) {

        log.info("PUT /admin/products/{} - name: {}", productId, request.getName());
        UpdateProductResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(ApiResponse.ok(response, httpRequest.getRequestURI()));
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/admin/products/{productId}")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. 품절 상태인 상품만 삭제 가능합니다. (Admin 권한 필요)")
    public ResponseEntity<ApiResponse<DeleteProductResponse>> deleteProduct(
            @Parameter(description = "상품 ID", example = "100")
            @PathVariable Long productId,
            HttpServletRequest request) {

        log.info("DELETE /admin/products/{} - productId: {}", productId, productId);
        DeleteProductResponse response = productService.deleteProduct(productId);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 카테고리별 상품 조회
     */
    @GetMapping("/products/category/{category}")
    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProductsByCategory(
            @Parameter(description = "카테고리", example = "인터넷")
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        log.info("GET /products/category/{} - category: {}", category, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ProductDto> response = productService.getProductsByCategory(category, pageable);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }

    /**
     * 상품명 검색
     */
    @GetMapping("/products/search")
    @Operation(summary = "상품명 검색", description = "상품명으로 검색합니다 (부분 일치).")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> searchProductsByName(
            @Parameter(description = "검색할 상품명", example = "인터넷")
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        log.info("GET /products/search?name={} - searching for: {}", name, name);

        // 빈 문자열이나 공백만 있는 경우 빈 결과 반환
        if (name == null || name.trim().isEmpty()) {
            log.warn("Empty search keyword provided, returning empty result");
            PageResponse<ProductDto> emptyResponse = PageResponse.empty();
            return ResponseEntity.ok(ApiResponse.ok(emptyResponse, request.getRequestURI()));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ProductDto> response = productService.searchProductsByName(name.trim(), pageable);

        return ResponseEntity.ok(ApiResponse.ok(response, request.getRequestURI()));
    }
}
