package com.template.connect;

import com.template.AbstractIntegrationTest;
import com.template.grpc.v1.GetServerInfoRequest;
import com.template.grpc.v1.GetServerInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ConnectEndpointIT extends AbstractIntegrationTest {

    private static final MediaType APPLICATION_PROTO = MediaType.parseMediaType("application/proto");

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .apply(SecurityMockServerConfigurers.springSecurity())
            .build();
    }

    @Test
    void getServerInfo_shouldReturnProtobufResponse() throws Exception {
        byte[] responseBytes = webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("integration-test-user")))
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
}
