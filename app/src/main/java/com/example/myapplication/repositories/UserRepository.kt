package com.example.myapplication.repositories
import com.example.myapplication.dto.UserDto
import com.example.myapplication.network.ApiService
import com.example.myapplication.schemas.UserSchema
import retrofit2.Response

class UserRepository(private val apiService: ApiService) {
    suspend fun getUserById(userId: Int): UserDto {
        return apiService.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): Response<UserDto> {
        return apiService.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserSchema): UserDto {
        return apiService.registerUser(user)
    }
}