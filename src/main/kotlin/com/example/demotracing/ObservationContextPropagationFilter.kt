package com.example.demotracing

import io.micrometer.context.ContextSnapshot
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.util.context.ContextView


@Component
class ObservationContextPropagationFilter: WebFilter {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return Mono.deferContextual { contextView: ContextView ->
            ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)
                .use { scope: ContextSnapshot.Scope ->
                    logger.info("Filter")
                    chain.filter(exchange)
                }
        }
    }

}