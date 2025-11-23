# CampaignProduct API 명세서

## 📋 개요

**MIXOLOGY** 프로젝트의 캠페인-상품 매핑 API입니다. 캠페인과 상품을 연결하여 어떤 캠페인에 어떤 상품이 포함되는지 관리하는 기능을 제공합니다.

---

## 🗂️ 도메인 구조

```
domain/campaign_product/
├── entity/
│   ├── CampaignProduct.java          # 캠페인-상품 매핑 엔티티
│   └── CampaignProductId.java        # 복합키 (Composite Key)
├── dto/
│   ├── CampaignProductDto.java       # 매핑 조회용 DTO
│   ├── AddProductToCampaignRequest.java    # 상품 추가 요청 DTO
│   ├── AddProductToCampaignResponse.java   # 상품 추가 응답 DTO
│   └── RemoveProductFromCampaignResponse.java # 상품 제거 응답 DTO
├── repository/
│   └── CampaignProductRepository.java # 매핑 Repository (JPA)
├── service/
│   └── CampaignProductService.java   # 비즈니스 로직
├── controller/
│   └── CampaignProductController.java # REST API Controller
└── README.md                         # 이 문서
```

---

## 📊 데이터베이스 스키마

### 테이블명: `캠페인_상품_매핑` (Campaign_Products)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| campaign_id | BIGINT | PK, FK | 캠페인 ID (외래키 → 캠페인) |
| product_id | BIGINT | PK, FK | 상품 ID (외래키 → 상품) |
| created_at | DATETIME | NOT NULL | 생성일시 (자동 생성) |
| updated_at | DATETIME | | 수정일시 (자동 업데이트) |

### 복합 Primary Key
- **(campaign_id, product_id)** - 캠페인과 상품의 조합이 유일해야 함

### Foreign Key 제약조건
- `campaign_id` → `캠페인(campaign_id)` ON DELETE CASCADE
- `product_id` → `상품(product_id)` ON DELETE CASCADE

---

## 🔌 API 엔드포인트

### 1. 캠페인에 상품 추가

**POST** `/admin/campaigns/{campaignId}/products`

**설명**: 특정 캠페인에 상품을 추가합니다. (Admin 권한 필요)

**Path Parameters**:
- `campaignId`: 캠페인 ID

**Request Body**:
```json
{
  "productId": 1
}
```

**Response Example**:
```json
{
  "status": 201,
  "success": true,
  "data": {
    "campaignId": 1,
    "productId": 1,
    "message": "상품이 캠페인에 추가되었습니다",
    "createdAt": "2025-11-23T16:30:28Z"
  },
  "timestamp": "2025-11-23T07:30:28Z",
  "path": "/admin/campaigns/1/products"
}
```

---

### 2. 캠페인에서 상품 제거

**DELETE** `/admin/campaigns/{campaignId}/products/{productId}`

**설명**: 특정 캠페인에서 상품을 제거합니다. (Admin 권한 필요)

**Path Parameters**:
- `campaignId`: 캠페인 ID
- `productId`: 상품 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": {
    "campaignId": 1,
    "productId": 1,
    "removed": true,
    "message": "상품이 캠페인에서 제거되었습니다",
    "removedAt": "2025-11-23T16:30:58Z"
  },
  "timestamp": "2025-11-23T07:30:58Z",
  "path": "/admin/campaigns/1/products/1"
}
```

---

### 3. 캠페인의 모든 상품 조회

**GET** `/campaigns/{campaignId}/products`

**설명**: 특정 캠페인에 등록된 모든 상품을 조회합니다.

**Path Parameters**:
- `campaignId`: 캠페인 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": [
    {
      "campaignId": 1,
      "campaignName": "봄맞이 인터넷 프로모션",
      "productId": 1,
      "productName": "기가 인터넷 500M",
      "productCategory": "인터넷",
      "createdAt": "2025-11-23T16:30:28"
    }
  ],
  "timestamp": "2025-11-23T07:30:35Z",
  "path": "/campaigns/1/products"
}
```

---

### 4. 상품이 포함된 모든 캠페인 조회

**GET** `/products/{productId}/campaigns`

**설명**: 특정 상품이 포함된 모든 캠페인을 조회합니다.

**Path Parameters**:
- `productId`: 상품 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": [
    {
      "campaignId": 1,
      "campaignName": "봄맞이 인터넷 프로모션",
      "productId": 1,
      "productName": "기가 인터넷 500M",
      "productCategory": "인터넷",
      "createdAt": "2025-11-23T16:30:28"
    }
  ],
  "timestamp": "2025-11-23T07:30:42Z",
  "path": "/products/1/campaigns"
}
```

---

### 5. 캠페인의 상품 개수 조회

**GET** `/campaigns/{campaignId}/products/count`

**설명**: 특정 캠페인에 등록된 상품의 개수를 조회합니다.

**Path Parameters**:
- `campaignId`: 캠페인 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": 1,
  "timestamp": "2025-11-23T07:30:44Z",
  "path": "/campaigns/1/products/count"
}
```

---

### 6. 상품이 포함된 캠페인 개수 조회

**GET** `/products/{productId}/campaigns/count`

**설명**: 특정 상품이 포함된 캠페인의 개수를 조회합니다.

**Path Parameters**:
- `productId`: 상품 ID

**Response Example**:
```json
{
  "status": 200,
  "success": true,
  "data": 1,
  "timestamp": "2025-11-23T07:31:05Z",
  "path": "/products/1/campaigns/count"
}
```

---

## 🔒 비즈니스 로직

### 1. 중복 방지
- 동일한 캠페인에 동일한 상품을 중복 추가할 수 없음
- 중복 시도 시 `CAMPAIGN_PRODUCT_ALREADY_EXISTS` (409) 에러 발생

### 2. 참조 무결성 검증
- 캠페인 ID가 존재하지 않으면 `CAMPAIGN_NOT_FOUND` (404) 에러
- 상품 ID가 존재하지 않으면 `PRODUCT_NOT_FOUND` (404) 에러

### 3. 양방향 조회 지원
- 캠페인 → 상품 조회: 특정 캠페인에 어떤 상품들이 포함되어 있는지
- 상품 → 캠페인 조회: 특정 상품이 어떤 캠페인에 포함되어 있는지

### 4. 복합키 (Composite Key) 사용
- `CampaignProductId`로 (campaignId, productId) 조합을 관리
- JPA `@EmbeddedId`와 `@MapsId` 활용

---

## ⚠️ 에러 코드

| 에러 코드 | HTTP Status | 설명 |
|-----------|-------------|------|
| CAMPAIGN_NOT_FOUND | 404 | 캠페인을 찾을 수 없음 |
| PRODUCT_NOT_FOUND | 404 | 상품을 찾을 수 없음 |
| CAMPAIGN_PRODUCT_ALREADY_EXISTS | 409 | 이미 캠페인에 추가된 상품 |
| CAMPAIGN_PRODUCT_NOT_FOUND | 404 | 캠페인-상품 매핑을 찾을 수 없음 |

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

3. **CampaignProduct** 섹션에서 다음 API 테스트 가능:
   - POST `/admin/campaigns/{campaignId}/products` - 캠페인에 상품 추가
   - DELETE `/admin/campaigns/{campaignId}/products/{productId}` - 캠페인에서 상품 제거
   - GET `/campaigns/{campaignId}/products` - 캠페인의 상품 조회
   - GET `/products/{productId}/campaigns` - 상품이 포함된 캠페인 조회
   - GET `/campaigns/{campaignId}/products/count` - 캠페인의 상품 개수
   - GET `/products/{productId}/campaigns/count` - 상품이 포함된 캠페인 개수

---

## 📝 사용 예시

### 1. 캠페인에 상품 추가 (cURL)

```bash
curl -X POST "http://localhost:8080/admin/campaigns/1/products" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1
  }'
```

### 2. 캠페인의 상품 목록 조회 (cURL)

```bash
curl -X GET "http://localhost:8080/campaigns/1/products"
```

### 3. 상품이 포함된 캠페인 조회 (cURL)

```bash
curl -X GET "http://localhost:8080/products/1/campaigns"
```

### 4. 캠페인에서 상품 제거 (cURL)

```bash
curl -X DELETE "http://localhost:8080/admin/campaigns/1/products/1"
```

---

## ✅ 구현 완료 내역 (2025-11-23)

### 구현된 컴포넌트
- ✅ **Entity**: CampaignProduct, CampaignProductId (복합키)
- ✅ **DTO**: 4개 (CampaignProductDto, AddRequest/Response, RemoveResponse)
- ✅ **Repository**: CampaignProductRepository (5개 커스텀 쿼리)
- ✅ **Service**: CampaignProductService (완전한 CRUD 로직)
- ✅ **Controller**: CampaignProductController (6개 RESTful API)
- ✅ **Swagger**: OpenAPI 문서 자동 생성

### 주요 특징
1. **복합키 (Composite Key)**: @EmbeddedId로 (campaignId, productId) 관리
2. **외래키 제약조건**: Campaign, Product와 참조 무결성 보장
3. **중복 방지**: 동일 캠페인-상품 조합 중복 추가 불가
4. **양방향 조회**: 캠페인→상품, 상품→캠페인 모두 지원
5. **JOIN FETCH**: N+1 문제 방지를 위한 Fetch Join 사용
6. **개수 조회**: COUNT 쿼리로 효율적인 통계 제공

### 기술 스택
- **Spring Boot**: 3.5.7
- **Java**: 21
- **JPA/Hibernate**: 6.6.33
- **MySQL**: 9.5
- **복합키**: @EmbeddedId + @MapsId

### 데이터베이스
- **Database**: mixology
- **Table**: 캠페인_상품_매핑 (자동 생성 완료)
- **Foreign Keys**: campaign_id, product_id

### 테스트 완료
- ✅ 캠페인에 상품 추가
- ✅ 중복 추가 방지 (409 에러)
- ✅ 캠페인의 상품 목록 조회
- ✅ 상품이 포함된 캠페인 조회
- ✅ 개수 조회 (캠페인별, 상품별)
- ✅ 캠페인에서 상품 제거
- ✅ 참조 무결성 검증 (존재하지 않는 ID 처리)

---

## 🔄 향후 개선사항

1. **배치 작업** - 여러 상품을 한번에 추가/제거하는 API
2. **캠페인 상품 순서** - 상품 표시 순서 관리 (display_order 컬럼 추가)
3. **캠페인 상품 메타데이터** - 특정 캠페인에서 상품의 특별 가격, 할인율 등
4. **소프트 삭제** - 매핑 삭제 시 히스토리 유지
5. **검색 기능** - 카테고리별, 기간별 매핑 검색

---

## 📞 문의

CampaignProduct API 관련 문의사항은 MIXOLOGY 개발팀에 문의해주세요.

**Generated Date**: 2025-11-23
