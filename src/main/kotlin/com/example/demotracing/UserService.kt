package com.example.demotracing

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service

interface UserService {
    suspend fun getName(): String
}