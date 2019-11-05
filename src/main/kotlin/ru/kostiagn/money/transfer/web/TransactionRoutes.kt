package ru.kostiagn.money.transfer.web

import com.google.inject.Inject
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import ru.kostiagn.money.transfer.dto.TransactionRequestDto
import ru.kostiagn.money.transfer.service.TransactionService

class TransactionRoutes @Inject constructor(
    application: Application,
    transactionService: TransactionService
) {
    init {
        application.routing {
            route("/transaction") {
                get("/{transactionId}") {
                    val transactionId = call.parameters["transactionId"]?.toLong() ?: throw ParameterMissing("Must provide transactionId")
                    val transaction = transactionService.getTransaction(transactionId)
                    call.respond(transaction)
                }
                post("") {
                    val dto = call.receive<TransactionRequestDto>()
                    val transaction = transactionService.initTransaction(dto)
                    call.respond(transaction)
                }
                put("/{transactionId}") {
                    val transactionId = call.parameters["transactionId"]?.toLong() ?: throw ParameterMissing("Must provide transactionId")
                    transactionService.transfer(transactionId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
