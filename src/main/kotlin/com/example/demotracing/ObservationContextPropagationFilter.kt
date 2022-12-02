package com.example.demotracing

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink


@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class RequestLoggerPropagationFilter: WebFilter {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return Mono.just(Unit)
            .handle { t: Unit, u: SynchronousSink<Any> ->
                logger.info("beginning request")
                u.next(t)
            }.flatMap {
                chain.filter(exchange)
            }
        /*

        return Mono.deferContextual { contextView: ContextView ->
            ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)
                .use { scope: ContextSnapshot.Scope ->
                    logger.info("Filter")
                    chain.filter(exchange)
                }
        }*/
    }

}