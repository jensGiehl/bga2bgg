package de.agiehl.boardgame.b2btest.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Enables switching the UI language via a {@code ?lang=} request parameter. On the first visit the
 * language is derived from the browser's {@code Accept-Language} header (German if preferred,
 * otherwise English). English is the fallback for any unsupported language.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocaleFunction(WebConfig::resolveBrowserLocale);
        return resolver;
    }

    /**
     * Picks the UI language for the first visit from the browser's {@code Accept-Language} header:
     * German if the browser prefers it, otherwise English (also the fallback for any other
     * language).
     */
    private static Locale resolveBrowserLocale(HttpServletRequest request) {
        for (Locale requested : Collections.list(request.getLocales())) {
            if ("de".equals(requested.getLanguage())) {
                return Locale.GERMAN;
            }
            if ("en".equals(requested.getLanguage())) {
                return Locale.ENGLISH;
            }
        }
        return Locale.ENGLISH;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
