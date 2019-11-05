package ru.kostiagn.money.transfer.repository.impl

import org.jooq.Configuration
import org.jooq.impl.DSL
import ru.kostiagn.money.transfer.persistence.Tables.TRANSACTION
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
import ru.kostiagn.money.transfer.repository.TransactionRepository


class TransactionRepositoryImpl : TransactionRepository {
    override fun getTransaction(cfg: Configuration, transactionId: Long): TransactionPojo =
        DSL.using(cfg).selectFrom(TRANSACTION)
            .where(TRANSACTION.ID.eq(transactionId))
            .fetchOneIntoObj() ?: throw RecordNotFoundException("account_transcactoin with id $transactionId not found")

    override fun getTransactionWithLock(cfg: Configuration, transactionId: Long): TransactionPojo =
        DSL.using(cfg).selectFrom(TRANSACTION)
            .where(TRANSACTION.ID.eq(transactionId))
            .forNoKeyUpdate()
            .fetchOneIntoObj() ?: throw RecordNotFoundException("account_transcactoin with id $transactionId not found")

    override fun updateStatus(cfg: Configuration, transactionId: Long, newStatus: TransactionStatus) {
        DSL.using(cfg).update(TRANSACTION)
            .set(TRANSACTION.STATUS, newStatus)
            .set(TRANSACTION.UPDATED, now())
            .where(TRANSACTION.ID.eq(transactionId))
            .execute()
    }

    override fun create(cfg: Configuration, pojo: TransactionPojo): TransactionPojo {
        val now = now()
        return DSL.using(cfg).newRecord(TRANSACTION)
            .run {
                fromAccountId = pojo.fromAccountId
                toAccountId = pojo.toAccountId
                amount = pojo.amount
                status = pojo.status
                created = pojo.created ?: now
                updated = pojo.updated ?: now

                store()
                intoObj()
            }
    }
}