package com.gaia3d.mago.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Slf4j
@Configuration
public class LocaleResolverConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        Locale defaultLocale = Locale.ENGLISH;
        localeResolver.setDefaultLocale(defaultLocale);
        return localeResolver;
    }
}
