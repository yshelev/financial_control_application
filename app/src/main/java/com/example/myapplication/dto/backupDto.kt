package com.example.myapplication.dto

import com.example.myapplication.database.entities.ExchangeRateEntity

data class CreateBackupRequest (
    val user_email: String,
    val data: BackupData
)

data class BackupData(
    val cards: List<CardDto>,
    val transactions: List<TransactionDto>,
    val exchange_rates: List<ExchangeRateEntity>
)

data class GetBackupRequest(
    val email: String
)