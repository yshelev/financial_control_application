package com.example.myapplication.schemas

import java.math.BigDecimal

data class BalanceCardUpdateSchema (
    val card_id: Long,
    val new_balance: BigDecimal
)