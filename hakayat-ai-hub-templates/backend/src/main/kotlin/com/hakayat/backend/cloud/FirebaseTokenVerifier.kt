package com.hakayat.backend.cloud

interface FirebaseTokenVerifier {
    suspend fun verify(idToken: String): CloudIdentity
}
