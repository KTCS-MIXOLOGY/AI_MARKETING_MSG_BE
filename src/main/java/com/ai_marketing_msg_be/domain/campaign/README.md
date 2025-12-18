# Campaign Domain

캠페인 관리 도메인 - 마케팅 캠페인의 전체 생명주기를 관리합니다.

---

## 도메인 개요

Campaign 도메인은 MIXOLOGY 플랫폼의 핵심 도메인으로, 마케팅 캠페인의 생성, 수정, 조회, 삭제 및 상태 관리를 담당합니다.

### 주요 기능
- 캠페인 CRUD (생성, 조회, 수정, 삭제)
- 캠페인 상태 관리 (DRAFT → ACTIVE → COMPLETED/CANCELLED)
- 캠페인 타입 분류 (신규유치, 고객유지, 업셀링, 크로스셀링, 이탈방지)
- 페이징 기반 목록 조회
- 날짜 범위 검증
- 권한 기반 접근 제어 (작성자만 수정/삭제 가능)

---

## 패키지 구조

```
campaign/
├── entity/
│   ├── Campaign.java           # 캠페인 엔티티
│   ├── CampaignType.java       # 캠페인 타입 ENUM
│   └── CampaignStatus.java     # 캠페인 상태 ENUM
│
├── dto/
│   ├── CampaignDto.java                # 조회용 DTO
│   ├── CreateCampaignRequest.java      # 생성 요청 DTO
│   ├── CreateCampaignResponse.java     # 생성 응답 DTO
│   ├── UpdateCampaignRequest.java      # 수정 요청 DTO
│   ├── UpdateCampaignResponse.java     # 수정 응답 DTO
│   └── DeleteCampaignResponse.java     # 삭제 응답 DTO
│
├── repository/
│   └── CampaignRepository.java  # 데이터 접근 계층
│
├── service/
│   └── CampaignService.java     # 비즈니스 로직 계층
│
└── controller/
    └── CampaignController.java  # API 엔드포인트
```

---

## 데이터베이스 스키마

### 테이블: `캠페인`

| 컬럼명 | 타입 | NULL | 설명 |
|--------|------|------|------|
| `campaign_id` | BIGINT | NOT NULL | 캠페인 ID (PK, Auto Increment) |
| `user_id` | BIGINT | NOT NULL | 생성자 ID (FK → users) |
| `name` | VARCHAR(100) | NOT NULL | 캠페인명 |
| `type` | VARCHAR(50) | NULL | 캠페인 타입 |
| `description` | TEXT | NULL | 캠페인 설명 |
| `start_date` | DATE | NULL | 시작일 |
| `end_date` | DATE | NULL | 종료일 |
| `status` | VARCHAR(20) | NOT NULL | 캠페인 상태 |
| `created_at` | DATETIME | NOT NULL | 생성일시 (자동) |
| `updated_at` | DATETIME | NULL | 수정일시 (자동) |

### 인덱스
- PRIMARY KEY: `campaign_id`
- INDEX: `user_id` (생성자별 조회 최적화)
- INDEX: `status` (상태별 조회 최적화)
- INDEX: `created_at` (최신순 정렬 최적화)

---

## Entity 상세

### Campaign.java

**주요 필드:**
- `campaignId`: 캠페인 고유 ID
- `userId`: 캠페인 생성자 ID
- `name`: 캠페인명
- `type`: 캠페인 타입 (CampaignType ENUM)
- `status`: 캠페인 상태 (CampaignStatus ENUM)
- `startDate`, `endDate`: 캠페인 기간

**비즈니스 로직:**
```java
// 캠페인 정보 업데이트
public void update(String name, CampaignType type, String description,
                  LocalDate startDate, LocalDate endDate, CampaignStatus status)

// 캠페인 상태 변경
public void updateStatus(CampaignStatus status)

// 활성 캠페인 여부 확인
public boolean isActive()

// 삭제 가능 여부 확인 (DRAFT 또는 CANCELLED만 삭제 가능)
public boolean canBeDeleted()

// 날짜 범위 검증 (시작일 < 종료일)
public void validateDateRange()
```

---

### CampaignType (ENUM)

캠페인의 마케팅 목적을 분류합니다.

| 값 | 한글명 | 설명 |
|----|--------|------|
| `NEW_CUSTOMER` | 신규유치 | 신규 고객 확보를 위한 캠페인 |
| `RETENTION` | 고객유지 | 기존 고객 유지 캠페인 |
| `UPSELLING` | 업셀링 | 상위 상품/서비스 판매 캠페인 |
| `CROSS_SELLING` | 크로스셀링 | 관련 상품 교차 판매 캠페인 |
| `CHURN_PREVENTION` | 이탈방지 | 고객 이탈 방지 캠페인 |

**JSON 직렬화:**
- Request: `"업셀링"` (한글명) → `UPSELLING` (ENUM)
- Response: `"업셀링"` (한글명으로 반환)

---

### CampaignStatus (ENUM)

캠페인의 현재 상태를 나타냅니다.

| 값 | 한글명 | 설명 |
|----|--------|------|
| `DRAFT` | 초안 | 작성 중인 캠페인 (삭제 가능) |
| `ACTIVE` | 활성 | 실행 중인 캠페인 (삭제 불가) |
| `COMPLETED` | 완료 | 종료된 캠페인 (삭제 불가) |
| `CANCELLED` | 취소 | 취소된 캠페인 (삭제 가능) |

**상태 전이 규칙:**
```
DRAFT → ACTIVE → COMPLETED
  ↓
CANCELLED
```

**삭제 가능 상태:**
- ✅ `DRAFT`: 삭제 가능
- ❌ `ACTIVE`: 삭제 불가
- ❌ `COMPLETED`: 삭제 불가
- ✅ `CANCELLED`: 삭제 가능

---

## 📡 API 명세

### 1. 캠페인 목록 조회

**Endpoint:** `GET /campaigns`

**Query Parameters:**
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 20): 페이지 크기

**Response (200 OK):**
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "campaignId": 10,
        "name": "5G 전환 프로모션",
        "type": "업셀링",
        "description": "LTE 이용 고객 대상 5G 전환 프로모션",
        "startDate": "2025-11-01",
        "endDate": "2025-11-30",
        "status": "ACTIVE",
        "createdBy": 1,
        "createdAt": "2025-10-20T10:00:00",
        "updatedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  },
  "timestamp": "2025-11-16T16:45:00Z",
  "path": "/campaigns"
}
```

---

### 2. 캠페인 상세 조회

**Endpoint:** `GET /campaigns/{campaignId}`

**Path Parameters:**
- `campaignId` (Long): 캠페인 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "success": true,
  "data": {
    "campaignId": 10,
    "name": "5G 전환 프로모션",
    "type": "업셀링",
    "description": "LTE 이용 고객 대상 5G 전환 프로모션",
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "status": "ACTIVE",
    "createdBy": 1,
    "createdAt": "2025-10-20T10:00:00",
    "updatedAt": null
  },
  "timestamp": "2025-11-16T17:10:00Z",
  "path": "/campaigns/10"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "success": false,
  "message": "Campaign not found",
  "timestamp": "2025-11-16T17:10:00Z",
  "path": "/campaigns/999"
}
```

---

### 3. 캠페인 생성 (Admin)

**Endpoint:** `POST /admin/campaigns`

**Request Body:**
```json
{
  "name": "5G 전환 프로모션",
  "type": "업셀링",
  "description": "LTE 이용 고객 대상 5G 전환 프로모션",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "status": "DRAFT"
}
```

**Validation Rules:**
- `name`: 필수, 최대 100자
- `type`: 필수, CampaignType ENUM 값
- `description`: 선택
- `startDate`, `endDate`: 선택, yyyy-MM-dd 형식
- `status`: 선택 (기본값: DRAFT)

**Response (201 Created):**
```json
{
  "status": 201,
  "success": true,
  "data": {
    "campaignId": 10,
    "name": "5G 전환 프로모션",
    "status": "DRAFT",
    "createdBy": 1,
    "createdAt": "2025-10-20T10:00:00"
  },
  "timestamp": "2025-11-16T17:30:00Z",
  "path": "/admin/campaigns"
}
```

**Error Response (400 Bad Request - 날짜 검증 실패):**
```json
{
  "status": 400,
  "success": false,
  "message": "Invalid campaign date range",
  "timestamp": "2025-11-16T17:30:00Z",
  "path": "/admin/campaigns"
}
```

---

### 4. 캠페인 수정 (Admin)

**Endpoint:** `PUT /admin/campaigns/{campaignId}`

**Path Parameters:**
- `campaignId` (Long): 캠페인 ID

**Request Body:**
```json
{
  "name": "5G 전환 프로모션 (수정)",
  "type": "업셀링",
  "description": "수정된 설명",
  "startDate": "2025-11-01",
  "endDate": "2025-12-31",
  "status": "ACTIVE"
}
```

**Validation Rules:**
- `name`: 필수, 최대 100자
- `type`: 필수
- `status`: 필수
- 날짜 범위 검증: `startDate` < `endDate`

**Response (200 OK):**
```json
{
  "status": 200,
  "success": true,
  "data": {
    "campaignId": 10,
    "name": "5G 전환 프로모션 (수정)",
    "status": "ACTIVE",
    "createdBy": 1,
    "updatedBy": 1,
    "createdAt": "2025-10-20T10:00:00",
    "updatedAt": "2025-11-16T17:45:00"
  },
  "timestamp": "2025-11-16T17:45:01Z",
  "path": "/admin/campaigns/10"
}
```

**Error Response (403 Forbidden - 권한 없음):**
```json
{
  "status": 403,
  "success": false,
  "message": "해당 캠페인을 수정할 권한이 없습니다.",
  "timestamp": "2025-11-16T17:45:00Z",
  "path": "/admin/campaigns/10"
}
```

---

### 5. 캠페인 삭제 (Admin)

**Endpoint:** `DELETE /admin/campaigns/{campaignId}`

**Path Parameters:**
- `campaignId` (Long): 캠페인 ID

**삭제 조건:**
- ✅ 상태가 `DRAFT` 또는 `CANCELLED`인 경우만 삭제 가능
- ❌ `ACTIVE` 또는 `COMPLETED` 상태는 삭제 불가
- ✅ 본인이 생성한 캠페인만 삭제 가능

**Response (200 OK):**
```json
{
  "status": 200,
  "success": true,
  "data": {
    "campaignId": 10,
    "deleted": true,
    "deletedAt": "2025-11-16T19:10:00",
    "deletedBy": 1
  },
  "timestamp": "2025-11-16T19:10:05Z",
  "path": "/admin/campaigns/10"
}
```

**Error Response (400 Bad Request - 삭제 불가):**
```json
{
  "status": 400,
  "success": false,
  "message": "ACTIVE 또는 COMPLETED 상태의 캠페인은 삭제할 수 없습니다.",
  "timestamp": "2025-11-16T19:10:00Z",
  "path": "/admin/campaigns/10"
}
```

---

## 🔍 Repository 메서드

### 기본 메서드 (JpaRepository 상속)
- `save(Campaign)`: 캠페인 저장/수정
- `findById(Long)`: ID로 조회
- `findAll(Pageable)`: 전체 조회 (페이징)
- `delete(Campaign)`: 캠페인 삭제

### 커스텀 쿼리 메서드

```java
// 사용자별 캠페인 조회
Page<Campaign> findByUserId(Long userId, Pageable pageable);

// 상태별 캠페인 조회
Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

// 캠페인명 검색
Page<Campaign> findByNameContaining(String name, Pageable pageable);

// 특정 기간 내 캠페인 조회
@Query("SELECT c FROM Campaign c WHERE c.startDate <= :endDate AND c.endDate >= :startDate")
List<Campaign> findCampaignsInDateRange(
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);

// 현재 활성 캠페인 조회
@Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :today AND c.endDate >= :today")
List<Campaign> findActiveCampaigns(@Param("today") LocalDate today);

// 캠페인명 중복 확인
boolean existsByNameAndUserId(String name, Long userId);

// 사용자 + ID로 조회 (권한 확인용)
Optional<Campaign> findByCampaignIdAndUserId(Long campaignId, Long userId);
```

---

## 🛡️ 비즈니스 규칙

### 1. 캠페인 생성
- ✅ 캠페인명은 필수이며 100자 이내
- ✅ 동일 사용자가 같은 이름의 캠페인 생성 시 예외 발생
- ✅ 시작일이 종료일보다 늦으면 예외 발생
- ✅ 상태 미지정 시 기본값 `DRAFT` 적용

### 2. 캠페인 수정
- ✅ 본인이 생성한 캠페인만 수정 가능
- ✅ 날짜 범위 재검증
- ✅ 모든 필드 업데이트 가능

### 3. 캠페인 삭제
- ✅ 본인이 생성한 캠페인만 삭제 가능
- ✅ `DRAFT` 또는 `CANCELLED` 상태만 삭제 가능
- ❌ `ACTIVE` 또는 `COMPLETED` 상태는 삭제 불가
  - 이유: 활성/완료된 캠페인은 메시지 생성 기록이 있을 수 있음

### 4. 상태 관리
- ✅ DRAFT → ACTIVE: 캠페인 시작
- ✅ ACTIVE → COMPLETED: 캠페인 종료
- ✅ DRAFT → CANCELLED: 캠페인 취소
- ❌ COMPLETED → 다른 상태: 불가 (종료된 캠페인은 변경 불가)

---

## 🧪 테스트 시나리오

### 1. 정상 플로우
```bash
# 1. 캠페인 생성 (DRAFT)
POST /admin/campaigns
→ 201 Created, campaignId: 1

# 2. 캠페인 상세 조회
GET /campaigns/1
→ 200 OK, status: "DRAFT"

# 3. 캠페인 수정 (ACTIVE로 변경)
PUT /admin/campaigns/1
→ 200 OK, status: "ACTIVE"

# 4. 캠페인 목록 조회
GET /campaigns?page=0&size=20
→ 200 OK, totalElements: 1

# 5. 캠페인 완료
PUT /admin/campaigns/1 (status: COMPLETED)
→ 200 OK
```

### 2. 예외 케이스

**잘못된 날짜 범위:**
```bash
POST /admin/campaigns
{
  "startDate": "2025-12-31",
  "endDate": "2025-11-01"  # 시작일보다 이전
}
→ 400 Bad Request: "시작일은 종료일보다 이전이어야 합니다."
```

**활성 캠페인 삭제 시도:**
```bash
DELETE /admin/campaigns/1  # status: ACTIVE
→ 400 Bad Request: "ACTIVE 또는 COMPLETED 상태의 캠페인은 삭제할 수 없습니다."
```

**권한 없는 수정 시도:**
```bash
PUT /admin/campaigns/1  # 다른 사용자가 생성한 캠페인
→ 403 Forbidden: "해당 캠페인을 수정할 권한이 없습니다."
```

---

## 🔄 향후 개선 사항

### 단기
- [ ] 캠페인 복제 기능
- [ ] 캠페인 검색 필터 강화 (타입별, 기간별)
- [ ] 캠페인 상태 자동 전환 (스케줄러)
- [ ] 캠페인 통계 조회 API

### 중기
- [ ] 캠페인 템플릿 기능
- [ ] 캠페인 승인 워크플로우
- [ ] 캠페인 성과 지표 연동
- [ ] 캠페인 히스토리 추적

### 장기
- [ ] 다중 상품 연결 (CampaignProduct)
- [ ] 다중 세그먼트 연결 (CampaignSegment)
- [ ] AI 메시지 생성 통합
- [ ] A/B 테스트 지원

---

## 📞 문의

Campaign 도메인 관련 문의:
- 담당자: Backend Team
- Email: backend@mixology.com

---

## ✅ 구현 완료 내역 (2025-11-19)

### 구현된 컴포넌트
- ✅ **Entity**: Campaign, CampaignType, CampaignStatus
- ✅ **DTO**: 6개 (CampaignDto, CreateRequest/Response, UpdateRequest/Response, DeleteResponse)
- ✅ **Repository**: CampaignRepository (커스텀 쿼리 메서드 8개 포함)
- ✅ **Service**: CampaignService (CRUD 로직 완성)
- ✅ **Controller**: CampaignController (5개 RESTful API)
- ✅ **Swagger**: OpenAPI 문서 자동 생성 (Springdoc 2.7.0)

### 기술 스택
- Spring Boot 3.5.7
- Java 21
- MySQL 9.5
- JPA/Hibernate 6.6.33
- Lombok
- Springdoc OpenAPI 2.7.0

### 실행 환경
- **접속 URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **데이터베이스**: MySQL (mixology)
- **테이블**: 캠페인 (자동 생성 완료)

### 테스트 완료
- ✅ MySQL 연결 및 테이블 생성
- ✅ Swagger UI 정상 작동
- ✅ API 엔드포인트 5개 모두 정상 작동
- ✅ JSON 직렬화/역직렬화 (한글 ENUM 처리)
- ✅ 페이징 기능
- ✅ 예외 처리 (GlobalExceptionHandler)

### 주요 특징
1. **한글 친화적 ENUM**: CampaignType을 "업셀링"과 같은 한글로 입출력 가능
2. **표준화된 응답**: 모든 API가 ApiResponse<T> 포맷 사용
3. **페이징 지원**: Spring Data의 Page를 PageResponse로 변환
4. **JPA Auditing**: created_at, updated_at 자동 관리
5. **비즈니스 로직**: Entity에 검증 및 상태 관리 로직 포함
6. **예외 처리**: BusinessException + ErrorCode로 일관된 오류 응답

---

**작성일:** 2025-11-19
**버전:** 1.0.0
**상태:** ✅ 구현 완료 및 테스트 완료
