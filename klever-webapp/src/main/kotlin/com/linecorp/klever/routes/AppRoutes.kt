package com.linecorp.klever.routes

import com.linecorp.klever.handler.DefaultHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class AppRoutes(private val handler: DefaultHandler) {

    @Bean
    fun default() = router {
        (accept(MediaType.APPLICATION_JSON) and "/").nest {
            GET("/", handler::indexPage)
            GET("/login", handler::loginPage)
            GET("/{username}", handler::editorPage)
            POST("/login", handler::doLogin)
            POST("/build", handler::buildDsl)
            POST("/{username}/{clientId}", handler::dispatchRequest)
        }
    }
}
