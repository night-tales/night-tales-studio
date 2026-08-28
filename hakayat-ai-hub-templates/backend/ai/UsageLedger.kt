package com.hakayat.backend.ai

import java.math.BigDecimal

data class UsageRecord(
    val userId: String,
    val taskId: String?,
    val provider: AiProvider,
    val model: String,
    val providerRequestId: String?,
    val inputTokens: Long,
    val outputTokens: Long,
    val latencyMs: Long?,
    val inputCost: BigDecimal?,
    val outputCost: BigDecimal?,
    val totalCost: BigDecimal?,
    val currency: String = "USD"
)

data class ProviderPricing(
    val inputPerMillion: BigDecimal,
    val outputPerMillion: BigDecimal,
    val currency: String = "USD"
) {
    fun cost(inputTokens: Long, outputTokens: Long): Pair<BigDecimal, BigDecimal> =
        inputPerMillion.multiply(BigDecimal.valueOf(inputTokens)).divide(BigDecimal.valueOf(1_000_000L)) to
            outputPerMillion.multiply(BigDecimal.valueOf(outputTokens)).divide(BigDecimal.valueOf(1_000_000L))
}

fun UsageRecord.withCost(pricing: ProviderPricing): UsageRecord {
    val (input, output) = pricing.cost(inputTokens, outputTokens)
    return copy(
        inputCost = input,
        outputCost = output,
        totalCost = input.add(output),
        currency = pricing.currency
    )
}
