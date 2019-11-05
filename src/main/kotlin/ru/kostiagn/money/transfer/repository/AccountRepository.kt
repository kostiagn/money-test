package ru.kostiagn.money.transfer.repository

import org.jooq.Configuration
import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import java.math.BigDecimal

interface AccountRepository {
    fun getAccount(cfg: Configuration, accountId: Long): AccountPojo
    fun getAccountWithLock(cfg: Configuration, accountId: Long): AccountPojo
    fun updateBalance(cfg: Configuration, accountId: Long, newBalance: BigDecimal)
    fun updateStatus(cfg: Configuration, accountId: Long, newStatus: AccountStatus)
    fun create(cfg: Configuration, pojo: AccountPojo): AccountPojo
}

