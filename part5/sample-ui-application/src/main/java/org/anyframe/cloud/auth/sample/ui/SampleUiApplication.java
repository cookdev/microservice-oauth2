package org.anyframe.cloud.auth.sample.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@SpringBootApplication
@EnableZuulProxy
@EnableOAuth2Sso //set up the application to redirect to the authorization server
public class SampleUiApplication extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(SampleUiApplication.class, args);
    }

    @RequestMapping("/token")
    @ResponseBody
    public Map<String,String> token(HttpSession session) {
        return Collections.singletonMap("token", session.getId());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/index.html", "/home.html", "/").permitAll().anyRequest().authenticated()
            .and()
                .csrf().csrfTokenRepository(csrfTokenRepository())
            .and()
                .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
    }

    private Filter csrfHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                CsrfToken csrf = (CsrfToken) request
                        .getAttribute(CsrfToken.class.getName());
                if (csrf != null) {
                    Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                    String token = csrf.getToken();
                    if (cookie == null
                            || token != null && !token.equals(cookie.getValue())) {
                        cookie = new Cookie("XSRF-TOKEN", token);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class WorkaroundRestTemplateCustomizer implements UserInfoRestTemplateCustomizer {

    @Override
    public void customize(OAuth2RestTemplate template) {
        template.setInterceptors(new ArrayList<>(template.getInterceptors()));
    }

}