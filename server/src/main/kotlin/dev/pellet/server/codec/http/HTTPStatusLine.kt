package dev.pellet.server.codec.http

data class HTTPStatusLine(
    val version: String,
    val statusCode: Int,
    val reasonPhrase: String
)
