package com.sdm;

import com.sdm.core.db.DefaultRepositoryImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@EnableJpaRepositories(repositoryBaseClass = DefaultRepositoryImpl.class)
@Log4j2
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        application.bannerMode(Banner.Mode.OFF);
        return application.sources(Application.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void systemStartup() {
        log.info("System is running...");
    }
}
