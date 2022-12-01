package com.example.demotracing

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import io.micrometer.tracing.Tracer
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient


@RestController
@SpringBootApplication
class DemotracingApplication(
    private val webClient: WebClient,
    private val tracer: Tracer,
    private val observationRegistry: ObservationRegistry
) {

    private val logger = LoggerFactory.getLogger(javaClass)

            @GetMapping("/hello")
            suspend fun hello(): String {
                val context = observationRegistry.asContextElement()
                return withContext(context) {
                    val traceId = tracer.currentSpan()?.context()?.traceId()
                    logger.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello!", traceId)
                    val response: String = webClient.get().uri("http://localhost:7654/helloWc")
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .awaitSingle()
                    response
                }


            }

    @GetMapping("/helloWc")
    suspend fun helloWc(): String {
        val context = observationRegistry.asContextElement()
        return withContext(context) {
            val traceId = tracer.currentSpan()?.context()?.traceId()
            logger.info("<ACCEPTANCE_TEST> <TRACE:{}> HelloWc", traceId)
            "Hi there"
        }
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
