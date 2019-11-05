package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import ru.kostiagn.money.transfer.service.TransactionService
import ru.kostiagn.money.transfer.service.impl.TransactionServiceImpl

class ServiceModule : AbstractModule() {
    override fun configure() {
        bind(TransactionService::class.java).to(TransactionServiceImpl::class.java)
    }
}
