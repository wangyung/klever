package com.linecorp.klever.runtime.dsl

import com.linecorp.clova.extension.client.ClovaClient
import com.linecorp.clova.extension.client.clovaClient
import com.linecorp.clova.extension.converter.jackson.JacksonObjectMapper

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Dsl

fun klever(appId: String, block: (@Dsl ClovaClient).() -> Unit): ClovaClient =
    clovaClient(appId) {
        objectMapper = JacksonObjectMapper()
        block()
    }
