package com.linecorp.klever.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.linecorp.clova.extension.client.*
import com.linecorp.klever.model.User
import com.linecorp.klever.model.dao.ClovaClientDao
import com.linecorp.klever.model.dao.UserDao
import com.linecorp.klever.runtime.dsl.KleverRuntime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.net.URI
import javax.script.ScriptException

@Component
class DefaultHandler {
    private val logger: Logger = LoggerFactory.getLogger("Klever")
    private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    private val kleverRuntime: KleverRuntime = KleverRuntime()

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var clovaClientDao: ClovaClientDao

    private var clients: HashMap<Int, ClovaClient> = hashMapOf()

    fun doLogin(request: ServerRequest): Mono<ServerResponse> {
        return request.formData().flatMap { formData ->
            val name = formData["userName"]?.get(0)
            val password = formData["password"]?.get(0)
            if (password == "1234") {
                name?.let {
                    var user = userDao.getUser(name)
                    if (user == null) {
                        user = User(name = it)
                        userDao.insertUser(user)
                    }
                    ServerResponse.seeOther(URI.create("/${user.name}")).build()
                } ?: run {
                    ServerResponse.temporaryRedirect(URI.create("/login")).build()
                }
            } else {
                ServerResponse.temporaryRedirect(URI.create("/login")).build()
            }
        }
    }

    fun indexPage(request: ServerRequest): Mono<ServerResponse> = ServerResponse.ok().render("index")

    fun loginPage(request: ServerRequest): Mono<ServerResponse> = ServerResponse.ok().render("login")

    fun editorPage(request: ServerRequest): Mono<ServerResponse> {
        val username = request.pathVariable("username")
        val user = userDao.getUser(username)

        if (user == null) {
            return ServerResponse.temporaryRedirect(URI.create("/login")).build()
        }

        val clovaClient = clovaClientDao.getClovaClientByUserId(user.id)
        val model = ModelMap().apply {
            addAttribute("user", user)
            addAttribute(
                    "clientId",
                    clovaClient?.clientId ?: com.linecorp.klever.model.ClovaClient.NOT_EXIST
            )
            addAttribute("appId", clovaClient?.appId ?: "")
            addAttribute("code", clovaClient?.code ?: getDefaultDsl())
        }
        return ServerResponse.ok().render("editor", model)
    }

    fun buildDsl(request: ServerRequest): Mono<ServerResponse> =
            request.bodyToMono(String::class.java)
                    .flatMap { body ->
                        val clovaClient = objectMapper.readValue<com.linecorp.klever.model.ClovaClient>(body)
                        if (clovaClient.clientId == com.linecorp.klever.model.ClovaClient.NOT_EXIST) {
                            clovaClientDao.insertClovaClient(clovaClient)
                        } else {
                            clovaClientDao.updateClovaClient(clovaClient)
                        }
                        val dsl = generateClientDsl(clovaClient.code)
                        lateinit var response: Map<String, Any>
                        try {
                            val client = kleverRuntime.eval(dsl) as ClovaClient
                            clients[clovaClient.clientId] = client
                            response = mapOf("status" to 200, "clientId" to clovaClient.clientId)
                            return@flatMap ServerResponse.ok().body(BodyInserters.fromObject(response))
                        } catch (e: ScriptException) {
                            response = mapOf("status" to 400, "errorMessage" to (e.message ?: ""))
                            return@flatMap ServerResponse.badRequest().body(BodyInserters.fromObject(response))
                        }
                    }

    fun dispatchRequest(request: ServerRequest): Mono<ServerResponse> {
        return request
                .bodyToMono(String::class.java)
                .flatMap { requestBody ->
                    val clientId = request.pathVariable("clientId").toInt()
                    if (clients[clientId] == null) {
                        logger.info("rebuild the client code for id $clientId")
                        val clovaClient = clovaClientDao.getClovaClientByClientId(clientId)
                        clovaClient?.let {
                            clients[clientId] = kleverRuntime.eval(generateClientDsl(clovaClient.code)) as ClovaClient
                        }
                    }
                    val client = clients[clientId]
                    if (client == null) {
                        throw IllegalArgumentException("Cannot find client")
                    }
                    logger.info("-> $requestBody\n")
                    val response = client.handleClovaRequest(requestBody, request.headers().asHttpHeaders())
                    logger.info("<- $response\n")
                    return@flatMap ServerResponse.ok().body(
                            BodyInserters.fromObject(response))
                }
    }

    private fun generateClientDsl(source: String): String =
        """
            import com.linecorp.clova.extension.client.*
            import com.linecorp.clova.extension.model.util.*
            import com.linecorp.clova.extension.converter.jackson.JacksonObjectMapper
            import com.linecorp.klever.runtime.dsl.*

            $source
        """
    private fun getDefaultDsl(): String = DEFAULT_DSL.trimMargin()

    companion object {
        private const val DEFAULT_DSL = """
        |klever("the.clova.extension.id") {
        |
        |  launchHandler { _, _ ->
        |    simpleResponse(message = "はい、エクステンションが起動しました")
        |  }

        |  intentHandler { request, session ->
        |    val value = request.intent.slots["number"]?.value
        |    simpleResponseWithReprompt(
        |    message = "数字は""" + "$" + """{value}です",
        |      repromptMessage = "もう一度話しますか"
        |    )
        |  }

        |  sessionEndedHandler { _, _ ->
        |    simpleResponse(message = "またね")
        |  }
        |}
        """
    }
}
