package ru.kostiagn.apitest.dto

import java.math.BigDecimal
import java.sql.Timestamp

data class TransactionDto(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val status: TransactionStatus,
    val amount: BigDecimal,
    val created: Timestamp,
    val updated: Timestamp
)

