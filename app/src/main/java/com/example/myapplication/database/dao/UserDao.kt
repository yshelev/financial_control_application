package com.example.myapplication.database.dao

import androidx.room.*
import com.example.myapplication.database.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE users SET isVerified = :isVerified WHERE email = :email")
    suspend fun updateVerificationStatus(email: String, isVerified: Boolean)

    @Query("SELECT isVerified FROM users WHERE email = :email")
    suspend fun isUserVerified(email: String): Boolean?
}