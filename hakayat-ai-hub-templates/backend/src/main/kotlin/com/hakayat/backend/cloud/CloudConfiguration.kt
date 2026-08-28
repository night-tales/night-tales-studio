package com.hakayat.backend.cloud

/** Non-secret runtime configuration. Secrets must be injected by the deployment environment. */
data class CloudConfiguration(
    val googleCloudProjectId: String? = null,
    val cloudSqlInstanceConnectionName: String? = null,
    val firebaseProjectId: String? = null,
)
