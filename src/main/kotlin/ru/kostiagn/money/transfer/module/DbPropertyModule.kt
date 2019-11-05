package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import ru.kostiagn.money.transfer.config.DbProperties

class DbPropertyModule(private val application: Application) : AbstractModule() {
    @KtorExperimentalAPI
    override fun configure() {
        val dbProperties = DbProperties(
            url = application.environment.config.propertyOrNull("ktor.database.url")?.getString()
                ?: throw RuntimeException("db url is not defined"),
            username = application.environment.config.propertyOrNull("ktor.database.username")?.getString()
                ?: throw RuntimeException("db username is not defined"),
            password = application.environment.config.propertyOrNull("ktor.database.password")?.getString()
                ?: throw RuntimeException("db password is not defined")
        )

        bind(DbProperties::class.java).toInstance(dbProperties)
    }
}