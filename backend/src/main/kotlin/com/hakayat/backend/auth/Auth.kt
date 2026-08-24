package com.hakayat.backend.auth

import io.ktor.server.application.ApplicationCall

interface Authenticator {
    suspend fun authenticate(call: ApplicationCall): Principal?
}

data class Principal(val subject: String, val roles: Set<String> = emptySet())

class NoopAuthenticator : Authenticator {
    override suspend fun authenticate(call: ApplicationCall): Principal? = null
}
