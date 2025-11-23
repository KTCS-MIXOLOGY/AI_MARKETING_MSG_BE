# Product API 명세서

## 📋 개요

**MIXOLOGY** 프로젝트의 상품 관리 API입니다. 통신사 상품(인터넷, 스마트폰, 요금제, 부가서비스 등)의 생성, 조회, 수정, 삭제(CRUD) 기능을 제공합니다.

---

## 🗂️ 도메인 구조

```
domain/product/
├── entity/
│   ├── Product.java              # 상품 엔티티
│   └── StockStatus.java          # 재고 상태 ENUM
├── dto/
│   ├── ProductDto.java           # 상품 목록 조회용 DTO
│   ├── ProductDetailDto.java     # 상품 상세 조회용 DTO
│   ├── CreateProductRequest.java # 상품 생성 요청 DTO
│   ├── CreateProductResponse.java # 상품 생성 응답 DTO
│   ├── UpdateProductRequest.java # 상품 수정 요청 DTO
│   ├── UpdateProductResponse.java # 상품 수정 응답 DTO
│   └── DeleteProductResponse.java # 상품 삭제 응답 DTO
├── repository/
│   └── ProductRepository.java    # 상품 Repository (JPA)
├── service/
│   └── ProductService.java       # 상품 비즈니스 로직
├── controller/
│   └── ProductController.java    # REST API Controller
└── README.md                     # 이 문서
```

---

## 📊 데이터베이스 스키마

### 테이블명: `상품` (Products)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| product_id | BIGINT | PK, AUTO_INCREMENT | 상품 고유 ID |
| name | VARCHAR(100) | NOT NULL | 상품명 (예: 기가 인터넷 500M) |
| category | VARCHAR(50) | | 카테고리 (인터넷/스마트폰/요금제/부가서비스) |
| price | DECIMAL(12,2) | | 가격 |
| discount_rate | DECIMAL(5,2) | | 할인율 (%) |
| benefits | TEXT | | 혜택 내용 |
| conditions | TEXT | | 가입 조건 |
| stock_status | ENUM | | 재고 상태 (IN_STOCK/OUT_OF_STOCK/LIMITED) |
| created_at | DATETIME | NOT NULL | 생성일시 (자동 생성) |
| updated_at | DATETIME | | 수정일시 (자동 업데이트) |

### StockStatus ENUM 값

- `IN_STOCK`: 재고 있음
- `OUT_OF_STOCK`: 품절
- `LIMITED`: 한정 수량

---

## 🔌 API 엔드포인트

### 1. 상품 목록 조회

**GET** `/products`

**설명**: 모든 상품 목록을 페이징하여 조회합니다.

**Query Parameters**:
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "productId": 100,
        "name": "기가 인터넷 500M",
        "category": "인터넷",
        "price": 33000,
        "discountRate": 30.0,
        "stockStatus": "IN_STOCK",
        "createdAt": "2025-10-10T09:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 20,
    "totalPages": 1
  },
  "timestamp": "2025-11-23T15:30:00Z",
  "path": "/products"
}
```

---

### 2. 상품 상세 조회

**GET** `/products/{productId}`

**설명**: 특정 상품의 상세 정보를 조회합니다.

**Path Parameters**:
- `productId`: 상품 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": {
    "productId": 100,
    "name": "기가 인터넷 500M",
    "category": "인터넷",
    "price": 33000,
    "discountRate": 30.0,
    "benefits": "3년 약정 시 월 1.1만 원 할인",
    "conditions": "신규 가입 고객 대상",
    "stockStatus": "IN_STOCK",
    "createdAt": "2025-10-10T09:00:00",
    "updatedAt": "2025-11-16T19:05:00"
  },
  "timestamp": "2025-11-23T15:32:00Z",
  "path": "/products/100"
}
```

---

### 3. 상품 생성

**POST** `/admin/products`

**설명**: 새로운 상품을 생성합니다. (Admin 권한 필요)

**Request Body**:
```json
{
  "name": "기가 인터넷 500M",
  "category": "인터넷",
  "price": 33000,
  "discountRate": 30.0,
  "benefits": "3년 약정 시 월 1.1만 원 할인",
  "conditions": "신규 가입 고객 대상",
  "stockStatus": "IN_STOCK"
}
```

**Response Example**:
```json
{
  "status": 201,
  "success": true,
  "data": {
    "productId": 100,
    "name": "기가 인터넷 500M",
    "category": "인터넷",
    "stockStatus": "IN_STOCK",
    "createdAt": "2025-11-23T15:00:00Z"
  },
  "timestamp": "2025-11-23T15:00:05Z",
  "path": "/admin/products"
}
```

---

### 4. 상품 수정

**PUT** `/admin/products/{productId}`

**설명**: 기존 상품 정보를 수정합니다. (Admin 권한 필요)

**Path Parameters**:
- `productId`: 상품 ID

**Request Body**:
```json
{
  "name": "기가 인터넷 500M",
  "category": "인터넷",
  "price": 33000,
  "discountRate": 30.0,
  "benefits": "3년 약정 시 월 1.1만 원 할인",
  "conditions": "신규 가입 고객 대상",
  "stockStatus": "IN_STOCK"
}
```

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": {
    "productId": 100,
    "name": "기가 인터넷 500M",
    "category": "인터넷",
    "price": 33000,
    "discountRate": 30.0,
    "benefits": "3년 약정 시 월 1.1만 원 할인",
    "conditions": "신규 가입 고객 대상",
    "stockStatus": "IN_STOCK",
    "updatedAt": "2025-11-23T15:05:00Z"
  },
  "timestamp": "2025-11-23T15:05:05Z",
  "path": "/admin/products/100"
}
```

---

### 5. 상품 삭제

**DELETE** `/admin/products/{productId}`

**설명**: 상품을 삭제합니다. **품절 상태(OUT_OF_STOCK)인 상품만 삭제 가능**합니다. (Admin 권한 필요)

**Path Parameters**:
- `productId`: 상품 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": {
    "productId": 100,
    "deleted": true,
    "deletedAt": "2025-11-23T15:10:00Z"
  },
  "timestamp": "2025-11-23T15:10:05Z",
  "path": "/admin/products/100"
}
```

---

### 6. 카테고리별 상품 조회

**GET** `/products/category/{category}`

**설명**: 특정 카테고리의 상품 목록을 조회합니다.

**Path Parameters**:
- `category`: 카테고리명 (예: 인터넷, 스마트폰, 요금제)

**Query Parameters**:
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

---

### 7. 상품명 검색

**GET** `/products/search`

**설명**: 상품명으로 검색합니다 (부분 일치).

**Query Parameters**:
- `name`: 검색할 상품명
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

**Example**: `/products/search?name=인터넷`

---

## 🔒 비즈니스 로직

### 1. 상품명 중복 확인
- 상품 생성/수정 시 동일한 이름의 상품이 이미 존재하는지 확인
- 중복 시 `PRODUCT_ALREADY_EXISTS` 에러 발생

### 2. 가격 유효성 검증
- 가격은 0 이상이어야 함
- 할인율은 0~100 사이여야 함
- 검증 실패 시 `INVALID_PRODUCT_PRICE` 또는 `INVALID_DISCOUNT_RATE` 에러 발생

### 3. 삭제 가능 여부 확인
- **품절 상태(OUT_OF_STOCK)인 상품만 삭제 가능**
- 재고가 있는 상품 삭제 시도 시 `PRODUCT_CANNOT_BE_DELETED` 에러 발생
- 이는 실제 사용 중인 상품을 실수로 삭제하는 것을 방지하기 위한 안전장치

### 4. 할인된 최종 가격 계산
- Entity에 `getDiscountedPrice()` 메서드 제공
- 할인율 적용한 최종 가격을 자동 계산

---

## ⚠️ 에러 코드

| 에러 코드 | HTTP Status | 설명 |
|-----------|-------------|------|
| PRODUCT_NOT_FOUND | 404 | 상품을 찾을 수 없음 |
| PRODUCT_ALREADY_EXISTS | 409 | 동일한 이름의 상품이 이미 존재 |
| PRODUCT_CANNOT_BE_DELETED | 400 | 상품을 삭제할 수 없음 (재고 있음) |
| INVALID_PRODUCT_PRICE | 400 | 유효하지 않은 가격 |
| INVALID_DISCOUNT_RATE | 400 | 유효하지 않은 할인율 |
| OUT_OF_STOCK | 400 | 품절 상태 |

---

## 🧪 테스트

### Swagger UI로 테스트

1. 애플리케이션 실행:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

2. Swagger UI 접속:
```
http://localhost:8080/swagger-ui/index.html
```

3. **Product** 섹션에서 다음 API 테스트 가능:
   - GET `/products` - 상품 목록 조회
   - GET `/products/{productId}` - 상품 상세 조회
   - POST `/admin/products` - 상품 생성
   - PUT `/admin/products/{productId}` - 상품 수정
   - DELETE `/admin/products/{productId}` - 상품 삭제
   - GET `/products/category/{category}` - 카테고리별 조회
   - GET `/products/search` - 상품명 검색

---

## 📝 사용 예시

### 1. 상품 생성 (cURL)

```bash
curl -X POST "http://localhost:8080/admin/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "5G 프리미엄 요금제",
    "category": "요금제",
    "price": 75000,
    "discountRate": 20.0,
    "benefits": "월 100GB 데이터 제공",
    "conditions": "24개월 약정",
    "stockStatus": "IN_STOCK"
  }'
```

### 2. 상품 목록 조회 (cURL)

```bash
curl -X GET "http://localhost:8080/products?page=0&size=10"
```

### 3. 카테고리별 조회 (cURL)

```bash
curl -X GET "http://localhost:8080/products/category/인터넷?page=0&size=10"
```

### 4. 상품명 검색 (cURL)

```bash
curl -X GET "http://localhost:8080/products/search?name=인터넷"
```

---

## ✅ 구현 완료 내역 (2025-11-23)

### 구현된 컴포넌트
- ✅ **Entity**: Product, StockStatus
- ✅ **DTO**: 7개 (ProductDto, ProductDetailDto, CreateRequest/Response, UpdateRequest/Response, DeleteResponse)
- ✅ **Repository**: ProductRepository (커스텀 쿼리 메서드 10개 포함)
- ✅ **Service**: ProductService (CRUD + 검색 로직 완성)
- ✅ **Controller**: ProductController (7개 RESTful API)
- ✅ **Swagger**: OpenAPI 문서 자동 생성

### 주요 특징
1. **재고 상태 관리**: StockStatus ENUM으로 IN_STOCK/OUT_OF_STOCK/LIMITED 관리
2. **표준화된 응답**: 모든 API가 ApiResponse<T> 포맷 사용
3. **페이징 지원**: Spring Data의 Page를 PageResponse로 변환
4. **JPA Auditing**: created_at, updated_at 자동 관리
5. **비즈니스 로직**: Entity에 검증 및 가격 계산 로직 포함
6. **예외 처리**: BusinessException + ErrorCode로 일관된 오류 응답
7. **삭제 보호**: 품절 상태만 삭제 가능하여 실수 방지

### 기술 스택
- **Spring Boot**: 3.5.7
- **Java**: 21
- **JPA/Hibernate**: 6.6.33
- **MySQL**: 9.5
- **Springdoc OpenAPI**: 2.7.0

### 데이터베이스
- **Database**: mixology
- **Table**: 상품 (자동 생성 완료)
- **Connection**: localhost:3306

### 실행 환경
- **URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Profile**: dev

### 완료된 테스트
- ✅ 애플리케이션 정상 시작
- ✅ MySQL 연결 성공
- ✅ 상품 테이블 자동 생성
- ✅ Swagger UI 정상 작동
- ✅ 7개 API 엔드포인트 노출 확인

---

## 🔄 향후 개선사항

1. **JWT 인증/인가 구현** - 현재 임시로 전체 허용 중
2. **상품 이미지 업로드** - 파일 업로드 기능 추가
3. **재고 수량 관리** - 단순 상태가 아닌 정확한 수량 관리
4. **상품 검색 개선** - Elasticsearch 연동으로 고급 검색 기능
5. **캐싱 전략** - Redis 연동으로 조회 성능 향상
6. **배치 작업** - 품절 상품 자동 처리
7. **테스트 코드** - 단위 테스트 및 통합 테스트 작성

---

## 📞 문의

상품 API 관련 문의사항은 MIXOLOGY 개발팀에 문의해주세요.

**Generated Date**: 2025-11-23
