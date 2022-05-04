package academy.devdojo.webflux.repository

import academy.devdojo.webflux.domain.AppUser
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AppUserRepository : ReactiveCrudRepository<AppUser, Int> {

    fun findByUsername(username:String): Mono<AppUser>

}