package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class BulkInsertUsers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bulkInsertUsers() {
        int totalCount = 5_000_000;
        int batchSize = 10_000; // 1만 건씩 끊어서 처리

        String sql = "INSERT INTO users (email, password, user_role, nickname, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW())";

        for (int i = 0; i < totalCount / batchSize; i++) {
            List<Object[]> batchArgs = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                String nickname = "USER_" + UUID.randomUUID().toString().substring(0, 8) + "_" + (i * batchSize + j);

                batchArgs.add(new Object[]{
                        nickname + "@example.com",
                        "1234",
                        "USER",
                        nickname
                });
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
            System.out.println((i + 1) * batchSize + "건 완료...");
        }
    }
}
