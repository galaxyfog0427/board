# Board

Spring Boot와 MySQL을 사용해 백엔드 기본기를 학습하기 위한 게시판 프로젝트입니다.

## 현재 진행 상황
- Spring Boot 프로젝트 생성
- MySQL 연결
- 'post' 테이블 생성

## Post 테이블
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT | 게시글 식별자, PK, AUTO_INCREMENT |
| title | VARCHAR(200) | 게시글 제목 |
| content | TEXT | 게시글 내용 |
| created_at | DATETIME | 작성 시간 |
| updated_at | DATETIME | 수정 시간 |

## 현재 테이블 설계
```sql
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```
