package academy.devdojo.webflux.util

import academy.devdojo.webflux.domain.Anime

class AnimeCreator {
    companion object  {
        fun createAnimeToBeSaved() = Anime(0, "Tensei Shitara Slime Datta Ken")
        fun createValidAnime() = Anime(1, "Tensei Shitara Slime Datta Ken")
        fun createValidUpdatedAnime() = Anime(1, "Tensei Shitara Slime Datta Ken 2")
    }
}