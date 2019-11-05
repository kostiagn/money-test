package ru.kostiagn.apitest.dao

import ru.kostiagn.apitest.db.MoneyDb
import ru.kostiagn.apitest.dto.TransactionDto

class TransactionDao(private val moneyDb: MoneyDb) {
    fun selectTransaction(transactionId: Long): TransactionDto =
        moneyDb.selectOne(
            //language=PostgreSQL
            """
              select id, from_account_id as fromAccountId, to_account_id as toAccountId, status, amount, created, updated
               from transaction where id = $transactionId
            """
        )
}
