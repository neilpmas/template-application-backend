package com.template.connect;

import com.template.grpc.v1.GetServerInfoRequest;
import com.template.grpc.v1.GetServerInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "spring.flyway.enabled=false"
)
@EnableAutoConfiguration(exclude = {
    R2dbcAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
class ConnectEndpointTest {

    private static final MediaType APPLICATION_PROTO = MediaType.parseMediaType("application/proto");

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .apply(SecurityMockServerConfigurers.springSecurity())
            .build();
    }

    @Test
    void getServerInfo_shouldReturnProtobufResponse() throws Exception {
        byte[] responseBytes = webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("test-user")))
            .post()
            .uri("/connect/template.v1.TemplateService/GetServerInfo")
            .contentType(APPLICATION_PROTO)
            .bodyValue(GetServerInfoRequest.getDefaultInstance().toByteArray())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_PROTO)
            .expectBody(byte[].class)
            .returnResult()
            .getResponseBody();

        assertThat(responseBytes).isNotNull();
        GetServerInfoResponse response = GetServerInfoResponse.parseFrom(responseBytes);
        assertThat(response.getVersion()).isNotBlank();
        assertThat(response.getEnvironment()).isNotBlank();
    }

    @Test
    void unknownMethod_shouldReturn404() {
        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("test-user")))
            .post()
            .uri("/connect/template.v1.TemplateService/NonExistent")
            .contentType(APPLICATION_PROTO)
            .bodyValue(new byte[0])
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.code").isEqualTo("unimplemented");
    }

    @Test
    void unauthenticated_shouldReturn401() {
        webTestClient
            .post()
            .uri("/connect/template.v1.TemplateService/GetServerInfo")
            .contentType(APPLICATION_PROTO)
            .bodyValue(new byte[0])
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
