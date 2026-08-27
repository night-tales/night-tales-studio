package com.hakayat.backend.ai

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class UsageLedgerTest {
    @Test
    fun calculates_input_output_and_total_cost() {
        val record = UsageRecord(
            userId = "u1",
            taskId = "t1",
            provider = AiProvider.OPENAI,
            model = "test",
            providerRequestId = "r1",
            inputTokens = 2_000_000,
            outputTokens = 500_000,
            latencyMs = 100
        ).withCost(
            ProviderPricing(
                inputPerMillion = BigDecimal("2.00"),
                outputPerMillion = BigDecimal("8.00")
            )
        )

        assertEquals(BigDecimal("4.00"), record.inputCost)
        assertEquals(BigDecimal("4.00"), record.outputCost)
        assertEquals(BigDecimal("8.00"), record.totalCost)
    }
}
