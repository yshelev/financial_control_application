package com.example.myapplication.repositories
import com.example.myapplication.database.entities.User
import com.example.myapplication.network.ApiService
import retrofit2.Response

class UserRepository(private val apiService: ApiService) {
    suspend fun getUserById(userId: Int): Response<User> {
        return apiService.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): Response<User> {
        return apiService.getUserByEmail(email)
    }

    suspend fun registerUser(user: User): Response<User> {
        return apiService.registerUser(user)
    }
}