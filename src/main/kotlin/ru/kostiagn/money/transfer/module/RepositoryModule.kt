package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import ru.kostiagn.money.transfer.repository.AccountRepository
import ru.kostiagn.money.transfer.repository.TransactionRepository
import ru.kostiagn.money.transfer.repository.impl.AccountRepositoryImpl
import ru.kostiagn.money.transfer.repository.impl.TransactionRepositoryImpl

class RepositoryModule : AbstractModule() {
    override fun configure() {
        bind(TransactionRepository::class.java).to(TransactionRepositoryImpl::class.java)
        bind(AccountRepository::class.java).to(AccountRepositoryImpl::class.java)
    }
}
