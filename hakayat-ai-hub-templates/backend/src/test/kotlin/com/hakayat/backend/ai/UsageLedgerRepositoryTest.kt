package com.hakayat.backend.ai

import io.ktor.client.*
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class UsageLedgerRepositoryTest {
    @Test
    fun normalized_record_can_be_costed_before_persistence() {
        val record = UsageRecord(
            userId = "u1",
            taskId = "00000000-0000-0000-0000-000000000001",
            provider = AiProvider.OPENAI,
            model = "test",
            providerRequestId = "resp-1",
            inputTokens = 100,
            outputTokens = 50,
            latencyMs = 120
        ).withCost(
            ProviderPricing(BigDecimal("1.00"), BigDecimal("2.00"))
        )

        assertEquals(BigDecimal("0.0001"), record.inputCost)
        assertEquals(BigDecimal("0.0001"), record.outputCost)
        assertEquals(BigDecimal("0.0002"), record.totalCost)
    }
}
