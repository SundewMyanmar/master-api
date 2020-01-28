package com.sdm.core.config;

import com.sdm.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@EnableSwagger2
@Configuration
public class SwaggerConfig extends WebMvcConfigurationSupport {
    private SecurityContext swaggerSecurityContext() {
        return SecurityContext.builder().securityReferences(swaggerAuth())
                .forPaths(PathSelectors.any()).build();
    }

    private List<SecurityReference> swaggerAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope(
                "global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference(Constants.Auth.PARAM_NAME,
                authorizationScopes));
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("SUNDEW MYANMAR", "http://www.sundewmyanmar.com", "info@sundewmyanmar.com");
        VendorExtension springBoot = new StringVendorExtension("Spring Boot", "https://docs.spring.io/spring-boot/docs/current/reference/html/");

        ApiInfo info = new ApiInfo("MasterAPI Documentation",
                "This is a master-api backend system documentation page by swagger-ui.",
                "v1.5", "", contact, "", "", Arrays.asList(springBoot));
        return info;
    }

    private ApiKey apiKey() {
        return new ApiKey(Constants.Auth.PARAM_NAME, HttpHeaders.AUTHORIZATION.toString(), "header");
    }

    @Bean
    SecurityConfiguration swaggerSecurity() {
        return SecurityConfigurationBuilder.builder()
                .scopeSeparator(",")
                .additionalQueryStringParams(null)
                .useBasicAuthenticationWithAccessCodeGrant(false)
                .build();
    }

    @Bean
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Arrays.asList(apiKey()))
                .securityContexts(Arrays.asList(swaggerSecurityContext()))
                .ignoredParameterTypes(ApiIgnore.class)
                .enableUrlTemplating(true);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        super.addResourceHandlers(registry);
    }
}
