package academy.devdojo.webflux.integration

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.repository.AnimeRepository
import academy.devdojo.webflux.util.AnimeCreator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
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

    val URI_MAPPING = "/animes"

    //didactic solution to not need to create test database (not used in production)
    @MockBean
    private lateinit var animeRepositoryMock: AnimeRepository

    @Autowired
    private lateinit var testClient: WebTestClient

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
    fun `listAll returns a Flux of Anime`() {
        testClient
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(anime.id)
            .jsonPath("$.[0].name").isEqualTo(anime.name)
    }

    @Test
    fun `listAll Flavor2 returns a Flux of Anime`() {
        testClient
            .get()
            .uri(URI_MAPPING)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Anime::class.java)
            .hasSize(1)
            .contains(anime)
    }

    @Test
    fun `findById returns Mono with anime when it exists`() {
        testClient
            .get()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isOk
            .expectBody(Anime::class.java)
            .isEqualTo(anime)
    }

    @Test
    fun `findById returns Mono error when anime does not exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        testClient
            .get()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")
    }

    @Test
    fun `save creates an anime when successful`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        testClient
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
    fun `save returns mono error with bad request when name is empty`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved().copy(name = "")

        testClient
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
    fun `saveAll creates a list of anime when successful`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        testClient
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
    fun `saveAll returns Mono error when one of the objects in the list contains null or empty name`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        BDDMockito.`when`(animeRepositoryMock
            .saveAll(ArgumentMatchers.anyIterable()))
            .thenReturn(Flux.just(anime, anime.copy(name = "")))

        testClient
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
    fun `delete removes the anime when successful`() {
        testClient
            .delete()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `delete returns Mono error when anime does not exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        testClient
            .delete()
            .uri("$URI_MAPPING/{id}", 1)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened")

    }

    @Test
    fun `update save updated anime and returns empty mono when successful`() {

        testClient
            .put()
            .uri("$URI_MAPPING/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(AnimeCreator.createValidUpdatedAnime()))
            .exchange()
            .expectStatus().isNoContent

    }

    @Test
    fun `update returns Mono error when anime does exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        testClient
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