package com.example.demotracing

import io.micrometer.context.ContextSnapshot
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.ContextView

fun <T> setupObservationContext(f: () -> Mono<T>): Mono<T> =
    Mono.deferContextual { contextView: ContextView ->
        ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)
            .use { scope: ContextSnapshot.Scope ->
                f()
            }
    }