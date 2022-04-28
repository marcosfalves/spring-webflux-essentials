package academy.devdojo.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.blockhound.BlockHound

@SpringBootApplication
class SpringWebfluxEssentialsApplication

fun main(args: Array<String>) {
	BlockHound.builder()
		.allowBlockingCallsInside("java.util.UUID", "randomUUID")
		.install()
	runApplication<SpringWebfluxEssentialsApplication>(*args)
}

