package academy.devdojo.webflux.service

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.repository.AnimeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimeService(val animeRepository: AnimeRepository) {

    fun findAll(): Flux<Anime> {
        return animeRepository.findAll()
    }

    fun findById(id:Int): Mono<Anime> {
        return animeRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found")))
            .log()
    }

}