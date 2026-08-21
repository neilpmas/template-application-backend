package com.template.connect;

import com.template.grpc.v1.GetServerInfoRequest;
import com.template.grpc.v1.GetServerInfoResponse;
import com.template.grpc.v1.TemplateServiceGrpc;
import dev.neilmason.boot.connect.test.AutoConfigureConnectTestClient;
import dev.neilmason.boot.connect.test.ConnectError;
import dev.neilmason.boot.connect.test.ConnectTestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.MockServerConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "spring.flyway.enabled=false"
)
@EnableAutoConfiguration(exclude = {
    R2dbcAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
@AutoConfigureConnectTestClient
class ConnectEndpointTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityTestConfig {
        @Bean
        MockServerConfigurer springSecurityConfigurer() {
            return SecurityMockServerConfigurers.springSecurity();
        }
    }

    @Autowired
    private ConnectTestClient connectTestClient;

    private ConnectTestClient authenticated() {
        return connectTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("test-user")));
    }

    @Test
    void getServerInfo_shouldReturnProtobufResponse() {
        GetServerInfoResponse response = authenticated().call(
            TemplateServiceGrpc.getGetServerInfoMethod(),
            GetServerInfoRequest.getDefaultInstance());

        assertThat(response.getVersion()).isNotBlank();
        assertThat(response.getEnvironment()).isNotBlank();
    }

    @Test
    void unknownMethod_shouldReturn404() {
        ConnectError error = authenticated().callExpectingError(
            TemplateServiceGrpc.getGetServerInfoMethod().toBuilder()
                .setFullMethodName("template.v1.TemplateService/NonExistent")
                .build(),
            GetServerInfoRequest.getDefaultInstance());

        assertThat(error.code()).isEqualTo("unimplemented");
    }

    @Test
    void unauthenticated_shouldReturn401() {
        ConnectError error = connectTestClient.callExpectingError(
            TemplateServiceGrpc.getGetServerInfoMethod(),
            GetServerInfoRequest.getDefaultInstance());

        assertThat(error.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(error.code()).isNull();
    }
}
