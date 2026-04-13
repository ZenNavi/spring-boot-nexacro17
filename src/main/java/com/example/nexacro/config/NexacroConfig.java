package com.example.nexacro.config;

import org.springframework.context.annotation.Configuration;

/**
 * Nexacro configuration.
 * When nexacro-xapi17.jar is added to libs/, register NexacroServiceFilter here:
 *
 * @Bean
 * public FilterRegistrationBean<Filter> nexacroServiceFilter() {
 *     FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
 *     reg.setFilter(new com.nexacro.xapi.spring.NexacroServiceFilter());
 *     reg.addUrlPatterns("/nexacro/*");
 *     reg.setName("nexacroServiceFilter");
 *     return reg;
 * }
 */
@Configuration
public class NexacroConfig {
    // NexacroServiceFilter registration goes here when jar is available
}
