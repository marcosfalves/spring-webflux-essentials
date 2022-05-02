package academy.devdojo.webflux.controller

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.service.AnimeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/animes")
class AnimeController(val animeService: AnimeService) {

    @GetMapping
    fun listAll(): Flux<Anime> = animeService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): Mono<Anime> = animeService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody anime:Anime): Mono<Anime> = animeService.create(anime)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBatch(@RequestBody animes:List<Anime>): Flux<Anime> = animeService.createAll(animes)

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id:Int, @Valid @RequestBody anime:Anime): Mono<Void> = animeService.update(anime.copy(id = id))

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id:Int): Mono<Void> = animeService.delete(id)

}