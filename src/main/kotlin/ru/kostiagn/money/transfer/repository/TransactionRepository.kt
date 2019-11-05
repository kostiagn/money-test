package ru.kostiagn.money.transfer.repository

import org.jooq.Configuration
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo

interface TransactionRepository {
    fun getTransaction(cfg: Configuration, transactionId: Long): TransactionPojo
    fun getTransactionWithLock(cfg: Configuration, transactionId: Long): TransactionPojo
    fun updateStatus(cfg: Configuration, transactionId: Long, newStatus: TransactionStatus)
    fun create(cfg: Configuration, pojo: TransactionPojo): TransactionPojo
}