package com.jdl.practica1.domain

interface UserRepository {
    suspend fun getUserById(id: String): User?
}