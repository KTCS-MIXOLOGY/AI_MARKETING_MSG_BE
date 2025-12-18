package com.ai_marketing_msg_be.domain.product.repository;

import com.ai_marketing_msg_be.domain.product.entity.Product;
import com.ai_marketing_msg_be.domain.product.entity.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 상품 Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 전체 상품 목록 조회 (페이징)
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * 카테고리별 상품 조회
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * 재고 상태별 상품 조회
     */
    Page<Product> findByStockStatus(StockStatus stockStatus, Pageable pageable);

    /**
     * 상품명으로 검색 (부분 일치, 대소문자 무시)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * 가격 범위로 검색
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findProductsByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * 재고 있는 상품만 조회
     */
    @Query("SELECT p FROM Product p WHERE p.stockStatus = 'IN_STOCK'")
    List<Product> findAvailableProducts();

    /**
     * 할인 상품만 조회 (할인율 > 0)
     */
    @Query("SELECT p FROM Product p WHERE p.discountRate > 0 ORDER BY p.discountRate DESC")
    List<Product> findDiscountedProducts();

    /**
     * 카테고리와 재고 상태로 검색
     */
    Page<Product> findByCategoryAndStockStatus(String category, StockStatus stockStatus, Pageable pageable);

    /**
     * 상품명 중복 확인
     */
    boolean existsByName(String name);

    /**
     * 상품 ID로 조회 (Optional)
     */
    Optional<Product> findByProductId(Long productId);
}
