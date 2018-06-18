package org.aksw.defacto.restful;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@EnableOAuth2Client
@EnableAuthorizationServer
@Order(6)
public abstract class WebSecurityAbstract extends WebSecurityConfigurerAdapter {

  public static Logger LOG = LogManager.getLogger(WebSecurityAbstract.class);

  @Autowired
  OAuth2ClientContext oauth2ClientContext;

  @Configuration
  @EnableResourceServer
  protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(final HttpSecurity http) throws Exception {
      http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
    }
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.antMatcher("/**").authorizeRequests()
        .antMatchers("/", "/login**", "/lib/**", "/js/**", "/img/**", "/css/**", "/fonts/**",
            "/templates/**")
        .permitAll().anyRequest().authenticated().and().exceptionHandling()
        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
        .logoutSuccessUrl("/").permitAll().and().csrf().csrfTokenRepository(csrfTokenRepository())
        .and().addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
        .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
  }

  private CsrfTokenRepository csrfTokenRepository() {
    final HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    repository.setHeaderName("X-XSRF-TOKEN");
    return repository;
  }

  private Filter csrfHeaderFilter() {
    return new OncePerRequestFilter() {

      @Override
      protected void doFilterInternal(final HttpServletRequest request,
          final HttpServletResponse response, final FilterChain filterChain)
              throws ServletException, IOException {

        final CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
          Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
          final String token = csrf.getToken();
          if ((cookie == null) || ((token != null) && !token.equals(cookie.getValue()))) {
            cookie = new Cookie("XSRF-TOKEN", token);
            cookie.setPath("/");
            response.addCookie(cookie);
          }
        }
        filterChain.doFilter(request, response);
      }
    };
  }

  private Filter ssoFilter() {
    final CompositeFilter filter = new CompositeFilter();
    filter.setFilters(Arrays.asList(//
        ssoFilter(facebook(), "/login/facebook"), //
        ssoFilter(github(), "/login/github"), //
        ssoFilter(google(), "/login/google") //
    ));
    return filter;
  }

  private Filter ssoFilter(final ClientResources client, final String path) {
    final OAuth2ClientAuthenticationProcessingFilter//
    oAuth2Filter = new OAuth2ClientAuthenticationProcessingFilter(path);
    final OAuth2RestTemplate //
    oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
    oAuth2Filter.setRestTemplate(oAuth2RestTemplate);

    final UserInfoTokenServices//
    userInfoTokenServices = new UserInfoTokenServices(//
        client.getResource().getUserInfoUri(), //
        client.getClient().getClientId()//
    );

    userInfoTokenServices.setRestTemplate(oAuth2RestTemplate);

    oAuth2Filter.setTokenServices(userInfoTokenServices);
    return oAuth2Filter;
  }

  @Bean
  public FilterRegistrationBean oauth2ClientFilterRegistration(
      final OAuth2ClientContextFilter filter) {
    final FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(filter);
    registration.setOrder(-100);
    return registration;
  }

  @Bean
  @ConfigurationProperties("github")
  ClientResources github() {
    return new ClientResources();
  }

  @Bean
  @ConfigurationProperties("facebook")
  ClientResources facebook() {
    return new ClientResources();
  }

  @Bean
  @ConfigurationProperties("google")
  ClientResources google() {
    return new ClientResources();
  }

}


/**
 * Reads config.
 */
class ClientResources {

  private final OAuth2ProtectedResourceDetails client = new AuthorizationCodeResourceDetails();
  private final ResourceServerProperties resource = new ResourceServerProperties();

  public OAuth2ProtectedResourceDetails getClient() {
    return client;
  }

  public ResourceServerProperties getResource() {
    return resource;
  }
}
