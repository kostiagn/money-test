package ru.kostiagn.apitest.rest

class EndPoints {
    class Transaction{
        companion object{
            const val GET = "/transaction/{transactionId}"
            const val POST = "/transaction"
            const val PUT = "/transaction/{transactionId}"
        }
    }
}