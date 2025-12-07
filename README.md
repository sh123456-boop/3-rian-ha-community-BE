# 🎙️ 프로젝트 소개 (Project Overview)
개인적인 고민과 개발을 주제로 서로 소통하는 커뮤니티 서비스의 **Back-end 서버**입니다.  
게시글·댓글·좋아요와 같은 커뮤니티 기능과, STOMP 기반 실시간 채팅 기능을 제공하는 REST API 서버입니다.

---

## ⚙️ Back-end 소개

- **아키텍처**
  - Spring Boot 기반 REST API 서버
  - 도메인 중심의 계층형 구조 (Controller – Service – Repository)
  - WebSocket + STOMP 기반 실시간 채팅 구현

- **주요 도메인 및 기능**
  - **회원**
    - 회원가입, 로그인, 로그아웃
    - 마이페이지/프로필 조회
  - **인증/인가**
    - JWT 기반 인증 (Access / Refresh 토큰 구조)
    - 인증 필터를 통한 요청 검증 및 예외 처리
  - **게시판**
    - 게시글 CRUD (작성 / 조회 / 수정 / 삭제)
    - 페이징, 정렬
  - **댓글**
    - 게시글별 댓글 CRUD
  - **좋아요**
    - 게시글/댓글 좋아요 처리
  - **실시간 채팅 (STOMP)**
    - WebSocket + STOMP 기반 채팅방 구현
    - 채팅방 입장/퇴장, 메시지 송수신
    - 특정 채팅방(topic)으로 메시지 브로드캐스팅

- **기술적 특징**
  - Entity / DTO 분리 및 Bean Validation을 통한 요청 데이터 검증
  - Global Exception Handler로 일관된 에러 응답 형식 제공
  - 서비스/도메인 단위로 비즈니스 로직 분리
  - Redis 등을 활용한 세션/채팅 메시지 관리

---

## 👨‍👩‍👧‍👦 개발 인원 및 기간
- 개발 기간: 2025.09 ~ 2025.12 
- 개발 인원: Back-end 1인 개발 (본인)
- 역할:
  - 요구사항 분석 및 도메인 설계
  - REST API 및 실시간 채팅 서버 구현
  - 인증/인가, 예외 처리, 응답 DTO 등 공통 인프라 구현
  - 배포 및 운영 환경 구성

---

## 🛠 사용 기술 및 Tools

### Back-end
- Java
- Spring Boot (Spring Web, Spring WebSocket, Spring Messaging(STOMP))
- Spring Security, JWT
- Spring Data JPA, Hibernate
- MySQL
- Redis
- Gradle

### Infra & 기타
- AWS EC2 / RDS / S3 / CloudFront / Route 53
- Docker / Docker Compose
- Git, GitHub
- GitHub Actions(CI/CD)

---

## 📂 GitHub Repository
- Front-end GitHub: https://github.com/sh123456-boop/3-rian-ha-community-FE
- Back-end GitHub (Spring Webflux 서버 - chat 구현): https://github.com/sh123456-boop/chat_flux

---

## 🎥 시연 영상
- YouTube: https://youtu.be/3V6KJWw1kFE
