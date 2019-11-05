package ru.kostiagn.apitest.dao

import ru.kostiagn.apitest.db.MoneyDb
import ru.kostiagn.apitest.dto.AccountDto
import ru.kostiagn.apitest.dto.AccountStatus
import java.math.BigDecimal

class AccountDao(private val moneyDb: MoneyDb) {
    fun insertAccount(balance: BigDecimal, status: AccountStatus = AccountStatus.OPEN): AccountDto =
        moneyDb.insert(
            //language=PostgreSQL
            """
              insert into account (balance, status, created, updated)
                     values ('$balance', '$status', now(), now())
                     returning *
            """
        )

    fun selectAccount(accountId: Long): AccountDto =
        moneyDb.selectOne(
            //language=PostgreSQL
            """
              select * from account where id = $accountId
            """
        )

    fun updateAccount(accountId: Long, balance: BigDecimal, status: AccountStatus) {
        moneyDb.update(
            //language=PostgreSQL
            """
              update account set balance = '$balance', status = '$status' where id = $accountId
            """
        )
    }


}
