package ru.kostiagn.money.transfer.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.Inject
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import mu.KLogging
import ru.kostiagn.money.transfer.repository.impl.RecordNotFoundException
import ru.kostiagn.money.transfer.service.impl.AccountMustBeInOpenStatus
import ru.kostiagn.money.transfer.service.impl.NotEnoughMoney
import ru.kostiagn.money.transfer.service.impl.TransactionAmountMustBeGreaterThenZero
import ru.kostiagn.money.transfer.service.impl.TransactionMustBeInInitStatus

class ApplicationConfig @Inject constructor(application: Application) {
    companion object : KLogging()

    init {
        application.apply {
            install(DefaultHeaders)
            install(CallLogging)
            install(ContentNegotiation) {
                jackson {
                    configure(SerializationFeature.INDENT_OUTPUT, true)
                }
            }
            install(StatusPages) {
                exception<RecordNotFoundException> {
                    call.respond(HttpStatusCode.NotFound)
                }
                exception<TransactionMustBeInInitStatus> {
                    call.respond(HttpStatusCode.ExpectationFailed, "transaction has closed yet")
                }
                exception<TransactionAmountMustBeGreaterThenZero> {
                    call.respond(HttpStatusCode.ExpectationFailed, "transaction amount must be greater then zero")
                }
                exception<AccountMustBeInOpenStatus> {
                    call.respond(HttpStatusCode.ExpectationFailed, "account is not in open status")
                }
                exception<NotEnoughMoney> {
                    call.respond(HttpStatusCode.ExpectationFailed, "there is not enough money for transaction")
                }
                exception<Throwable> {
                    call.respond(HttpStatusCode.InternalServerError, "the internal server error")
                }
            }
        }
    }
}

