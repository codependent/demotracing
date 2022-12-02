package com.example.demotracing

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.observability.DefaultSignalListener
import reactor.core.observability.SignalListener
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import java.util.function.Supplier

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class RequestLoggerFilter : WebFilter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return setupObservationContext {
            logger.info("Beginning request")
            chain.filter(exchange)
                .tap(Supplier<SignalListener<Void>> {
                    val x = object : DefaultSignalListener<Void>() {
                        override fun doFinally(terminationType: SignalType) {
                            logger.info("Finishing request")
                        }
                    }
                    x
                })
        }
    }

}