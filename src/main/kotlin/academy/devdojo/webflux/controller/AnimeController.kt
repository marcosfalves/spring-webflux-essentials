package academy.devdojo.webflux.controller

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.service.AnimeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/animes")
@SecurityScheme(
    name = "Basic Authentication",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
class AnimeController(val animeService: AnimeService) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all animes",
        security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun listAll(): Flux<Anime> = animeService.findAll()

    @GetMapping("/{id}")
    @Operation(security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun findById(@PathVariable id: Int): Mono<Anime> = animeService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun create(@Valid @RequestBody anime:Anime): Mono<Anime> = animeService.create(anime)

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun createBatch(@RequestBody animes:List<Anime>): Flux<Anime> = animeService.createAll(animes)

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun update(@PathVariable id:Int, @Valid @RequestBody anime:Anime): Mono<Void> = animeService.update(anime.copy(id = id))

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = [SecurityRequirement(name = "Basic Authentication")],
        tags = ["anime"])
    fun delete(@PathVariable id:Int): Mono<Void> = animeService.delete(id)

}