package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@SpringBootTest
public class BulkInsertUsers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void bulkInsertUsersTest() {
        String sql = "INSERT INTO users (email, password, user_role, nickname, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW())";
        String encodedPassword = passwordEncoder.encode("password123");

        int totalCount = 5000000; // 500만 건
        int batchSize = 10000;    // 한 번에 1만 건씩 묶어서 쏘기! (DB 메모리 보호)

        for (int j = 0; j < totalCount / batchSize; j++) {
            final int batchIndex = j;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int currentId = (batchIndex * batchSize) + i;
                    String nickname = "USER_" + UUID.randomUUID().toString().substring(0, 8) + "_" + (i * batchSize);

                    ps.setString(1, nickname + "@example.com");
                    ps.setString(2, encodedPassword);
                    ps.setString(3, "USER");
                    ps.setString(4, nickname);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            System.out.println((j + 1) * batchSize + "건 완료...");
        }
    }

    /*@Test
    void bulkInsertUsers() {
        int totalCount = 5_000_000;
        int batchSize = 10_000; // 1만 건씩 끊어서 처리

        String sql = "INSERT INTO users (email, password, user_role, nickname, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW())";

        String encodedPassword = passwordEncoder.encode("password123");

        for (int i = 0; i < totalCount / batchSize; i++) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                String nickname = "USER_" + UUID.randomUUID().toString().substring(0, 8) + "_" + (i * batchSize + j);

                batchArgs.add(new Object[]{
                        nickname + "@example.com",
                        encodedPassword,
                        "USER",
                        nickname
                });
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
            System.out.println((i + 1) * batchSize + "건 완료...");
        }
    }*/
}
