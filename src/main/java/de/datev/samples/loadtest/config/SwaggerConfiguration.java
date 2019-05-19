package de.datev.samples.loadtest.config;

import com.fasterxml.classmate.TypeResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.Optional;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(SwaggerConfiguration.class);

    @Value("${swagger.enable}")
    private boolean isEnable;

    @Value("${spring.swagger.host:#{null}}")
    private Optional<String> swaggerHost;

    private final TypeResolver typeResolver;

    private final Environment env;

    @Autowired
    SwaggerConfiguration(TypeResolver typeResolver, Environment env){
        this.typeResolver = typeResolver;
        this.env = env;
    }


    @Bean
    public Docket v1Api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        // due X-Forwarded-For (XFF) issues we have to change the host for swagger calls
        if (env.acceptsProfiles("cloud")) {
            LOGGER.info("1 - Changing swagger-host for cloud access to: "+swaggerHost);
            if (swaggerHost.isPresent()) {
                docket.host(swaggerHost.get());
                LOGGER.info("Changing swagger-host for cloud access to: "+swaggerHost);
            }

        }
        docket.select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex("/api/.*"))
                .build()
                .tags(new Tag("Search", ""))
                .apiInfo(metaData())
                .enable(isEnable);

        return docket;
    }

    private ApiInfo metaData() {
        return new ApiInfo(
                "LOAD TEST REST API",
                "This is the LOAD TEST REST API based on Spring Boot.",
                "1.0",
                "Terms of service",
                new Contact("DATEV eG", "https://www.datev.de", "info@datev.de"),
                "License of API", "license.txt", Collections.emptyList());
    }

    // Workaround: change base url for versioning (https://github.com/springfox/springfox/issues/963#issuecomment-198551416)
    static class BasePathAwareRelativePathProvider extends AbstractPathProvider {
        private String basePath;

        public BasePathAwareRelativePathProvider(String basePath) {
            this.basePath = basePath;
        }

        @Override
        protected String applicationPath() {
            return basePath;
        }

        @Override
        protected String getDocumentationPath() {
            return "/";
        }

        @Override
        public String getOperationPath(String operationPath) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
            return Paths.removeAdjacentForwardSlashes(
                    uriComponentsBuilder.path(operationPath.replaceFirst(basePath, "")).build().toString());
        }
    }
}
