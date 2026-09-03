# Board

Spring Boot와 MySQL을 사용해 백엔드 기본기를 학습하기 위한 게시판 프로젝트입니다.


## Post 테이블
| 컬럼 | 타입 | 설명 |
|---|---|---|
| post_id | BIGINT | 게시글 식별자, PK, AUTO_INCREMENT |
| member_id | BIGINT | 작성자, FK (member.member_id 참조) |
| title | VARCHAR(200) | 게시글 제목 |
| content | TEXT | 게시글 내용 |
| comment_count | INT | 댓글 개수 (역정규화, 기본값 0) |
| created_at | DATETIME | 작성 시간 |
| updated_at | DATETIME | 수정 시간 |

## Member 테이블
| 컬럼 | 타입 | 설명 |
|---|---|---|
| member_id | BIGINT | 회원 식별자, PK, AUTO_INCREMENT |
| login_id | VARCHAR(50) | 로그인 ID, UNIQUE |
| password | VARCHAR(255) | 비밀번호 |
| nickname | VARCHAR(50) | 닉네임 |
| status | VARCHAR(20) | 회원 상태 (ACTIVE / WITHDRAWN), 기본값 ACTIVE |
| withdrawn_at | DATETIME | 탈퇴 시각 (NULL 허용) |
| created_at | DATETIME | 가입 시간 |
| updated_at | DATETIME | 수정 시간 |

## Comment 테이블
| 컬럼 | 타입 | 설명 |
|---|---|---|
| comment_id | BIGINT | 댓글 식별자, PK, AUTO_INCREMENT |
| post_id | BIGINT | 게시글, FK (post.post_id 참조) |
| member_id | BIGINT | 작성자, FK (member.member_id 참조) |
| content | VARCHAR(1000) | 댓글 내용 |
| created_at | DATETIME | 작성 시간 |
| updated_at | DATETIME | 수정 시간 |

## Post_file 테이블
| 컬럼 | 타입 | 설명 |
|---|---|---|
| file_id | BIGINT | 파일 식별자, PK, AUTO_INCREMENT |
| post_id | BIGINT | 게시글, FK (post.post_id 참조) |
| upload_file_name | VARCHAR(255) | 사용자가 업로드한 원본 파일명 |
| store_file_name | VARCHAR(255) | 서버 내부 저장용 파일명 (UUID, 충돌 방지) |
| file_size | BIGINT | 파일 크기 (byte) |
| created_at | DATETIME | 업로드 시간 |

## 현재 테이블 설계
```sql
CREATE TABLE member (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    withdrawn_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id),
    UNIQUE KEY uq_login_id (login_id)
);

CREATE TABLE post (
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    comment_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE comment (
    comment_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (post_id),
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE post_file (
    file_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    upload_file_name VARCHAR(255) NOT NULL,
    store_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (file_id),
    CONSTRAINT fk_post_file_post FOREIGN KEY (post_id) REFERENCES post (post_id)
);
```

## 학습 진행 상황

### 구현

#### JDBC 기초
- Spring Boot 프로젝트 초기 설정
- MySQL 연결 및 'post' 테이블 생성
- SQL CRUD 직접 실행
- JDBC 기본 동작 원리 학습
- `Connection`, `PreparedStatement`, `ResultSet` 사용
- 순수 JDBC로 게시글 CRUD 구현
- `AUTO_INCREMENT`로 생성된 게시글 ID 처리
- CRUD 반복 테스트 완료
- DataSource를 이용한 커넥션 획득 방식 추상화
- DriverManagerDataSource 적용
- HikariCP 커넥션 풀 적용

#### 트랜잭션
- post 테이블에 member_id, comment_count 컬럼 추가 (역정규화)
- member, comment 테이블 생성
- Member, Comment 도메인 및 Repository 구현 (순수 JDBC)
- Repository를 스프링 빈으로 전환, application.properties 기반 DataSource/TransactionManager 자동 등록 적용
- 트랜잭션 개념 학습 (원자성, 커밋/롤백, 자동/수동 커밋)
- 댓글 작성 + comment_count 증가 로직에 @Transactional 적용
- 트랜잭션 롤백 테스트 작성 (의도적 예외 발생 . 전체 롤백되는 것을 검증)

#### 예외 처리
- 체크 예외 vs 언체크 예외 차이 학습
- Post/Member/Comment Repository를 JdbcTemplate으로 전환
- SQLException 누수 문제 해결 (JdbcTemplate이 DataAccessException으로 자동 변환)
- 조회 결과 없음 처리를 EmptyResultDataAccessException으로 통일 (직접 예외 클래스 제거)
- 테스트를 @Transactional 기반 자동 롤백 방식으로 전환

#### Spring MVC
- 서블릿 -> MVC 패턴 -> 프론트 컨트롤러 -> DispatcherServlet 구조 학습
- 핸들러 매핑 / 핸들러 어댑터 / 뷰 리졸버로 이어지는 요청 처리 흐름 이해
- `@RequestMapping`. `@RequestParam`, `@ModelAttribute` 등 요청 매핑 / 파라미터 바인딩 학습
- `PostRepository.findAll()`, `CommentRepository.findByPostId()` 추가 (게시글 목록 / 댓글 목록 조회 준비)
- 동시간대 생성 데이터의 정렬 안정성을 위한 타이브레이커(`post_id`, `comment_id`) 적용 및 검증 테스트 작성
- Thymeleaf 기반 게시글 목록/상세/등록/수정(CRUD) 페이지 - 구현
- 폼 전송 객체(`PostForm`)를 도메인 객체(`Post`)와 분리 - Mass Assignment 방지 및 도메인 불변성 유지
- PRG 패턴 적용 및 `RedirectAttributes`로 새로고침 중복 등록 문제 해결
- 타임리프 유틸리티 객체(`#temporals`)로 날짜 포맷팅, `th:if`/`th:unless`로 빈 목록 처리
- 등록/수정 폼을 `th:object`, `th:field` 기반으로 개선 (id/name/value 자동 처리, 수정 폼에서 작성자 필드 제거)
- 화면 문구를 `messages.properties`로 외부화 (다국어는 실제 요구사항이 아니라 스킵, 메시지 외부화만 적용)
- Bean Validation 적용, 폼 객체를 등록용(`PostSaveForm`)/수정용(`PostEditForm`)으로 분리하여 검증 중복 방지
- 회원가입(`MemberController`, 로그인 ID 중복 확인용 `Validator` + `@InitBinder`) 및 로그인/로그아웃(`HttpSession` 기반) 구현
- 게시글 작성 시 작성자를 폼 직접 입력 대신 세션의 로그인 회원 정보로 자동 처리
- 스프링 인터셉터(`HandlerInterceptor`)로 로그인 인증을 공통 처리, `WebMvcConfigurer`로 등록
- 로그인 성공 후 원래 요청 경로로 복귀하는 `redirectURL` 처리
- `@SessionAttribute`로 세션 조회 코드 최소화
- 게시글 수정 시 작성자 본인 확인 추가
- 스프링 부트 기본 오류 처리(`BasicErrorController`) 활용, `templates/error/4xx.html, 5xx.html` 등록으로 기본 스프링 에러 페이지 대체
- `@ControllerAdvice`, `@ExceptionHandler`로 예외 처리 로직 일원화
- 영속성 계층 예외(`EmptyResultDataAccessException`)를 도메인 예외(`PostNotFoundException`) 변환 책임을 Repository로 이동
- Repository를 인터페이스/구현체로 분리 (`PostRepository` -> `JdbcTemplatePostRepository` 등) 추후 JPA 전환 시 Controller/Service 코드 변경 없이 구현체만 교체 가능하도록 설계
- 게시글 파일 첨부 기능 구현 - 종류/개수 제한 없이 업로드로 용량만 제한
- `FileStore` 인터페이스로 저장 방식을 추상화 (`LocalFileStore` 구현, 추후 S3 등으로 교체 가능하도록 설계)
- MockMvc 기반 Controller 계층 테스트 추가 (`PostControllerTest`, `MemberControllerTest`, `LoginControllerTest`)

#### JPA
- Member/Post/Comment를 JPA 엔티티로 매핑, `protected` 기본 생성자 추가
- Member.status를 String에서 MemberStatus enum으로 전환
- DB DEFAULT 컬럼(status, created_at, updated_at, comment_count)에 의존하던 로직이 JPA에서는 깨지는 것을 확인, `@PrePersist`/`@PreUpdate`로 엔티티가 null로 넘어가지 않고 기본값을 직접 책임지도록 수정
- MemberRepository, CommentRepository, PostRepository를 `JpaRepository` 상속으로 전환, 기존 JdbcTemplate 구현체는 빈 등록만 해제하고 참고용으로 보존
- `CommentRepository.findByPostId`는 원본 정렬 순서 유지를 위해 `@Quary`로 직접 작성
- `PostRepository.incrementCommentCount()`는 `@Modifying` 벌크 쿼리로 전환
- `PostRepository.findAll()`을 인터페이스 내 `default` 메서드로 재정의하여 기존 정렬 기준(created_at DESC, post_id DESC) 유지
- 게시글 수정 로직을 Repository의 명시적 update() 대신 변경 감지(더티 체킹) 기반으로 전환, 이를 위해 `PostService` 신설 (트랜잭션 경계와 `PostNotFoundException` 변환 책임을 기존 Repository에서 Service로 이동)
- `ddl-auto=validate`로 JPA 엔티티 매핑과 기존 DDL 스키마의 정합성 검증

#### 페이징 처리
- 게시글 목록에 `Page`/`Pageable` 적용, 기본 10건씩 최신순(`createdAt`, `id` DESC)으로 조회
- 페이지 번호는 내부적으로 0부터 시작하지만, 화면에는 1부터 시작하는 번호로 변환해서 표시
- Post와 Member를 연관관계 매핑 없이 ON 조건으로 JOIN, JPQL 생성자 표현식으로 목록 전용 DTO(`PostListItem`)에 바로 매핑해 작성자 닉네임 함께 조회
- count 쿼리는 목록 조회 쿼리와 분리(`@Query`의 `countQuery`)해 불필요한 JOIN 제거
- 테스트 작성 중 테스트가 기존 DB 데이터와 공유되어 결과가 흔들리는 문제, 정렬 없는 페이징은 순서가 보장되지 않는다는 점을 직접 겪고 수정

#### JPA Auditing
- 손으로 관리하던 @PrePersist/@PreUpdate 기반 시간 처리를 Spring Data JPA Auditing으로 전환
- BaseTimeEntity(@MappedSuperclass)에 @CreatedDate/@LastModifiedDate 공통화, @EnableJpaAuditing 활성화
- 값이 항상 자동으로 채워지는 필드는 엔티티 생성자에서 아예 제거해 "받지만 안 쓰는" 파라미터를 없앰

#### 연관관계 매핑 (Post/Comment → Member/Post)
- Post.memberId, Comment.postId/memberId(Long)를 실제 객체 참조(@ManyToOne)로 전환
- 외래 키를 가진 쪽을 연관관계의 주인으로 설정, 양방향 대신 단방향으로만 매핑(불필요한 컬렉션/toString 순환 참조 방지)
- 모든 @ManyToOne에 fetch = LAZY 명시적으로 설정 (기본값 EAGER 회피)
- 순수 JdbcTemplate 참고용 구현체(Post/Comment)는 생성자 시그니처 변경으로 삭제, Git 히스토리로 이력 보존

### 데이터베이스 설계
- 개념적/논리적 모델링 설계 완료 (Member/Post/Comment 엔티티, 관계, 참여도, 식별 여부 확정)
- 물리적 모델링 완료 (데이터 타입, 제약조건, 역정규화 확정)

## 설계 문서
- 데이터베이스 논리적 모델링 설계 결정사항 [docs/design.md](./docs/design.md) 참고