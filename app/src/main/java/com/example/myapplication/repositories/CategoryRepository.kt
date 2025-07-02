package com.example.myapplication.repositories
import com.example.myapplication.dto.CategoryDto
import com.example.myapplication.network.ApiService
import com.example.myapplication.schemas.CreateCategorySchema
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {
    suspend fun getCategoryById(id: Long): CategoryDto {
        return apiService.getCategoryById(id)
    }

    suspend fun addCategory(category: CreateCategorySchema): CategoryDto {
        return apiService.addCategories(category)
    }

    suspend fun deleteCategory(id: Long): Response<Void> {
        return apiService.deleteCategory(id)
    }

    suspend fun getCategories(email: String): List<CategoryDto> {
        return apiService.getCategories(email)
    }

    suspend fun deleteUserCategories(userId: Long): Response<Void> {
        return apiService.deleteUserCategories(userId)
    }
}