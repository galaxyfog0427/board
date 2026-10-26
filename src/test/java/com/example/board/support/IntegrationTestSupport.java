package com.example.board.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;

public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4.13");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8.10.2")
            .withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }
}
