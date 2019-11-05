package ru.kostiagn.money.transfer.repository.impl

import org.jooq.Record
import org.jooq.ResultQuery
import java.sql.Timestamp
import java.time.LocalDateTime

inline fun <reified T : Any?> Record.intoObj(): T = into(T::class.java)
inline fun <R : Record, reified T : Any?> ResultQuery<R>.fetchIntoObj(): List<T> = fetchInto(T::class.java)
inline fun <R : Record, reified T : Any?> ResultQuery<R>.fetchOneIntoObj(): T = fetchOneInto(T::class.java)
fun now(): Timestamp = Timestamp.valueOf(LocalDateTime.now())

