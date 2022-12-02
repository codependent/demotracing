package com.example.demotracing

import io.micrometer.context.ContextSnapshot
import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import io.micrometer.tracing.Tracer
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import kotlin.coroutines.coroutineContext


@RestController
@SpringBootApplication
class DemotracingApplication(
    private val webClient: WebClient,
    private val tracer: Tracer,
    private val observationRegistry: ObservationRegistry
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/hello")
    suspend fun hello(): String {

        logger.debugAsync("Hey")
        logger.info("Here")

        val contextElement = ContextSnapshot.setThreadLocalsFrom(
            coroutineContext[ReactorContext]!!.context,
            ObservationThreadLocalAccessor.KEY
        )
            .use {
                observationRegistry.asContextElement()
            }
        return withContext(contextElement) {
            val traceId = tracer.currentSpan()?.context()?.traceId()
            logger.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello!", traceId)
            val response: String = webClient.get().uri("http://localhost:7654/helloWc")
                .retrieve()
                .bodyToMono(String::class.java)
                .handle { t: String, u: SynchronousSink<String> ->
                    logger.info("Next {}", t)
                    u.next(t)
                }
                .awaitSingle()
            response
        }


    }

    @GetMapping("/helloWc")
    suspend fun helloWc(): String {
        val contextElement = ContextSnapshot.setThreadLocalsFrom(
            coroutineContext[ReactorContext]!!.context,
            ObservationThreadLocalAccessor.KEY
        )
            .use {
                observationRegistry.asContextElement()
            }
        return withContext(contextElement) {
            val traceId = tracer.currentSpan()?.context()?.traceId()
            logger.info("<ACCEPTANCE_TEST> <TRACE:{}> HelloWc", traceId)
            "Hi there"
        }
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}

suspend fun Logger.debugAsync(message: String) {
    Mono.just(Unit)
        .handle { t: Unit, u: SynchronousSink<Any> ->
            info(message)
            u.next(t)
    }.awaitSingleOrNull()
}
