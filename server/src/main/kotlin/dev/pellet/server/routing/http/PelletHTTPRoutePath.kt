package dev.pellet.server.routing.http

import dev.pellet.server.routing.RouteVariableDescriptor

public data class PelletHTTPRoutePath(
    internal val components: List<Component>
) {

    companion object {

        fun parse(rawPath: String): PelletHTTPRoutePath {
            return Builder()
                .addComponents(rawPath)
                .build()
        }

        internal fun parsePlainComponents(rawPath: String): List<Component.Plain> {
            return rawPath
                .split("/")
                .mapNotNull {
                    val trimmedString = it
                        .removePrefix("/")
                        .removeSuffix("/")
                    if (trimmedString.isEmpty()) {
                        return@mapNotNull null
                    }
                    Component.Plain(trimmedString)
                }
        }
    }

    val path: String
    init {
        val joinedComponents = components.joinToString("/") {
            when (it) {
                is Component.Plain -> it.string
                is Component.Variable -> {
                    if (it.visualType != null) {
                        "{${it.name}:${it.visualType}}"
                    } else {
                        "{${it.name}}"
                    }
                }
            }
        }
        path = "/$joinedComponents"
    }

    fun prefixedWith(routePath: PelletHTTPRoutePath): PelletHTTPRoutePath {
        return PelletHTTPRoutePath(
            components = routePath.components + this.components
        )
    }

    override fun toString(): String {
        return path
    }

    sealed class Component {

        data class Plain(val string: String) : Component()
        data class Variable(val name: String, val visualType: String?) : Component()
    }

    public class Builder {

        private var components = mutableListOf<Component>()

        fun addComponents(string: String): Builder {
            components += parsePlainComponents(string)
            return this
        }

        fun addVariable(variableName: String): Builder {
            components += Component.Variable(variableName, null)
            return this
        }

        fun addVariable(descriptor: RouteVariableDescriptor<*>): Builder {
            components += Component.Variable(descriptor.name, descriptor.visualType)
            return this
        }

        fun build(): PelletHTTPRoutePath {
            return PelletHTTPRoutePath(
                components
            )
        }
    }
}
