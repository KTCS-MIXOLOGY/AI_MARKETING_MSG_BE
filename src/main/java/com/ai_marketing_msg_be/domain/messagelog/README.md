# MessageLog API

AI 마케팅 메시지 생성 및 관리 시스템의 메시지 로그 도메인입니다.

## 목차
- [개요](#개요)
- [도메인 모델](#도메인-모델)
- [API 엔드포인트](#api-엔드포인트)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [사용 예제](#사용-예제)

---

## 개요

MessageLog 도메인은 AI가 생성한 마케팅 메시지의 이력을 관리하고 조회하는 기능을 제공합니다.

### 주요 기능
- ✅ 메시지 로그 목록 조회 (페이징)
- ✅ 메시지 로그 상세 조회
- ✅ 캠페인별 메시지 로그 조회
- ✅ 사용자별 메시지 로그 조회
- ✅ AI 모델 정보 및 프롬프트 추적
- ✅ 메시지 버전 관리

---

## 도메인 모델

### MessageLog Entity

```java
@Entity
@Table(name = "메시지_로그")
public class MessageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private Long campaignId;        // 캠페인 ID
    private Long segmentId;         // 세그먼트 ID
    private Long customerId;        // 개별 고객 ID (null 가능)

    private String messageContent;  // 메시지 전문
    private Integer messageVersion; // 메시지 버전 (기본값: 1)
    private MessageType messageType; // SMS, LMS, MMS, KAKAO, EMAIL
    private Integer characterCount; // 글자 수

    private String aiModelUsed;     // 사용된 AI 모델 (예: gpt-4)
    private String generationPrompt; // 생성 프롬프트
    private String tone;            // 메시지 톤 (friendly, professional 등)

    private Long createdBy;         // 생성자 User ID
    private TargetType targetType;  // SEGMENT, INDIVIDUAL
    private String segmentName;     // 타겟 세그먼트 이름
}
```

### Enums

#### MessageType
```java
public enum MessageType {
    SMS,    // 단문 메시지
    LMS,    // 장문 메시지
    MMS,    // 멀티미디어 메시지
    KAKAO,  // 카카오톡 메시지
    EMAIL   // 이메일
}
```

#### TargetType
```java
public enum TargetType {
    SEGMENT,    // 세그먼트 타겟팅
    INDIVIDUAL  // 개별 고객 타겟팅
}
```

---

## API 엔드포인트

### 1. 메시지 로그 목록 조회

**GET** `/admin/messages`

모든 메시지 로그를 페이징하여 조회합니다.

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| page | Integer | ❌ | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | ❌ | 20 | 페이지 크기 |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "messageId": 1,
        "campaignId": 10,
        "segmentId": 1,
        "customerId": null,
        "messageType": "SMS",
        "messageVersion": 1,
        "tone": "friendly",
        "characterCount": 78,
        "createdBy": 3,
        "createdAt": "2025-11-26T17:15:33.578506",
        "summary": "20대 여성 VIP 고객님을 위한 5G 전환 프로모션 안내입니다. 기존 요금제 대비 20%..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "timestamp": "2025-11-26T08:15:35.3607601Z",
  "path": "/admin/messages"
}
```

#### 특징
- `summary`: 메시지 내용의 첫 50자 + "..." (자동 생성)
- 최신 생성 순으로 정렬 (`createdAt DESC`)

---

### 2. 메시지 로그 상세 조회

**GET** `/admin/messages/{messageId}`

특정 메시지 로그의 상세 정보를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| messageId | Long | ✅ | 메시지 ID |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "messageId": 1,
    "campaignId": 10,
    "segmentId": 1,
    "customerId": null,
    "messageContent": "20대 여성 VIP 고객님을 위한 5G 전환 프로모션 안내입니다...",
    "messageVersion": 1,
    "messageType": "SMS",
    "characterCount": 78,
    "aiModelUsed": "gpt-4",
    "generationPrompt": "20대 여성 VIP 고객 대상 5G 전환 프로모션 메시지 생성",
    "tone": "friendly",
    "createdBy": 3,
    "createdAt": "2025-11-26T17:15:33.578506",
    "executor": {
      "userId": 3,
      "name": "실행자1",
      "department": "마케팅부"
    },
    "target": {
      "type": "SEGMENT",
      "segmentName": "20대 여성 VIP"
    }
  },
  "timestamp": "2025-11-26T08:15:36.9669222Z",
  "path": "/admin/messages/1"
}
```

#### 특징
- 전체 메시지 내용 포함
- 실행자(User) 정보 자동 조회
- AI 모델 및 생성 프롬프트 정보 포함

#### Error Responses

**404 Not Found** - 메시지 로그가 존재하지 않음
```json
{
  "status": 404,
  "success": false,
  "message": "Resource not found",
  "timestamp": "2025-11-26T08:15:36.9669222Z",
  "path": "/admin/messages/999"
}
```

---

### 3. 캠페인별 메시지 로그 조회

**GET** `/admin/messages/campaign/{campaignId}`

특정 캠페인에서 생성된 모든 메시지 로그를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| campaignId | Long | ✅ | 캠페인 ID |

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| page | Integer | ❌ | 0 | 페이지 번호 |
| size | Integer | ❌ | 20 | 페이지 크기 |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "messageId": 1,
        "campaignId": 10,
        "segmentId": 1,
        "messageType": "SMS",
        "summary": "20대 여성 VIP 고객님을 위한 5G 전환 프로모션..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  },
  "timestamp": "2025-11-26T08:15:45.7051792Z",
  "path": "/admin/messages/campaign/10"
}
```

#### 사용 사례
- 특정 캠페인에서 생성된 메시지 분석
- 캠페인별 메시지 생성 통계
- A/B 테스트 결과 비교

---

### 4. 사용자별 메시지 로그 조회

**GET** `/admin/messages/user/{userId}`

특정 사용자가 생성한 모든 메시지 로그를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| page | Integer | ❌ | 0 | 페이지 번호 |
| size | Integer | ❌ | 20 | 페이지 크기 |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "messageId": 1,
        "campaignId": 10,
        "createdBy": 3,
        "messageType": "SMS",
        "summary": "20대 여성 VIP 고객님을 위한 5G 전환 프로모션..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  },
  "timestamp": "2025-11-26T08:15:47.2778513Z",
  "path": "/admin/messages/user/3"
}
```

#### 사용 사례
- 실행자별 메시지 생성 이력 조회
- 실행자별 성과 분석
- 메시지 품질 모니터링

---

## 데이터베이스 스키마

### 메시지_로그 테이블

```sql
CREATE TABLE 메시지_로그 (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),

    -- 연관 정보
    campaign_id BIGINT NOT NULL,
    segment_id BIGINT,
    customer_id BIGINT,

    -- 메시지 정보
    message_content TEXT NOT NULL,
    message_version INTEGER NOT NULL DEFAULT 1,
    message_type ENUM('SMS', 'LMS', 'MMS', 'KAKAO', 'EMAIL') NOT NULL,
    character_count INTEGER NOT NULL,

    -- AI 생성 정보
    ai_model_used VARCHAR(50),
    generation_prompt TEXT,
    tone VARCHAR(50),

    -- 타겟 정보
    created_by BIGINT NOT NULL,
    target_type ENUM('SEGMENT', 'INDIVIDUAL') NOT NULL,
    segment_name VARCHAR(100),

    PRIMARY KEY (message_id),
    INDEX idx_campaign_id (campaign_id),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 인덱스 전략

| 인덱스 | 컬럼 | 목적 |
|--------|------|------|
| PRIMARY KEY | message_id | 상세 조회 성능 |
| idx_campaign_id | campaign_id | 캠페인별 조회 최적화 |
| idx_created_by | created_by | 사용자별 조회 최적화 |
| idx_created_at | created_at | 시간순 정렬 최적화 |

---

## 사용 예제

### cURL 예제

#### 1. 메시지 로그 목록 조회
```bash
curl -X GET "http://localhost:8080/admin/messages?page=0&size=20"
```

#### 2. 메시지 로그 상세 조회
```bash
curl -X GET "http://localhost:8080/admin/messages/1"
```

#### 3. 캠페인별 메시지 로그 조회
```bash
curl -X GET "http://localhost:8080/admin/messages/campaign/10?page=0&size=10"
```

#### 4. 사용자별 메시지 로그 조회
```bash
curl -X GET "http://localhost:8080/admin/messages/user/3?page=0&size=10"
```

---

## Repository 쿼리 메서드

### MessageLogRepository

```java
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    // 전체 메시지 로그 조회 (최신순)
    Page<MessageLog> findAllMessages(Pageable pageable);

    // 캠페인별 메시지 로그 조회
    Page<MessageLog> findByCampaignId(Long campaignId, Pageable pageable);

    // 메시지 타입별 조회
    Page<MessageLog> findByMessageType(MessageType messageType, Pageable pageable);

    // 생성자별 메시지 로그 조회
    Page<MessageLog> findByCreatedBy(Long createdBy, Pageable pageable);

    // 메시지 ID로 상세 조회
    Optional<MessageLog> findByMessageId(Long messageId);

    // 세그먼트별 메시지 로그 조회
    Page<MessageLog> findBySegmentId(Long segmentId, Pageable pageable);
}
```

---

## 비즈니스 로직

### MessageLogService

#### 주요 메서드

1. **getMessageLogs(Pageable)**: 전체 메시지 로그 목록 조회
   - 페이징 처리
   - DTO 변환 (summary 자동 생성)

2. **getMessageLogDetail(Long)**: 메시지 로그 상세 조회
   - User 정보 자동 조회 및 포함
   - 존재하지 않는 메시지/사용자 예외 처리

3. **getMessageLogsByCampaign(Long, Pageable)**: 캠페인별 조회
   - 특정 캠페인의 모든 메시지 이력

4. **getMessageLogsByUser(Long, Pageable)**: 사용자별 조회
   - 특정 사용자가 생성한 모든 메시지 이력

---

## 도메인 구조

```
messagelog/
├── controller/
│   ├── MessageLogController.java      # REST API 컨트롤러
│   └── MessageLogTestController.java  # 테스트 데이터 생성
├── dto/
│   ├── MessageLogListResponse.java    # 목록 조회 응답
│   ├── MessageLogDetailResponse.java  # 상세 조회 응답
│   ├── ExecutorInfo.java              # 실행자 정보
│   └── TargetInfo.java                # 타겟 정보
├── entity/
│   ├── MessageLog.java                # 메시지 로그 엔티티
│   ├── MessageType.java               # 메시지 타입 enum
│   └── TargetType.java                # 타겟 타입 enum
├── repository/
│   └── MessageLogRepository.java      # JPA Repository
├── service/
│   └── MessageLogService.java         # 비즈니스 로직
└── README.md                          # 이 문서
```

---

## 향후 확장 가능성

### 1. 메시지 효과 분석
- 메시지 발송 결과 추적 (오픈율, 클릭률)
- A/B 테스트 결과 분석
- ROI 측정

### 2. 메시지 품질 관리
- AI 모델별 성과 비교
- 프롬프트 최적화
- 메시지 버전별 성과 추적

### 3. 검색 및 필터링
- 메시지 내용 전문 검색
- 메시지 타입별 필터링
- 날짜 범위 검색
- AI 모델별 필터링

### 4. 통계 및 리포팅
- 일별/월별 메시지 생성 통계
- 캠페인별 메시지 수 집계
- 실행자별 메시지 수 집계
- 메시지 타입 분포

---

## 참고사항

### 보안
- ⚠️ **개발 환경에서만** `/admin/messages/**` 경로가 인증 없이 허용됨
- 프로덕션 환경에서는 ADMIN 권한 필요
- JWT 토큰 기반 인증 적용 예정

### 성능
- 페이징 처리로 대용량 데이터 효율적 처리
- 인덱스를 통한 쿼리 최적화
- N+1 문제 방지 (User 정보 별도 조회)

### 데이터 보관
- 메시지 로그는 영구 보관
- 삭제 API 미제공 (감사 추적 목적)
- 필요시 아카이빙 정책 수립 필요

---

## 관련 도메인

- **Campaign**: 캠페인 정보 관리
- **Product**: 상품 정보 관리
- **User**: 사용자 정보 관리

---

**Last Updated**: 2025-11-26
**Version**: 1.0.0
**Author**: AI Marketing Message System Team
