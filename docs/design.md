# 게시판 프로젝트 - 데이터베이스 설계 문서

> 작성 시점: 논리적 모델링 단계 (관계 설계 완료, 물리적 모델링 진행 예정)
> 참고: 실전 데이터베이스 설계 1편, 5~8장 (키, 참여도와 일대다 관계, 일대일·다대다 관계, 식별·비식별 관계)

## 1. 엔티티 개요

| 엔티티 | 설명 |
|---|---|
| Member (회원) | 게시판에 가입한 사용자 |
| Post (게시글) | 회원이 작성한 게시글 |
| Comment (댓글) | 게시글에 달린 댓글 |

## 2. 관계 설계 결정사항

### 2-1. Member ↔ Post (1:N)

FK는 '다(N)' 쪽인 `post` 테이블에 `member_id`로 둔다.

| 방향 | 질문 | 결정 | 구현 |
|---|---|---|---|
| Post → Member | 모든 게시글은 반드시 작성자가 있어야 하는가? | **필수적 참여** | `member_id` **NOT NULL** |
| Member → Post | 모든 회원은 게시글을 하나 이상 써야 하는가? | **선택적 참여** | DB 제약으로 강제 불가 (애플리케이션 책임) |

### 2-2. Member ↔ Comment (1:N)

구조는 Member-Post와 동일하다.

| 방향 | 질문 | 결정 | 구현 |
|---|---|---|---|
| Comment → Member | 모든 댓글은 반드시 작성자가 있어야 하는가? | **필수적 참여** | `member_id` **NOT NULL** |
| Member → Comment | 모든 회원은 댓글을 하나 이상 써야 하는가? | **선택적 참여** | DB 제약으로 강제 불가 |

### 2-3. Post ↔ Comment (1:N)

FK는 '다(N)' 쪽인 `comment` 테이블에 `post_id`로 둔다.

| 방향 | 질문 | 결정 | 구현 |
|---|---|---|---|
| Comment → Post | 모든 댓글은 반드시 게시글에 속해야 하는가? | **필수적 참여** | `post_id` **NOT NULL** |
| Post → Comment | 모든 게시글은 댓글이 하나 이상 있어야 하는가? | **선택적 참여** | DB 제약으로 강제 불가 |

### 2-4. 식별 관계 vs 비식별 관계

**결정: 모든 관계를 비식별 관계 + 대리 키(Surrogate Key)로 설계한다.**

- `comment`는 `comment_id`라는 자신만의 독립적인 PK를 가진다.
- `post_id`, `member_id`는 `comment`의 PK 일부가 아니라 일반 FK 컬럼이다.
- 이유: 댓글은 게시글에 종속되어 있지만, `comment_id` 하나로 충분히 독립 식별 가능하다. 식별 관계(복합키)로 설계했다면 향후 구조 변경(예: 대댓글 기능 추가) 시 PK 자체를 뜯어고쳐야 하는 큰 공사가 필요해진다. 비식별 관계는 제약조건 조정만으로 유연하게 대응할 수 있다.

### 2-5. 1:1 관계 — 현재 미적용

회원의 "자주 쓰는 정보"와 "가끔 쓰는 정보(자기소개 등)"를 분리하는 `member_detail` 같은 1:1 관계는 현재 필요 없다. 회원 프로필 기능이 커지면 그때 도입을 검토한다 (도입 시 비식별 관계 + UNIQUE 제약조건 방식을 원칙으로 한다).

### 2-6. M:N 관계 — 현재 미적용, 향후 확장 지점

현재 스키마엔 다대다 관계가 없다. 다만 향후 "게시글에 태그 여러 개 달기" 같은 기능이 추가되면 `post`-`tag`가 M:N 관계가 되며, `post_tag` 연결 테이블(비식별 관계, 대리 키)이 필요해진다.

## 3. 회원 탈퇴 정책

**결정: 소프트 삭제(논리적 삭제)**

| 후보안 | 내용 | 채택 여부                                                 |
|---|---|-----------------------------------------------------------|
| 하드 삭제 + CASCADE | 회원 삭제 시 게시글/댓글도 함께 삭제 | 다른 사용자가 단 댓글까지 함께 사라지는 문제              |
| 하드 삭제 + FK NULL 허용 | `member_id`를 NULL로 변경 | "게시글은 반드시 작성자가 있다"는 필수적 참여 원칙과 충돌 |
| **소프트 삭제** | 회원 row는 유지, `status` 컬럼만 변경 | 채택                                                      |

소프트 삭제를 채택하면:
- `post.member_id`, `comment.member_id`의 `NOT NULL` 제약을 그대로 유지할 수 있다.
- 다른 회원이 작성한 게시글/댓글의 맥락(스레드)이 깨지지 않는다.
- 탈퇴한 회원의 게시글/댓글은 애플리케이션 레벨에서 "탈퇴한 회원"으로 표시 처리한다.

## 4. 테이블 초안 (논리적 모델 — 데이터 타입/인덱스는 물리적 모델링 단계에서 확정)

```
member
├─ member_id      PK, 대리 키
├─ login_id       UQ, NOT NULL
├─ password       NOT NULL
├─ nickname       NOT NULL
├─ status         NOT NULL, DEFAULT 'ACTIVE'   -- ACTIVE / WITHDRAWN
├─ withdrawn_at   NULL                          -- 탈퇴 시점 기록
├─ created_at     NOT NULL
└─ updated_at     NOT NULL

post
├─ post_id        PK, 대리 키
├─ member_id      FK → member.member_id, NOT NULL   -- 비식별 관계, 필수적 참여
├─ title          NOT NULL
├─ content        NOT NULL
├─ created_at     NOT NULL
└─ updated_at     NOT NULL

comment
├─ comment_id     PK, 대리 키
├─ post_id        FK → post.post_id, NOT NULL        -- 비식별 관계, 필수적 참여
├─ member_id      FK → member.member_id, NOT NULL    -- 비식별 관계, 필수적 참여
├─ content        NOT NULL
├─ created_at     NOT NULL
└─ updated_at     NOT NULL
```
