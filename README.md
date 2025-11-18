# 📨 AI Marketing Message Generator (Backend)

Spring Boot 기반으로 구현된 **AI 생성형 마케팅 메시지 자동 생성 서비스**입니다.  
JWT 기반의 인증/인가, OpenAPI 명세, 환경별 설정을 지원하며 확장 가능한 구조를 목표로 설계되었습니다.

---

## 🧰 기술 스택

| 분야 | 사용 기술 |
|------|----------|
| Backend | Spring Boot 3.5.7, Spring Web, Spring Data JPA, Spring Security |
| Database | MySQL |
| API 연동 | OpenFeign |
| API 문서화 | Springdoc OpenAPI (Swagger UI) |
| 인증/인가 | JWT (io.jsonwebtoken) |
| 개발환경 | Java 21, Gradle 8.x |


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



