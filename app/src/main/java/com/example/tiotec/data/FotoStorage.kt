package com.example.tiotec.data

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

class FotoStorage(private val context: Context) {
    private val capturas = File(context.filesDir, "capturas").apply { mkdirs() }

    fun nuevaCaptura(): String {
        val file = File.createTempFile("camara_", ".jpg", capturas)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
    }
}