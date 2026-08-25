package com.hakayat.backend.render

import java.util.UUID

data class RenderJobCompletion(val jobId: UUID, val outputAssetId: UUID)