package com.example.demotracing

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