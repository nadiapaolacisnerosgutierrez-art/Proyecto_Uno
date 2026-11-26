package com.jdl.practica1.infrastructure

import com.jdl.practica1.domain.User
import com.jdl.practica1.domain.UserRepository
import kotlinx.coroutines.delay

class MockUserRepository : UserRepository {
    override suspend fun getUserById(id: String): User? {
        delay(1500) // Simula la carga de red
        return User(
            id = id,
            name = "Juan Pérez",
            email = "juan.perez@example.com"
        )
    }
}