package com.ai_marketing_msg_be.domain.product.service;

import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.product.dto.*;
import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회 (페이징)
     */
    public PageResponse<ProductDto> getProductList(Pageable pageable) {
        log.info("Fetching product list with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        Page<ProductDto> productDtos = products.map(ProductDto::from);
        return PageResponse.from(productDtos);
    }

    /**
     * 상품 상세 조회
     */
    public ProductDetailDto getProductDetail(Long productId) {
        log.info("Fetching product detail for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "상품을 찾을 수 없습니다. productId: " + productId));
        return ProductDetailDto.from(product);
    }

    /**
     * 상품 생성
     */
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

        // 상품명 중복 확인
        if (productRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS,
                    "동일한 이름의 상품이 이미 존재합니다: " + request.getName());
        }

        // Entity 생성 및 검증
        Product product = request.toEntity();
        product.validatePrice();

        // 저장
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with productId: {}", savedProduct.getProductId());

        return CreateProductResponse.from(savedProduct);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public UpdateProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        log.info("Updating product with productId: {}", productId);

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "상품을 찾을 수 없습니다. productId: " + productId));

        // 상품명 변경 시 중복 확인
        if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS,
                    "동일한 이름의 상품이 이미 존재합니다: " + request.getName());
        }

        // 업데이트
        product.update(
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getDiscountRate(),
                request.getBenefits(),
                request.getConditions(),
                request.getStockStatus()
        );

        // 가격 검증
        product.validatePrice();

        log.info("Product updated successfully with productId: {}", productId);
        return UpdateProductResponse.from(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public DeleteProductResponse deleteProduct(Long productId) {
        log.info("Deleting product with productId: {}", productId);

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "상품을 찾을 수 없습니다. productId: " + productId));

        // 삭제 가능 여부 확인 (품절 상태만 삭제 가능)
        if (!product.canBeDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_CANNOT_BE_DELETED,
                    "재고가 있는 상품은 삭제할 수 없습니다. 먼저 품절 상태로 변경해주세요.");
        }

        // 삭제
        productRepository.delete(product);
        log.info("Product deleted successfully with productId: {}", productId);

        return DeleteProductResponse.of(productId);
    }

    /**
     * 카테고리별 상품 조회
     */
    public PageResponse<ProductDto> getProductsByCategory(String category, Pageable pageable) {
        log.info("Fetching products by category: {}", category);
        Page<Product> products = productRepository.findByCategory(category, pageable);
        Page<ProductDto> productDtos = products.map(ProductDto::from);
        return PageResponse.from(productDtos);
    }

    /**
     * 상품명으로 검색
     */
    public PageResponse<ProductDto> searchProductsByName(String name, Pageable pageable) {
        log.info("Searching products by name: {}", name);
        Page<Product> products = productRepository.findByNameContaining(name, pageable);
        Page<ProductDto> productDtos = products.map(ProductDto::from);
        return PageResponse.from(productDtos);
    }
}
