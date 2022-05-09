package academy.devdojo.webflux.service

import academy.devdojo.webflux.repository.AppUserRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AppUserDetailsService(
    val appUserRepository: AppUserRepository
): ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return appUserRepository.findByUsername(username)
            .cast(UserDetails::class.java)
    }
}