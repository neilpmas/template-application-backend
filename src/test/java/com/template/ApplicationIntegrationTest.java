package com.template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.example.com/",
        "spring.security.oauth2.resourceserver.jwt.audiences=https://api.test.example.com",
        "DATABASE_URL=jdbc:placeholder",
        "R2DBC_URL=r2dbc:placeholder"
    }
)
@Testcontainers
class ApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private int port;

    @Test
    void healthEndpointReturnsOk() {
        WebTestClient client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();

        client.get().uri("/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("ok");
    }
}
