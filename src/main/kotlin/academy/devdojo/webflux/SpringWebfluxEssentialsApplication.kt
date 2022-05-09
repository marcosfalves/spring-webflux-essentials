package academy.devdojo.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.blockhound.BlockHound

@SpringBootApplication
class SpringWebfluxEssentialsApplication

fun main(args: Array<String>) {
	//println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("devdojo"))
	BlockHound.builder()
		.allowBlockingCallsInside("java.util.UUID", "randomUUID")
		.allowBlockingCallsInside("java.io.FilterInputStream", "read")
		.allowBlockingCallsInside("java.io.InputStream", "readNBytes")
		.install()
	runApplication<SpringWebfluxEssentialsApplication>(*args)
}

