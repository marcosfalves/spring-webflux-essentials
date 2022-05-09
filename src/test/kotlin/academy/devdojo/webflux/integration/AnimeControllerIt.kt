package academy.devdojo.webflux.integration

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.repository.AnimeRepository
import academy.devdojo.webflux.util.AnimeCreator
import academy.devdojo.webflux.util.WebTestClientUtil
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.blockhound.BlockHound
import reactor.blockhound.BlockingOperationError
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
//@WebFluxTest
//@Import(AnimeService::class, CustomAttributes::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnimeControllerIt {

    companion object {
        private const val URI_MAPPING = "/animes"
        private const val ADMIN_USER = "malves"
        private const val REGULAR_USER = "user"
    }

    //didactic solution to not need to create test database (not used in production)
    @MockBean
    private lateinit var animeRepositoryMock: AnimeRepository

    @Autowired
    private lateinit var client: WebTestClient

    private val anime = AnimeCreator.createValidAnime()

    @BeforeAll
    fun blockHoundSetup() {
        BlockHound.builder()
            .allowBlockingCallsInside("java.util.UUID", "randomUUID")
            .install()
    }

    @BeforeEach
    fun setUp() {
        BDDMockito.`when`(animeRepositoryMock.findAll())
            .thenReturn(Flux.just(anime))

        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.just(anime))

        BDDMockito.`when`(animeRepositoryMock.save(AnimeCreator.createAnimeToBeSaved()))
            .thenReturn(Mono.just(anime))

        BDDMockito.`when`(animeRepositoryMock.saveAll(listOf(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
            .thenReturn(Flux.just(anime, anime))

        BDDMockito.`when`(animeRepositoryMock.delete(ArgumentMatchers.any(Anime::class.java)))
            .thenReturn(Mono.empty())

        BDDMockito.`when`(animeRepositoryMock.save(AnimeCreator.createValidUpdatedAnime()))
            .thenReturn(Mono.empty())
    }

    @Test
    fun `Verifying operations non blocking with blockHound`() {
        try {
            val task: FutureTask<*> = FutureTask {
                Thread.sleep(0)
                ""
            }
            Schedulers.parallel().schedule(task)
            task[10, TimeUnit.SECONDS]
            Assertions.fail("should fail")
        } catch (e: Exception) {
            Assertions.assertTrue(e.cause is BlockingOperationError)
        }
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `listAll returns a Flux of Anime when user is successfully authenticated and has role ADMIN`() {
        client
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(anime.id)
            .jsonPath("$.[0].name").isEqualTo(anime.name)
    }

    @Test
    @WithUserDetails(REGULAR_USER)
    fun `listAll returns forbidden when user is successfully authenticated and does not have role ADMIN`() {
        client
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `listAll returns unauthorized when user is not authenticated`() {
        client
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `listAll Flavor2 returns a Flux of Anime when user is successfully authenticated and has role ADMI`() {
        client
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Anime::class.java)
            .hasSize(1)
            .contains(anime)
    }

    @Test
    @WithUserDetails(REGULAR_USER)
    fun `findById returns Mono with anime when it exists and user is successfully authenticated and has role USER`() {
        client
            .get()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isOk
            .expectBody(Anime::class.java)
            .isEqualTo(anime)
    }

    @Test
    @WithUserDetails(REGULAR_USER)
    fun `findById returns Mono error when anime does not exist and user is successfully authenticated and has role USER`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        client
            .get()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `save creates an anime when successful when user is successfully authenticated and has role ADMIN`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        client
            .post()
            .uri(URI_MAPPING)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animeToBeSaved))
            .exchange()
            .expectStatus().isCreated
            .expectBody(Anime::class.java)
            .isEqualTo(anime)
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `save returns mono error with bad request when name is empty and user is successfully authenticated and has role ADMIN`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved().copy(name = "")

        client
            .post()
            .uri(URI_MAPPING)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animeToBeSaved))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `saveAll creates a list of anime when successful and user is successfully authenticated and has role ADMIN`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        client
            .post()
            .uri("$URI_MAPPING/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(listOf(animeToBeSaved, animeToBeSaved)))
            .exchange()
            .expectStatus().isCreated
            .expectBodyList(Anime::class.java)
            .hasSize(2)
            .contains(anime)
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `saveAll returns Mono error when one of the objects in the list contains null or empty name and user is successfully authenticated and has role ADMIN`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        BDDMockito.`when`(animeRepositoryMock
            .saveAll(ArgumentMatchers.anyIterable()))
            .thenReturn(Flux.just(anime, anime.copy(name = "")))

        client
            .post()
            .uri("$URI_MAPPING/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(listOf(animeToBeSaved, animeToBeSaved)))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `delete removes the anime when successful and user is successfully authenticated and has role ADMIN`() {
        client
            .delete()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `delete returns Mono error when anime does not exist and user is successfully authenticated and has role ADMIN`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        client
            .delete()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `update save updated anime and returns empty mono when successful and user is successfully authenticated and has role ADMIN`() {

        client
            .put()
            .uri("$URI_MAPPING/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(AnimeCreator.createValidUpdatedAnime()))
            .exchange()
            .expectStatus().isNoContent

    }

    @Test
    @WithUserDetails(ADMIN_USER)
    fun `update returns Mono error when anime does exist and user is successfully authenticated and has role ADMIN`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        client
            .put()
            .uri("$URI_MAPPING/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(AnimeCreator.createValidUpdatedAnime()))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

}