package com.hakayat.backend.ai

data class ProviderExecutionPolicy(
    val primary: AiProvider,
    val fallback: AiProvider? = null,
)

fun ProviderExecutionPolicy.providersInOrder(): List<AiProvider> =
    listOfNotNull(primary, fallback).distinct()
