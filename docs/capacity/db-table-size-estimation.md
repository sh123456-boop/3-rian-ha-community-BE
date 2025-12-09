# DB 저장 공간 계산

제 서비스의 엔티티는 아래와 같습니다.  

https://www.erdcloud.com/d/JQnRbxZw8QRe8eynC

---

## MySQL innoDB 기준

MySQL innoDB 기준  

18B의 고정 오버헤드(5B row header + 6B trx id + 7B roll ptr)와 타입별로

- DATETIME : 5B
- BIGINT : 8B
- INT : 4B
- BOOLEAN : 1B
- ENUM : 1B
- VARCHAR : 길이 + (256이면 1B, 그 이상이면 2B의 length 필드 추가)
- TEXT : 20B 포인터 (나머지는 별도 페이지에)

의 크기를 갖습니다. 

---

## 구체적인 변수에 따른 길이 가정 (단위 B)

구체적인 변수에 따른 길이는 다음과 같이 잡았습니다. (단위 B)

- email : 35  
- password hash : 60  
- nickname: 8  
- s3 key : 120  
- 게시글 제목 : 40  
- 본문 : 2K  
- 댓글 : 150  
- 리프레시 토큰 : 64  
- 만료문자열 : 32  
- OAuth provider : 6  
- 채팅방이름 : 24  
- 채팅 메시지 : 180  

---

## 엔티티 별 row의 바이트 크기

이를 기반으로 엔티티 별 row의 바이트크기를 계산하겠습니다.

- User (id, email, password, nickname, role, created_at, updated_at)
  - 유니크 인덱스 2개 추가
  - 205B
- Images(id, s3_key, user_id)
  - 148B
- posts(id, title, contents, created_at, updated_at, user_id)
  - 2.1KB
- comments(id, contents, created_at, updated_at, post_id, user_id)
  - 204B
- post_images(합성 pk , orders)
  - 38B
- counts(id, 3개의 int)
  - 38B
- user_like_posts (복합 Pk , 보조 인덱스 2개)
  - 55B
- refresh_entity(id, user_id, refresh, expiration)
  - 132B
- chat_rooms(id, name, is_group_chat, created_at, updated_at)
  - 95B
- chat_participants(id, user_id, chat_room_id, created_at, updated_at)
  - 76B
- chat_messages(id, contents, chat_room_id, user_id, created_at, updated_at)
  - 250B
- read_status(id, is_read, chat_room_id, chat_message_id, user_id, created_at, updated_at)
  - 61B

---

## 쓰기 작업이 있는 서비스별 월별 DB 저장 공간

제 서비스 중 쓰기 작업이 있는 서비스별 연관된 엔티티의 월별 db 저장 공간을 계산하겠습니다.  

해당 서비스가 실행될 때 아래의 엔티티가 모두 새롭게 작성되는 것입니다. 

- 유저 회원가입
  - user  
  - 신규가입자 10만명/월 ⇒ 100,000 * 205B = 205MB

- 게시글 작성
  - posts  
  - count  
  - 약 0.06 QPS → 150,000/월 ⇒ 150,000 * (2100B+ 38B) = 320.7MB

- 이미지 저장
  - postImage  
  - image  
  - 150,000/월 ⇒ 150,000 * (148B + 38B) = 27.9MB

- 댓글 작성
  - comments  
  - 450,000/월 ⇒ 450,000 * 204B = 91.8MB

- 좋아요
  - user_like_posts  
  - 1,500,000/월 ⇒ 1,500,000 * 55B = 82.5MB

- 채팅방 만들기
  - chat_rooms  
  - 20,000/월 ⇒ 20,000 * 95B = 1.9MB

- 채팅방 참여
  - chat_participants  
  - read_status  
  - 50,000/월 ⇒ 50,000 * (76B + 61B)  = 6.8MB

- 채팅 메시지 작성
  - chat_messages  
  - 45,000,000/월 ⇒ 45,000,000 * 250B = 11.25GB

---

## 전체 합산

전체 합산 

736.6MB(채팅 메시지 외) + 11.25GB(채팅메시지)

= 11.986GB  

= 약 12GB

---

## InnoDB 페이지 특성 고려

InnoDB는 모든 데이터를 16KB 페이지 단위로 저장하고 그 과정에서 빈공간이 생기기 때문에 20% ~ 30%의 여유를 두고 저장공간을 잡으면

- 월별 필요 저장 공간 : **15GB**

서비스를 3년간 같은 DB로 유지한다 했을 때 **540GB의 저장공간 필요**
