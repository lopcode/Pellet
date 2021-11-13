package dev.skye.pellet.codec.http

import dev.skye.pellet.CloseReason
import dev.skye.pellet.PelletClient
import dev.skye.pellet.PelletContext
import dev.skye.pellet.PelletResponder
import dev.skye.pellet.codec.CodecHandler

internal class HTTPRequestHandler(
    private val client: PelletClient,
    private val action: suspend (PelletContext, PelletResponder) -> Unit
) : CodecHandler<HTTPRequestMessage> {

    override suspend fun handle(request: HTTPRequestMessage) {
        val context = PelletContext(request, client)
        val responder = PelletResponder(client)
        action(context, responder)

        val connectionHeader = request.headers.getSingleOrNull(HTTPHeaderConstants.connection)
        handleConnectionHeader(connectionHeader)
    }

    private fun handleConnectionHeader(connectionHeader: HTTPHeader?) {
        if (connectionHeader == null) {
            // keep alive by default
            return
        }

        if (connectionHeader.rawValue.equals(HTTPHeaderConstants.keepAlive, ignoreCase = true)) {
            return
        }

        if (connectionHeader.rawValue.equals(HTTPHeaderConstants.close, ignoreCase = true)) {
            client.close(CloseReason.ServerInitiated)
        }
    }
}
