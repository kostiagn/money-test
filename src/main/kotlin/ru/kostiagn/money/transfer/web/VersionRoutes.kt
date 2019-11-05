package ru.kostiagn.money.transfer.web

import com.google.inject.Inject
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

class VersionRoutes @Inject constructor(
    application: Application
) {
    init {
        application.routing {
            route("/version") {
                get("") {
                    call.respond("0.0.1")
                }
            }
        }
    }
}
