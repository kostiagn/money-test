package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import ru.kostiagn.money.transfer.config.ApplicationConfig

class ApplicationConfigModule : AbstractModule() {
    override fun configure() {
        bind(ApplicationConfig::class.java).asEagerSingleton()
    }
}