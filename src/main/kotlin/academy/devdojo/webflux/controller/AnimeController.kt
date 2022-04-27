package academy.devdojo.webflux.controller

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.service.AnimeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/animes")
class AnimeController(val animeService: AnimeService) {

    @GetMapping
    fun listAll(): Flux<Anime> = animeService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): Mono<Anime> = animeService.findById(id)

}