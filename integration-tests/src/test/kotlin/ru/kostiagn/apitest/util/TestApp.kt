package ru.kostiagn.apitest.util

import java.util.Properties

object TestApp {
    const val LOCAL_ENV = "local"
    const val ENV = "env"
    const val APP_NAME = "money-test"
    const val APP_MAIN_CLASS = "ru.kostiagn.money.transfer.MainKt"
    const val INTEGRATION_TEST_DIR = "integration-tests"

    val env: String by lazy {
        val env = System.getProperty(ENV)
        if (env.isNullOrBlank()) "local" else env
    }

    val moneyDbJdbcUrl: String by lazy { System.getProperty("MONEY_DB_URL") }
    val moneyDbUsername: String by lazy { System.getProperty("MONEY_DB_USERNAME") }
    val moneyDbPassword: String by lazy { System.getProperty("MONEY_DB_PASSWORD") }
    val port: Int by lazy { System.getProperty("PORT").toInt() }
    val url: String by lazy { System.getProperty("URL") }

    fun init() {
        this::class.java.classLoader
            .getResource("""$APP_NAME-$env.properties""")?.apply {
                val properties = Properties(System.getProperties())
                properties.load(openStream())
                System.setProperties(properties)
            }
    }
}
