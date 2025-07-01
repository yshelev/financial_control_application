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

    suspend fun registerWithEmailVerification(user: UserSchema): Response<UserDto> {
        return apiService.registerWithVerification(user)
    }

    suspend fun verifyEmail(token: String): Response<Unit> {
        return apiService.verifyEmail(token)
    }

    suspend fun resendVerificationEmail(email: String): Response<Unit> {
        return apiService.resendVerification(email)
    }

    suspend fun checkEmailVerified(email: String): Response<Boolean> {
        return apiService.checkEmailVerified(email)
    }
}