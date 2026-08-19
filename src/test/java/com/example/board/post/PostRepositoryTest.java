package com.example.board.post;

import com.example.board.connection.ConnectionConst;
import com.zaxxer.hikari.HikariDataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PostRepositoryTest {

    PostRepository repository;

    @BeforeEach
    void beforeEach() throws Exception {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(ConnectionConst.URL);
        dataSource.setUsername(ConnectionConst.USERNAME);
        dataSource.setPassword(ConnectionConst.PASSWORD);

        repository = new PostRepository(dataSource);
    }

    @Test
    void crud() throws SQLException {
        //create
        Post post = new Post(
                null,
                "JDBC 게시글",
                "순수 JDBC로 저장",
                null,
                null
        );
        Long savedPostId = repository.save(post);

        //read
        Post foundPost = repository.findById(savedPostId);
        assertThat(foundPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(foundPost.getContent()).isEqualTo(post.getContent());

        //update
        repository.update(savedPostId, "수정된 제목", "수정된 내용");
        Post updatedPost = repository.findById(savedPostId);
        System.out.println(updatedPost);

        //delete
        repository.delete(savedPostId);
        assertThatThrownBy(() -> repository.findById(savedPostId))
                .isInstanceOf(NoSuchElementException.class);
    }

}