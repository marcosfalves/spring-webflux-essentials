package academy.devdojo.webflux.config

import academy.devdojo.webflux.service.AppUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http:ServerHttpSecurity): SecurityWebFilterChain {
        //@formatter:off
        return http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/animes/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/animes/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/animes/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/animes/**").hasRole("USER")
            .anyExchange().authenticated()
            .and()
                .formLogin()
            .and()
                .httpBasic()
            .and()
                .build()
        //@formatter:on
    }

    @Bean
    fun authenticationManager(appUserDetailsService: AppUserDetailsService) : ReactiveAuthenticationManager
        = UserDetailsRepositoryReactiveAuthenticationManager(appUserDetailsService)

}