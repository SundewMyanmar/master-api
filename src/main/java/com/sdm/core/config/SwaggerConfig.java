package com.sdm.core.config;

import com.sdm.Constants;
import com.sdm.core.util.Globalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.RequestHandler;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Configuration
public class SwaggerConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        super.addResourceHandlers(registry);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
    }

    private SecurityContext swaggerSecurityContext() {
        return SecurityContext.builder().securityReferences(swaggerAuth())
                .forPaths(PathSelectors.any()).build();
    }

    private List<SecurityReference> swaggerAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope(
                "global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return List.of(new SecurityReference(Constants.Auth.PARAM_NAME, authorizationScopes));
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("SUNDEW MYANMAR", "http://www.sundewmyanmar.com", "info@sundewmyanmar.com");
        VendorExtension springBoot = new StringVendorExtension("Spring Boot", "https://docs.spring.io/spring-boot/docs/current/reference/html/");

        return new ApiInfo("MasterAPI Documentation",
                "This is a master-api backend system documentation page by swagger-ui.",
                "v1.5", "", contact, "", "", List.of(springBoot));
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

    private Docket buildDocket(String groupName, String basePackage) {
        Predicate<RequestHandler> findThere = null;

        if (Globalizer.isNullOrEmpty(basePackage)) {
            findThere = RequestHandlerSelectors.any();
        } else {
            findThere = RequestHandlerSelectors.basePackage(basePackage);
        }

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(groupName)
                .apiInfo(apiInfo())
                .select()
                .apis(findThere)
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Arrays.asList(apiKey()))
                .securityContexts(Arrays.asList(swaggerSecurityContext()))
                .ignoredParameterTypes(ApiIgnore.class)
                .enableUrlTemplating(true);
    }

    @Bean
    Docket coreApi() {
        return this.buildDocket("01. Core", "com.sdm.core");
    }

    @Bean
    Docket authApi() {
        return this.buildDocket("02. Auth", "com.sdm.auth");
    }

    @Bean
    Docket adminApi() {
        return this.buildDocket("03. Admin", "com.sdm.admin");
    }

    @Bean
    Docket fileApi() {
        return this.buildDocket("04. File", "com.sdm.file");
    }

    @Bean
    Docket notiApi() {
        return this.buildDocket("05. Notification", "com.sdm.notification");
    }

    @Bean
    Docket paymentApi() {
        return this.buildDocket("06. Payment", "com.sdm.payment");
    }

    @Bean
    Docket allApi() {
        return this.buildDocket("All", "com.sdm");
    }
}
