package ru.kostiagn.apitest.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.restassured.internal.mapping.Jackson2Mapper
import io.restassured.mapper.factory.Jackson2ObjectMapperFactory

val restObjectMapper: io.restassured.mapper.ObjectMapper by lazy {
    Jackson2Mapper(Jackson2ObjectMapperFactory { _, _ ->
        ObjectMapper().apply {
            registerModule(KotlinModule())
        }
    })
}

