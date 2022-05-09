package academy.devdojo.webflux.util

import org.springframework.context.ApplicationContext
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions

@Component
class WebTestClientUtil(
    val applicationContext: ApplicationContext
) {
    fun authenticateClient(username:String, password:String): WebTestClient {
        return WebTestClient.bindToApplicationContext(applicationContext)
            .apply(SecurityMockServerConfigurers.springSecurity())
            .configureClient()
            .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
            .build()
    }
}