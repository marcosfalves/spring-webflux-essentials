package academy.devdojo.webflux.controller

import academy.devdojo.webflux.service.AnimeService
import academy.devdojo.webflux.util.AnimeCreator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.test.context.junit.jupiter.SpringExtension
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
internal class AnimeControllerTest() {

    @Mock
    lateinit var animeServiceMock: AnimeService

    @InjectMocks
    lateinit var animeController: AnimeController

    private val anime = AnimeCreator.createValidAnime()

    @BeforeAll
    fun blockHoundSetup() {
        BlockHound.install()
    }

    @BeforeEach
    fun setUp(){
        BDDMockito.`when`(animeServiceMock.findAll())
            .thenReturn(Flux.just(anime))

        BDDMockito.`when`(animeServiceMock.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.just(anime))

        BDDMockito.`when`(animeServiceMock.create(AnimeCreator.createAnimeToBeSaved()))
            .thenReturn(Mono.just(anime))

        BDDMockito.`when`(animeServiceMock.delete(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty())

        BDDMockito.`when`(animeServiceMock.update(AnimeCreator.createValidUpdatedAnime()))
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
        StepVerifier.create(animeController.listAll())
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `findById returns Mono with anime when it exists`() {
        StepVerifier.create(animeController.findById(1))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `save creates an anime when successful`() {
        val animeToBeSaved = AnimeCreator.createAnimeToBeSaved()

        StepVerifier.create(animeController.create(animeToBeSaved))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete()
    }

    @Test
    fun `delete removes the anime when successful`() {
        StepVerifier.create(animeController.delete(1))
            .expectSubscription()
            .verifyComplete()
    }

    @Test
    fun `update save updated anime and returns empty mono when successful`() {
        StepVerifier.create(animeController.update(1, AnimeCreator.createValidUpdatedAnime()))
            .expectSubscription()
            .verifyComplete()
    }

}