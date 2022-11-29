package com.example.demotracing

import io.micrometer.context.ContextSnapshot
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.util.context.ContextView

@RestController
@SpringBootApplication
class DemotracingApplication(
    private val webClient: WebClient,
    private val tracer: Tracer
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/hello")
    fun hello(): Mono<String> {
        return Mono.deferContextual { contextView: ContextView ->
            ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)
                .use { scope: ContextSnapshot.Scope ->
                    val traceId = tracer.currentSpan()!!.context().traceId()
                    logger.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello!", traceId)
                    webClient.get().uri("http://localhost:7654/helloWc")
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .handle { t: String, u: SynchronousSink<String> ->
                            logger.info("Retrieved helloWc {}", t)
                        }
                }
        }
    }

    @GetMapping("/helloWc")
    fun helloWc(): Mono<String> {
        return Mono.deferContextual { contextView: ContextView ->
            ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)
                .use { scope: ContextSnapshot.Scope ->
                    val traceId = tracer.currentSpan()!!.context().traceId()
                    logger.info("<ACCEPTANCE_TEST> <TRACE:{}> HelloWc", traceId)
                    Mono.just(traceId)
                }
        }
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
