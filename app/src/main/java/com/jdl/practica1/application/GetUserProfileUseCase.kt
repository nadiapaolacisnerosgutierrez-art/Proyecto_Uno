package com.jdl.practica1.application

import com.jdl.practica1.domain.User
import com.jdl.practica1.domain.UserRepository

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<User> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("El ID no puede estar vacío"))
        }

        val user = userRepository.getUserById(userId)
            ?: return Result.failure(NoSuchElementException("Usuario no encontrado"))

        return Result.success(user)
    }
}