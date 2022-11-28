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
        val newSpan: Span = tracer.nextSpan().name("hey").start()
        try {
            tracer.withSpan(newSpan.start()).use { ws ->
                // You can tag a span - put a key value pair on it for better debugging
                newSpan.tag("taxValue", "hi!")
                // You can log an event on a span - an event is an annotated timestamp
                newSpan.event("taxCalculated")
            }
        } finally {
            newSpan.end()
        }
        logger.info("Hello!")
        return "hello"
    }

}

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
