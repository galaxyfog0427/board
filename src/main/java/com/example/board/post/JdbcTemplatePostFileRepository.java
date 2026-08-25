package com.example.board.post;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcTemplatePostFileRepository implements PostFileRepository {

    private final JdbcTemplate template;

    public JdbcTemplatePostFileRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Long save(PostFile postFile) {
        String sql = """
                INSERT INTO post_file(post_id, upload_file_name, store_file_name, file_size)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, postFile.getPostId());
            pstmt.setString(2, postFile.getUploadFileName());
            pstmt.setString(3, postFile.getStoreFileName());
            pstmt.setLong(4, postFile.getFileSize());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<PostFile> findByPostId(Long postId) {
        String sql = """
                SELECT file_id, post_id, upload_file_name, store_file_name, file_size, created_at
                FROM post_file
                WHERE post_id = ?
                ORDER BY file_id
                """;

        return template.query(sql, postFileRowMapper(), postId);
    }

    @Override
    public PostFile findById(Long fileId) {
        String sql = """
                SELECT file_id, post_id, upload_file_name, store_file_name, file_size, created_at
                FROM post_file
                WHERE file_id = ?
                """;

        return template.queryForObject(sql, postFileRowMapper(), fileId);
    }

    private RowMapper<PostFile> postFileRowMapper() {
        return (rs, rowNum) -> new PostFile(
                rs.getLong("file_id"),
                rs.getLong("post_id"),
                rs.getString("upload_file_name"),
                rs.getString("store_file_name"),
                rs.getLong("file_size"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
