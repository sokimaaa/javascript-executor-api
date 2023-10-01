package com.sokima.executor.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Value("${springdoc.swagger-customize.contact-name:John Doe}")
    private String contactName;

    @Value("${springdoc.swagger-customize.contact-email:myeaddress@company.com}")
    private String contactEmail;

    @Value("${springdoc.swagger-customize.title:Some API}")
    private String title;

    @Value("${springdoc.swagger-customize.version:v0.0.1}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info())
                .externalDocs(externalDocumentation());
    }

    private Info info() {
        return new Info()
                .title(title)
                .version(version)
                .contact(contact())
                .license(license());
    }

    private License license() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation()
                .description("Executor API Documentation")
                .url("https://spring.io");
    }

    private Contact contact() {
        return new Contact()
                .name(contactName)
                .email(contactEmail);
    }
}
