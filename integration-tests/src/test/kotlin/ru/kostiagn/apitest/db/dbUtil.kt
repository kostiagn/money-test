package ru.kostiagn.apitest.db

import java.sql.ResultSet
import kotlin.reflect.KClass


fun selectFromDataClass(rs: ResultSet, objectClass: KClass<*>): String {
    val objectFields = objectClass.constructors.first().parameters.map {
        it.name?.toUpperCase() to it.name
    }.toMap()

    val sb = StringBuilder()
    sb.append("{")
    val metaData = rs.metaData
    for (i in 1..metaData.columnCount) {

        val value: Any? = when (metaData.getColumnTypeName(i)) {
            "timestamp" -> rs.getTimestamp(i).time
            else -> rs.getString(i)
        }
        sb.append(""""${objectFields[metaData.getColumnLabel(i).toUpperCase()]}":"$value",""")
    }
    return sb.removeLastComma().append("}").toString()
}


fun StringBuilder.removeLastComma() = this.apply {
    if (last() == ',') {
        setLength(length - 1)
    }
}