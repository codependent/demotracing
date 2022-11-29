package com.example.demotracing

import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient


@Configuration
internal class WebClientConfiguration {

    @Bean
    fun webClient (webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder.build()
    }
}