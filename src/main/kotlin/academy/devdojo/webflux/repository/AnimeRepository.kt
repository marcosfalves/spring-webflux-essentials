package academy.devdojo.webflux.repository

import academy.devdojo.webflux.domain.Anime
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AnimeRepository: ReactiveCrudRepository<Anime, Int> {

}