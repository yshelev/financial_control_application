package com.example.myapplication.repositories

import com.example.myapplication.dto.BackupData
import com.example.myapplication.dto.CreateBackupRequest
import com.example.myapplication.dto.GetBackupRequest
import com.example.myapplication.network.ApiService
import okhttp3.Response

class BackupRepository(private val apiService: ApiService) {
    suspend fun createBackup(data: CreateBackupRequest): retrofit2.Response<Any> {
        return apiService.createBackup(data)
    }

    suspend fun getBackup(data: GetBackupRequest): retrofit2.Response<BackupData> {
        return apiService.getBackup(data)
    }
}