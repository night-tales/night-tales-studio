package com.hakayat.backend.render

import java.io.File

class RenderOutputValidator {
    fun validate(file: File) {
        require(file.exists() && file.isFile) { "render output does not exist" }
        require(file.length() > 0L) { "render output is empty" }
    }
}