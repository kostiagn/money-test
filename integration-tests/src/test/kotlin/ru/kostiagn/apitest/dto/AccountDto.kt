package ru.kostiagn.apitest.dto

import java.math.BigDecimal
import java.sql.Timestamp

data class AccountDto(
    val id: Long,
    val balance: BigDecimal,
    val status: AccountStatus,
    val created: Timestamp,
    val updated: Timestamp
)

