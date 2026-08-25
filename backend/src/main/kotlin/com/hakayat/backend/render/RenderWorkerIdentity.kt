package com.hakayat.backend.render

data class RenderWorkerIdentity(val id: String) { init { require(id.isNotBlank()) } }