package com.example.demotracing

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
@SpringBootApplication
class DemotracingApplication(
    private val webClient: WebClient,
    private val observationRegistry: ObservationRegistry,
    private val tracer: Tracer
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/hello")
    fun hello(): Mono<String> {
        val observation: Observation = Observation.start("webclient-sample", observationRegistry)
        return Mono.just(observation).flatMap { span ->
            observation.scoped {
                logger.info("Hello!")
            }
            webClient.get().uri("http://localhost:7654/helloWc")
                .retrieve()
                .bodyToMono(String::class.java)
                .doOnNext {
                    observation.scoped {
                        logger.info("Retrieved hello")
                    }
                }
        }.doFinally { signalType ->
            observation.stop()
        }.contextWrite { it.put(ObservationThreadLocalAccessor.KEY, observation) }
    }

    @GetMapping("/helloWc")
    fun helloWc(): Mono<String> {
        val observation: Observation = Observation.start("webclient-samplewc", observationRegistry)
        observation.scoped {
            logger.info("HelloWc!")
        }
        return Mono.just("HelloWc")
            .map {
                observation.scoped {
                    logger.info("HelloWc map!")
                }
                it
            }
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
