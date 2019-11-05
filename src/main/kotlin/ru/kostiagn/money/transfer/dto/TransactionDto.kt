package ru.kostiagn.money.transfer.dto

import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
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


fun TransactionPojo.toDto(): TransactionDto =
    TransactionDto(
        id = this.id,
        fromAccountId = this.fromAccountId,
        toAccountId = this.toAccountId,
        status = this.status,
        amount = this.amount,
        created = this.created,
        updated = this.updated
    )
