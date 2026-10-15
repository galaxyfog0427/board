CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `login_id` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `withdrawn_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uq_login_id` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `comment_count` int NOT NULL DEFAULT '0',
  `view_count` bigint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`post_id`),
  KEY `fk_post_member` (`member_id`),
  CONSTRAINT `fk_post_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `fk_comment_post` (`post_id`),
  KEY `fk_comment_member` (`member_id`),
  CONSTRAINT `fk_comment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post_file` (
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `upload_file_name` varchar(255) NOT NULL,
  `store_file_name` varchar(255) NOT NULL,
  `file_size` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`),
  KEY `fk_post_file_post` (`post_id`),
  CONSTRAINT `fk_post_file_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post_like` (
  `post_like_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_like_id`),
  UNIQUE KEY `uk_post_like` (`post_id`,`member_id`),
  KEY `fk_post_like_member` (`member_id`),
  CONSTRAINT `fk_post_like_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
