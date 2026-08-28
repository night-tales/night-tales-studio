package com.hakayat.backend.ai

import java.math.BigDecimal

data class ProviderPricing(val inputPerMillion: BigDecimal, val outputPerMillion: BigDecimal)

data class UsageRecord(
    val userId: String,
    val taskId: String,
    val provider: AiProvider,
    val model: String,
    val providerRequestId: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val latencyMs: Long,
    val inputCost: BigDecimal = BigDecimal.ZERO,
    val outputCost: BigDecimal = BigDecimal.ZERO,
    val totalCost: BigDecimal = BigDecimal.ZERO,
) {
    fun withCost(pricing: ProviderPricing): UsageRecord {
        val input = pricing.inputPerMillion.multiply(BigDecimal(inputTokens)).divide(BigDecimal("1000000"))
        val output = pricing.outputPerMillion.multiply(BigDecimal(outputTokens)).divide(BigDecimal("1000000"))
        return copy(inputCost = input, outputCost = output, totalCost = input.add(output))
    }
}
