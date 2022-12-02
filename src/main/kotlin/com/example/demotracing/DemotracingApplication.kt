package com.example.demotracing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
class DemotracingApplication

fun main(args: Array<String>) {
    runApplication<DemotracingApplication>(*args)
}
