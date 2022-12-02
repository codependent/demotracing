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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


@RestController
class HelloRestController(
    private val webClient: WebClient,
    private val tracer: Tracer,
    private val observationRegistry: ObservationRegistry,
    private val userService: UserService
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/hello")
    suspend fun hello(): String {

        logger.observedInfo("Hey")
        logger.observedInfo("My message is {}", "Hey")
        logger.info("Here")

        return withContext(generateObservationContextElement(observationRegistry)) {
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
        return withContext(generateObservationContextElement(observationRegistry)) {
            val traceId = tracer.currentSpan()?.context()?.traceId()
            logger.info("<ACCEPTANCE_TEST> <TRACE:{}> HelloWc", traceId)
            userService.getName()
        }
    }
}

suspend fun generateObservationContextElement(observationRegistry: ObservationRegistry): CoroutineContext {
    return ContextSnapshot.setThreadLocalsFrom(
        coroutineContext[ReactorContext]!!.context,
        ObservationThreadLocalAccessor.KEY
    )
    .use {
        observationRegistry.asContextElement()
    }
}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}

suspend fun Logger.observedInfo(message: String) {
    observedInfoGen { info(message) }
}

suspend fun Logger.observedInfo(message: String, arg: Any) {
    observedInfoGen { info(message, arg) }
}

suspend fun Logger.observedInfo(message: String, arg: Any, arg2: Any) {
    observedInfoGen{ info(message, arg, arg2) }
}

private suspend fun observedInfoGen(f: () -> Any?) {
    Mono.just(Unit)
        .handle { t: Unit, u: SynchronousSink<Any> ->
            f()
            u.next(t)
        }.awaitSingleOrNull()
}