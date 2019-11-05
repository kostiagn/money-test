package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import ru.kostiagn.money.transfer.web.TransactionRoutes
import ru.kostiagn.money.transfer.web.VersionRoutes

class RoutesModule : AbstractModule() {
    override fun configure() {
        bind(TransactionRoutes::class.java).asEagerSingleton()
        bind(VersionRoutes::class.java).asEagerSingleton()
    }

}