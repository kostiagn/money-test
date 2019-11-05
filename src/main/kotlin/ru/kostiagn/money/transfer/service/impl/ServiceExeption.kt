package ru.kostiagn.money.transfer.service.impl

import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import java.math.BigDecimal


class TransactionMustBeInInitStatus(transactionId: Long, status: TransactionStatus) :
    RuntimeException("transaction must be in status INIT. id = $transactionId status = $status")

class TransactionAmountMustBeGreaterThenZero(transactionId: Long?, amount: BigDecimal) :
    RuntimeException("transaction amount must be greater then zero. id = $transactionId amount = $amount")

abstract class FailTransactionException(msg: String) : RuntimeException(msg)

class AccountMustBeInOpenStatus(accountId: Long, status: AccountStatus) :
    FailTransactionException("account must be in status OPEN. id = $accountId status = $status")

class NotEnoughMoney(accountId: Long, balance: BigDecimal, amount: BigDecimal) :
    FailTransactionException("account do not have enough money. accountId = $accountId, balance = $balance, transfer amount = $amount")

