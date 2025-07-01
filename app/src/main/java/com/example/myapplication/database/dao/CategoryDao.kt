package com.example.myapplication.database.dao
import androidx.room.*
import com.example.myapplication.database.entities.Category
import kotlinx.coroutines.flow.Flow


@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name AND isIncome = :isIncome LIMIT 1")
    suspend fun getCategoryByNameAndType(name: String, isIncome: Boolean): Category?

    @Query("SELECT * FROM categories WHERE isIncome = :isIncome")
    fun getCategoriesByType(isIncome: Boolean): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<Category>

    @Delete
    suspend fun delete(category: Category)
}