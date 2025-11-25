package org.loamok.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "org.loamok.jobs",
    "org.loamok.libs.o2springsecurity" // Scan des @Service, @Component, @Controller
})
// 1. Force la détection des Repositories (Interfaces + Impl)
@EnableJpaRepositories(basePackages = {
    "org.loamok.jobs.repository"
})
// 2. Force la détection des Entités JPA (@Entity)
@EntityScan(basePackages = {
    "org.loamok.jobs.entity"
})
public class JobsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(JobsApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(JobsApplication.class, args);
    }

}
