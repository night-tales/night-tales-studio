package com.hakayat.backend.cloud

/** Authenticated application identity derived from a verified Firebase ID token. */
data class CloudIdentity(
    val uid: String,
)
