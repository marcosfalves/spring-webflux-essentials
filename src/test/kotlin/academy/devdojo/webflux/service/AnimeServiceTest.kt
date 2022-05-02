package academy.devdojo.webflux.service

import academy.devdojo.webflux.domain.Anime
import academy.devdojo.webflux.repository.AnimeRepository
import academy.devdojo.webflux.util.AnimeCreator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import reactor.blockhound.BlockHound
import reactor.blockhound.BlockingOperationError
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit


@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AnimeServiceTest() {

    @Mock
    lateinit var animeRepositoryMock: AnimeRepository

    @InjectMocks
    lateinit var animeService: AnimeService

    private val anime = AnimeCreator.createValidAnime()

    @BeforeAll
    fun blockHoundSetup() {
        BlockHound.install()
    }

    @BeforeEach
    fun setUp(){
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
    fun `findAll returns a Flux of Anime`() {
        StepVerifier.create(animeService.findAll())
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `findById returns Mono with anime when it exists`() {
        StepVerifier.create(animeService.findById(1))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `findById returns Mono error when anime does not exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        StepVerifier.create(animeService.findById(1))
            .expectSubscription()
            .expectError(ResponseStatusException::class.java)
            .verify()
    }

    @Test
    fun `save creates an anime when successful`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        StepVerifier.create(animeService.create(animeToBeSaved))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `saveAll creates a list of anime when successful`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        StepVerifier.create(animeService.createAll(listOf(animeToBeSaved, animeToBeSaved)))
            .expectSubscription()
            .expectNext(anime, anime)
            .verifyComplete()
    }

    @Test
    fun `saveAll returns Mono error when one of the objects in the list contains null or empty name`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        BDDMockito.`when`(animeRepositoryMock
            .saveAll(ArgumentMatchers.anyIterable()))
            .thenReturn(Flux.just(anime, anime.copy(name = "")))

        StepVerifier.create(animeService.createAll(listOf(animeToBeSaved, animeToBeSaved.copy(name = ""))))
            .expectSubscription()
            .expectNext(anime)
            .expectError(ResponseStatusException::class.java)
            .verify()
    }

    @Test
    fun `delete removes the anime when successful`() {
        StepVerifier.create(animeService.delete(1))
            .expectSubscription()
            .verifyComplete()
    }

    @Test
    fun `delete returns Mono error when anime does not exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        StepVerifier.create(animeService.delete(1))
            .expectSubscription()
            .expectError(ResponseStatusException::class.java)
            .verify()
    }

    @Test
    fun `update save updated anime and returns empty mono when successful`() {
        StepVerifier.create(animeService.update(AnimeCreator.createValidUpdatedAnime()))
            .expectSubscription()
            .verifyComplete()
    }

    @Test
    fun `update returns Mono error when anime does exist`() {
        BDDMockito.`when`(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        StepVerifier.create(animeService.update(AnimeCreator.createValidUpdatedAnime()))
            .expectSubscription()
            .expectError(ResponseStatusException::class.java)
            .verify()
    }
}