package com.hakayat.backend.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import io.ktor.server.auth.Principal

data class FirebaseUserPrincipal(
    val uid: String,
    val claims: Map<String, Any>
) : Principal

object FirebaseAuthConfig {
    fun initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp()
                ?: error("Firebase Admin initialization failed. Configure Application Default Credentials.")
        }
    }

    fun verifyIdToken(idToken: String): FirebaseUserPrincipal {
        require(idToken.isNotBlank()) { "Missing Firebase ID token" }
        val decoded = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return FirebaseUserPrincipal(decoded.uid, decoded.claims)
    }
}
