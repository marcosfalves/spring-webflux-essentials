package academy.devdojo.webflux.service

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.repository.AnimeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AnimeService(val animeRepository: AnimeRepository) {

    fun findAll(): Flux<Anime> = animeRepository.findAll()

    fun findById(id:Int): Mono<Anime> = animeRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found")))

    fun create(anime: Anime): Mono<Anime> = animeRepository.save(anime)

    @Transactional
    fun createAll(animes: List<Anime>): Flux<Anime> = animeRepository.saveAll(animes)
        .doOnNext(this::throwResponseStatusExceptionWhenEmptyName)

    fun update(anime: Anime): Mono<Void> = findById(anime.id)
            .flatMap{animeRepository.save(anime)}
            .then()

    fun delete(id: Int): Mono<Void> = findById(id)
        .flatMap(animeRepository::delete)

    fun throwResponseStatusExceptionWhenEmptyName(anime: Anime) {
        if (anime.name.isNullOrEmpty()){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name")
        }
    }

}