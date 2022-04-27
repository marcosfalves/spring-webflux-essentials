package academy.devdojo.webflux.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Table("anime")
data class Anime(

    @field:Id
    val id: Int,

    @field:NotNull
    @field:NotEmpty(message = "The name of this anime cannot be empty")
    val name: String

)
