package com.sdm.core;


import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.sdm.Constants;
import com.sdm.core.model.AuthInfo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultTest {
    public static final String SCHEMA = "http";
    public static final String HOST = "localhost";
    public static final int USER_ID = 1;
    public static final String DEVICE_ID = "mock_mvc_test_device_id";
    public static final String DEVICE_OS = "mock_mvc_test_device_os";
    public static final String USER_AGENT = "SPRING_BOOT_TEST";
    public static String CLIENT_IP = "127.0.0.1";
    protected static Faker faker;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;
    public final int PORT = 8080;

    @BeforeAll
    protected synchronized static void init() {
        faker = new Faker();
        CLIENT_IP = faker.internet().ipV4Address();
    }

    @AfterAll
    protected synchronized static void close() {
        faker = null;
        CLIENT_IP = "127.0.0.1";
    }

    private void setSecurityContext() {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setToken(UUID.randomUUID().toString());
        authInfo.setUserId(USER_ID);
        authInfo.setDeviceId(DEVICE_ID);
        authInfo.setDeviceOs(DEVICE_OS);
        authInfo.addAuthority(Constants.Auth.ROOT_ROLE);
        authInfo.addAuthority(Constants.Auth.DEFAULT_USER_ROLE);

        Authentication auth = new UsernamePasswordAuthenticationToken(authInfo, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.setSecurityContext();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .alwaysDo(JacksonResultHandlers.prepareJackson(objectMapper))
                .alwaysDo(MockMvcRestDocumentation.document("{class-name}/{method-name}",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        preprocessResponse(
                                ResponseModifyingPreprocessors.replaceBinaryContent(),
                                ResponseModifyingPreprocessors.limitJsonArrayLength(objectMapper),
                                Preprocessors.prettyPrint()
                        )))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .uris()
                        .withScheme(SCHEMA)
                        .withHost(HOST)
                        .withPort(PORT)
                        .and().snippets()
                        .withDefaults(
                                CliDocumentation.curlRequest(),
                                HttpDocumentation.httpRequest(),
                                HttpDocumentation.httpResponse(),
                                AutoDocumentation.requestFields(),
                                AutoDocumentation.responseFields(),
                                AutoDocumentation.pathParameters(),
                                AutoDocumentation.requestParameters(),
                                AutoDocumentation.description(),
                                AutoDocumentation.methodAndPath(),
                                AutoDocumentation.section()
                        )
                ).build();
    }

    protected ResultActions test(String url, HttpMethod method, Object body) throws Exception {
        MockHttpServletRequestBuilder request;
        switch (method) {
            case POST:
                request = MockMvcRequestBuilders.post(url);
                break;
            case PUT:
                request = MockMvcRequestBuilders.put(url);
                break;
            case PATCH:
                request = MockMvcRequestBuilders.patch(url);
                break;
            case DELETE:
                request = MockMvcRequestBuilders.delete(url);
                break;
            default:
                request = MockMvcRequestBuilders.get(url);
                break;
        }

        request.contentType(MediaType.APPLICATION_JSON)
                .header("user-agent", USER_AGENT)
                .header("x-forwarded-for", CLIENT_IP);

        if (!method.equals(HttpMethod.GET) && body != null) {
            String requestBody = objectMapper.writeValueAsString(body);
            request.content(objectMapper.writeValueAsString(body));
        }
        return this.mockMvc.perform(request);
    }
}
