package dev.pellet.demo

import dev.pellet.logging.pelletLogger
import dev.pellet.server.PelletBuilder.httpRouter
import dev.pellet.server.PelletBuilder.pelletServer
import dev.pellet.server.PelletConnector
import dev.pellet.server.responder.http.PelletHTTPRouteContext
import dev.pellet.server.routing.http.HTTPRouteResponse
import dev.pellet.server.routing.stringDescriptor
import dev.pellet.server.routing.uuidDescriptor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object Demo

private val logger = pelletLogger<Demo>()
private val idDescriptor = uuidDescriptor("id")
private val suffixDescriptor = stringDescriptor("suffix")

fun main() = runBlocking {
    val sharedRouter = httpRouter {
        get("/", ::handleRequest)
        path("/v1") {
            get("/hello", ::handleResponseBody)
            post("/echo", ::handleEchoRequest)
            get("/error", ::handleForceError)
            path(idDescriptor) {
                get("/hello", ::handleNamedResponseBody)
            }
        }
    }
    val pellet = pelletServer {
        logRequests = false
        httpConnector {
            endpoint = PelletConnector.Endpoint(
                hostname = "localhost",
                port = 8082
            )
            router = sharedRouter
        }
    }
    pellet.start()
}

private fun simpleMain() = runBlocking {
    val pellet = pelletServer {
        httpConnector {
            endpoint = PelletConnector.Endpoint(
                hostname = "localhost",
                port = 8082
            )
            router {
                get("/v1/hello") {
                    HTTPRouteResponse.Builder()
                        .noContent()
                        .header("X-Hello", "World")
                        .build()
                }
            }
        }
    }
    pellet.start()
}

private fun handleRequest(
    context: PelletHTTPRouteContext
): HTTPRouteResponse {
    logger.debug { "got request: ${context.rawMessage}" }

    return HTTPRouteResponse.Builder()
        .noContent()
        .header("X-Hello", "World")
        .build()
}

@kotlinx.serialization.Serializable
private data class RequestBody(
    val message: String
)

@kotlinx.serialization.Serializable
private data class ResponseBody(
    val message: String
)

private fun handleEchoRequest(
    context: PelletHTTPRouteContext
): HTTPRouteResponse {
    logger.debug { "got echo POST request: ${context.rawMessage}" }

    val requestBody = context.decodeRequestBody<RequestBody>().getOrElse {
        logger.debug(it) { "failed to decode json body" }

        return HTTPRouteResponse.Builder()
            .badRequest()
            .build()
    }
    val responseBody = ResponseBody(
        message = requestBody.message
    )
    return HTTPRouteResponse.Builder()
        .jsonEntity(Json, responseBody)
        .build()
}

private fun handleForceError(
    context: PelletHTTPRouteContext
): HTTPRouteResponse {
    throw RuntimeException("intentional error")
}

private fun handleResponseBody(
    context: PelletHTTPRouteContext
): HTTPRouteResponse {
    val responseBody = ResponseBody(message = "hello, world 🌎")
    return HTTPRouteResponse.Builder()
        .statusCode(200)
        .jsonEntity(Json, responseBody)
        .header("X-Hello", "World")
        .build()
}

private fun handleNamedResponseBody(
    context: PelletHTTPRouteContext
): HTTPRouteResponse {
    val id = context.pathParameter(idDescriptor).getOrThrow()
    val suffix = context.firstQueryParameter(suffixDescriptor).getOrNull()
        ?: "👋"
    val responseBody = ResponseBody(message = "hello $id $suffix")
    return HTTPRouteResponse.Builder()
        .statusCode(200)
        .jsonEntity(Json, responseBody)
        .build()
}
