package ru.kostiagn.money.transfer

import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.ktor.application.Application
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import ru.kostiagn.money.transfer.module.ApplicationConfigModule
import ru.kostiagn.money.transfer.module.DbModule
import ru.kostiagn.money.transfer.module.DbPropertyModule
import ru.kostiagn.money.transfer.module.RepositoryModule
import ru.kostiagn.money.transfer.module.RoutesModule
import ru.kostiagn.money.transfer.module.ServiceModule


fun Application.module() {
    Guice.createInjector(
        MainModule(this),
        ApplicationConfigModule(),
        DbPropertyModule(this),
        DbModule(),
        RepositoryModule(),
        ServiceModule(),
        RoutesModule()
    )
}


class MainModule(private val application: Application) : AbstractModule() {
    override fun configure() {
        bind(Application::class.java).toInstance(application)
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}