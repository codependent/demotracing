package com.example.demotracing

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getName(): String {
        logger.info("Getting name")
        delay(100)
        return "Joe"
    }
}