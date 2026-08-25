package com.hakayat.backend.render

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import java.util.UUID

class RenderApiValidationTest {
    @Test fun `validates UUID`() { assertEquals(UUID.randomUUID().toString().length, RenderApiValidation.uuid(UUID.randomUUID().toString(), "id").toString().length) }
    @Test fun `rejects invalid UUID`() { assertFailsWith<IllegalArgumentException> { RenderApiValidation.uuid("bad", "id") } }
    @Test fun `rejects blank idempotency key`() { assertFailsWith<IllegalArgumentException> { RenderApiValidation.idempotencyKey(" ") } }
}