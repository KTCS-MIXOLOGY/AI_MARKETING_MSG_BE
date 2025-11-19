# 📨 AI Marketing Message Generator (Backend)

> KT CS 미래내일일경험 프로젝트  
> 생성형 AI 기반 개인화 마케팅 메시지 자동 생성 시스템

Spring Boot 기반으로 구현된 **AI 생성형 마케팅 메시지 자동 생성 서비스**입니다.  
고객 데이터를 기반으로 개인화된 마케팅 메시지(SMS, 알림톡 등)를 자동 생성하며,  
JWT 기반의 인증/인가, OpenAPI 명세, 환경별 설정을 지원합니다.

---

## 📋 프로젝트 개요

### 목표
- 고객 세그먼트(나이, 성별, 구매이력 등)와 캠페인 정보를 입력하면 AI가 맞춤형 마케팅 메시지를 자동 생성
- 관리자(KT CS 담당자)와 실행자(KT Plaza 담당자)를 위한 웹 기반 메시지 생성 도구 제공

### 주요 기능
- **세그먼트 기반 타겟팅**: 나이, 성별, 지역, 멤버십 등급, 최근 구매일 등으로 고객 필터링
- **1:1 개인화 메시지**: 고객 ID, 전화번호, 이름으로 개별 고객 대상 메시지 생성
- **AI 메시지 생성**: GPT 기반 자동 메시지 생성 (톤, 스타일 자동 적용)
- **메시지 히스토리 관리**: 생성된 메시지 저장 및 조회

---

## 🧰 기술 스택

| 분야 | 사용 기술 |
|------|----------|
| Backend | Spring Boot 3.5.7, Spring Web, Spring Data JPA, Spring Security |
| Database | MySQL 8.0 |
| AI 연동 | OpenAI API (GPT-4) |
| API Client | OpenFeign |
| API 문서화 | Springdoc OpenAPI (Swagger UI) |
| 인증/인가 | JWT (io.jsonwebtoken) |
| 개발환경 | Java 21, Gradle 8.x |

---

## 🏗️ 프로젝트 구조
```
src/main/java/com/kt/marketingmessage/
├── common/              # 공통 클래스 
├── config/              # 설정 클래스 (Security, OpenAPI, Feign 등)
├── domain/              # 엔티티 및 도메인 모델
    ├─── dto 
    ├─── entity 
    ├─── controller 
    ├─── service
    ├─── repository 
```

---

## 🔐 환경 변수 설정

환경 변수 기반으로 민감한 정보는 외부화되어 있습니다.  
아래 환경변수는 **개발 환경(DEV)** 기준 예시입니다.

| 환경 변수 | 설명 |
|----------|------|
| `SPRING_PROFILES_ACTIVE` | 실행할 프로필 (dev/prod) |
| `SPRING_DATASOURCE_URL_DEV` | 개발용 DB URL |
| `SPRING_DATASOURCE_USERNAME_DEV` | 개발용 DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD_DEV` | 개발용 DB 비밀번호 |
| `JWT_SECRET_DEV` | JWT 서명용 Secret Key |
| `OPENAI_API_KEY_DEV` | OpenAI API Key |


---

## 🚀 실행 방법

### 1. 사전 요구사항
- Java 21 이상
- MySQL 8.0 이상
- Gradle 8.x

### 2. 애플리케이션 실행
```bash
# 개발 환경 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 또는 IDE에서 환경변수 설정 후 실행
```

### 3. API 문서 확인
애플리케이션 실행 후 아래 주소에서 Swagger UI를 통해 API 명세를 확인할 수 있습니다.
```
http://localhost:8080/swagger-ui.html
```

---

## 📝 브랜치 전략

Git Flow 기반의 브랜치 전략을 사용합니다.
```
main (배포용)
  ↑
dev (개발 통합)
  ↑
feature/이슈번호-기능명 (기능 개발)
```

### 브랜치 네이밍 규칙
- `feature/1-user-authentication` - 새 기능 개발
- `fix/2-login-error` - 버그 수정
- `hotfix/3-critical-bug` - 긴급 수정

---

## 💬 커밋 메시지 컨벤션

AngularJS 커밋 컨벤션을 따르며, **영어로 작성**하고 body는 한글로 작성합니다. 

### 기본 형식
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 종류
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드 업무, 패키지 매니저 설정 등

### 예시
```bash
feat(auth): implement JWT-based authentication system
```
```bash
fix(message): resolve null pointer exception in message generation
```
```bash
docs(readme): update project setup instructions
```
```bash
chore: add initial project configuration
```

### 작성 규칙
- **subject**: 50자 이내, 명령문 사용, 마침표 없음, 소문자로 시작
- **body**: 72자마다 줄바꿈, 무엇을 왜 변경했는지 설명
- **footer**: 이슈 트래커 ID 참조 (Closes, Fixes, Resolves, Related to)

---

## 📅 개발 일정

| 회차 | 내용 |
|------|------|
| 1-2회차 | 마케팅 시나리오 정의, 고객 세그먼트 분류, 데이터 선정 |
| 3-5회차 | 프롬프트 엔지니어링 및 LLM 파인튜닝 |
| 6-7회차 | 생성기 프로토타입 개발 (Spring + React) |
| 8회차 | A/B 테스트 기능 구현 및 최종 시연 |

---

## 📄 License

이 프로젝트는 KT CS 미래내일일경험 프로젝트의 일환으로 개발되었습니다.
