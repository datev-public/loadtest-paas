package de.datev.samples.loadtest.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    private static final Logger LOGGER = LogManager.getLogger(WebConfiguration.class);

    @Autowired
    LoadTestConfiguration loadTestConfiguration;

    @Bean
    @ConditionalOnProperty(prefix = "app-config", name = "forwarded-header-filter-activated")
    public FilterRegistrationBean<ForwardedHeaderFilter> filterFilterRegistrationBean() {

        FilterRegistrationBean<ForwardedHeaderFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        ForwardedHeaderFilter forwardedHeaderFilter = new ForwardedHeaderFilter();
        filterFilterRegistrationBean.setFilter(forwardedHeaderFilter);
        if (loadTestConfiguration.isForwardedHeaderFilterRemoveOnly()) {
            forwardedHeaderFilter.setRemoveOnly(true);
        }
        if (loadTestConfiguration.isForwardedHeaderFilterRelativeRedirect()) {
            forwardedHeaderFilter.setRelativeRedirects(true);
        }
        if (loadTestConfiguration.getForwardedHeaderFilterPatterns() != null) {
            filterFilterRegistrationBean.addUrlPatterns(loadTestConfiguration.getForwardedHeaderFilterPatterns().split(","));
        }
        filterFilterRegistrationBean.setOrder(1);
        LOGGER.info("Registering FILTER {}", forwardedHeaderFilter);
        return filterFilterRegistrationBean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/static/index.html");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addRedirectViewController("/", "index.html");
        // Does not work!
        // registry.addViewController("/").setViewName("forward:/index.html");
    }
}
