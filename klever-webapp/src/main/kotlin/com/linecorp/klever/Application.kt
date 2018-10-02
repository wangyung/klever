package com.linecorp.klever

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KleverApplication

fun main(args: Array<String>) {
    runApplication<KleverApplication>(*args)
}
