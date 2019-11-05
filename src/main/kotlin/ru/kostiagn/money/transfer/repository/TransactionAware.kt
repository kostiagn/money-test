package ru.kostiagn.money.transfer.repository

import org.jooq.Configuration

interface TransactionAware {
    suspend fun <T> inTx(block: (configuration: Configuration) -> T): T
}