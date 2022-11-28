package com.example.demotracing

import io.micrometer.tracing.Span
import io.micrometer.tracing.brave.bridge.BraveTracer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
class DemotracingApplication (private val tracer: BraveTracer){

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/hello")
    fun hello(): String {
        val currentSpan: Span? = tracer.currentSpan()
        val start: Span = tracer.spanBuilder().name("hey").start()
        logger.info("Hello!")
        return "hello"
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
