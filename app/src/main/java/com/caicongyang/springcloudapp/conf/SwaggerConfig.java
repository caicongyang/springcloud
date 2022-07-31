package com.caicongyang.springcloudapp.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


/**
 *
 * @author caicongyang1
 * @version id: SwaggerConfig, v 0.1 16/4/22 下午4:12 caicongyang1 Exp $$
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Value("${swagger.ui.enable}") //该配置项在配置中心管理
    private boolean environmentSpecificBooleanFlag;




    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.caicongyang"))
                .paths(PathSelectors.any())
                .build().enable(Boolean.valueOf(environmentSpecificBooleanFlag));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("RESTful APIs")
                .description("RESTful APIs")
                .termsOfServiceUrl("http://localhost:8080/")
                .version("1.0")
                .build();
    }
}
