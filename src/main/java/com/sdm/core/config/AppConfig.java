package com.sdm.core.config;

import com.sdm.core.util.Globalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(Globalizer.jsonMapper()));
    }

    @Value("${com.sdm.cors.allow-origins}")
    private String allowedOrigins = "*";

    @Value("${com.sdm.cors.allow-methods}")
    private String allowedMethods = "*";

    @Value("${com.sdm.cors.allow-headers}")
    private String allowedHeaders = "*";

    @Value("${com.sdm.cors.exposed-headers}")
    private String exposedHeaders = "";

    @Value("${com.sdm.cors.allow-credentials}")
    private boolean allowCredentials = true;

    @Value("${com.sdm.cors.max-age}")
    private long maxAge = 36000;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(this.allowedOrigins)
            .allowedHeaders(this.allowedHeaders)
            .allowedMethods(this.allowedMethods)
            .allowCredentials(this.allowCredentials)
            .maxAge(this.maxAge)
            .exposedHeaders(this.exposedHeaders);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false)
            .favorParameter(true)
            .parameterName("mediaType")
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON_UTF8)
            .mediaType("html", MediaType.TEXT_HTML)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON_UTF8);
    }


}
