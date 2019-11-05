package ru.kostiagn.money.transfer

import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.ktor.application.Application
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import ru.kostiagn.money.transfer.module.ApplicationConfigModule
import ru.kostiagn.money.transfer.module.RoutesModule
import ru.kostiagn.money.transfer.service.TransactionService

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}

fun Application.testModule(transactionService: TransactionService) {
    Guice.createInjector(
        MainModule(this),
        ApplicationConfigModule(),
        RoutesModule(),
        object : AbstractModule() {
            override fun configure() {
                bind(TransactionService::class.java).toInstance(transactionService)
            }
        })
}

