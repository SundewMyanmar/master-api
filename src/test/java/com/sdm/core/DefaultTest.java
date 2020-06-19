package com.sdm.core;


import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.misc.AuthorizationSnippet;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.javafaker.Faker;
import com.sdm.Constants;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.core.model.AuthInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class DefaultTest {
    public final String scheme="http";
    public final String host="localhost";
    public final int port=8080;
    public final String token="vS04RUDUkfvDV1Lsuz";
    public final int userId=1;
    public final String user="sample@gmail.com";
    public final String password="password";
    public final String deviceId="TEST_BROWSER";
    public final String deviceOs="TEST_DEVICE";
    public final String userAgent="SPRING_BOOT_TEST";
    public final String uploadFile="var/www/sample.jpg";
    public final String uploadFileName="sample.jpg";

    public Faker FAKER(){
        return new Faker();
    }

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    protected String tokenString;

    protected ResultActions authenticate() throws Exception{
        AuthRequest request = new AuthRequest();
        request.setUser(this.user);
        request.setPassword(this.password);
        request.setDeviceId(this.deviceId);
        request.setDeviceOS(this.deviceOs);

        ResultActions result=this.mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", this.userAgent)
                .content(objectMapper.writeValueAsString(request))
        );

        return result;
    }

    protected Principal getPrincipal()throws Exception{
        ResultActions result=this.authenticate();
        return result.andReturn().getRequest().getUserPrincipal();
    }

    protected String getAccessTokenString()throws Exception{
        if(tokenString==null || tokenString.isBlank() || tokenString.isEmpty()){
            ResultActions result=this.authenticate();
            String json = result.andReturn().getResponse().getContentAsString();
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Map<String, String> contentMap=(HashMap<String,String>)map.get("content");
            this.tokenString=contentMap.get("current_token");
        }

        return "Bearer "+tokenString;
    }

    protected RequestPostProcessor accessToken() {
        return new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.addHeader("Authorization", "Bearer AccessTokenString");
                return AuthorizationSnippet.documentAuthorization(mockHttpServletRequest, "User Access Token is required.");
            }
        };
    }

    private void setSecurityContext(){
        AuthInfo authInfo=new AuthInfo();
        authInfo.setToken(this.token);
        authInfo.setUserId(this.userId);
        authInfo.setDeviceId(this.deviceId);
        authInfo.setDeviceOs(this.deviceOs);

        Authentication auth = new UsernamePasswordAuthenticationToken(authInfo,null);
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
                                //Preprocessors.replacePattern(Pattern.compile(Constants.Pattern.CURRENT_TOKEN_JSON),"\"current_token\":\"AccessTokenString\""),
                                Preprocessors.prettyPrint()
                        )))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .uris()
                        .withScheme(this.scheme)
                        .withHost(this.host)
                        .withPort(this.port)
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
}
