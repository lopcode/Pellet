package dev.pellet.server.responder.http

import dev.pellet.server.PelletServerClient
import dev.pellet.server.codec.http.HTTPRequestMessage
import dev.pellet.server.codec.http.query.QueryParameters
import dev.pellet.server.routing.RouteVariableDescriptor

data class PelletHTTPRouteContext(
    val rawMessage: HTTPRequestMessage,
    internal val client: PelletServerClient,
    internal val pathValueMap: Map<String, String>,
    internal val queryParameters: QueryParameters
) {

    fun <T : Any> pathParameter(
        descriptor: RouteVariableDescriptor<T>
    ): Result<T> {
        val rawValue = pathValueMap[descriptor.name]
            ?: return Result.failure(
                RuntimeException("no such path parameter found")
            )
        // todo: wrap deserialiser errors?
        return runCatching {
            descriptor.deserialiser.invoke(rawValue)
        }
    }

    fun pathParameter(
        name: String
    ): Result<String> {
        val rawValue = pathValueMap[name]
            ?: return Result.failure(
                RuntimeException("no such path parameter found")
            )
        return Result.success(rawValue)
    }

    fun firstQueryParameter(
        name: String
    ): Result<String?> {
        val items = queryParameters.values[name]
        if (items.isNullOrEmpty()) {
            return Result.success(null)
        }
        return Result.success(
            items.firstOrNull()
        )
    }

    fun <T : Any> firstQueryParameter(
        descriptor: RouteVariableDescriptor<T>
    ): Result<T> {
        val rawValue = queryParameters[descriptor.name]
            ?.firstOrNull()
            ?: return Result.failure(
                RuntimeException("no such query parameter found")
            )
        // todo: wrap deserialiser errors?
        return runCatching {
            descriptor.deserialiser.invoke(rawValue)
        }
    }

    fun <T : Any> queryParameter(
        name: String
    ): Result<List<String?>> {
        val items = queryParameters.values[name]
            ?: return Result.failure(
                RuntimeException("no such query parameter found")
            )
        return Result.success(items)
    }

    fun <T : Any> queryParameter(
        descriptor: RouteVariableDescriptor<T>
    ): Result<List<T?>> {
        val rawValues = queryParameters[descriptor.name]
            ?: return Result.failure(
                RuntimeException("no such query parameter found")
            )
        // todo: wrap deserialiser errors?
        return runCatching {
            rawValues.map { rawValue ->
                if (rawValue == null) {
                    null
                } else {
                    descriptor.deserialiser.invoke(rawValue)
                }
            }
        }
    }
}
