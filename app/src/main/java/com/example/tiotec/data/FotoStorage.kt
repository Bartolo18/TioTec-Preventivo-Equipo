package com.example.tiotec.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

class FotoStorage(private val context: Context) {
    private val fotos = File(context.filesDir, "fotos").apply { mkdirs() }
    private val capturas = File(context.filesDir, "capturas").apply { mkdirs() }

    fun nuevaCaptura(): String {
        val file = File.createTempFile("camara_", ".jpg", capturas)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
    }

    fun importar(uri: Uri): String {
        val temporal = File.createTempFile("importar_", ".tmp", context.cacheDir)
        val destino = File(fotos, "${UUID.randomUUID()}.jpg")
        try {
            context.contentResolver.openInputStream(uri).use { entrada ->
                requireNotNull(entrada) { "No se puede leer la foto." }
                temporal.outputStream().use { salida ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val leidos = entrada.read(buffer)
                        if (leidos == -1) break
                        total += leidos
                        require(total <= 25L * 1024 * 1024) { "La foto supera 25 MB." }
                        salida.write(buffer, 0, leidos)
                    }
                }
            }
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(temporal)) { decoder, info, _ ->
                val escala = minOf(1f, 1600f / maxOf(info.size.width, info.size.height))
                decoder.setTargetSize(maxOf(1, (info.size.width * escala).toInt()),
                    maxOf(1, (info.size.height * escala).toInt()))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            try {
                destino.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) }
            } finally { bitmap.recycle() }
            return destino.absolutePath
        } catch (e: Exception) {
            destino.delete()
            throw e
        } finally { temporal.delete() }
    }

    fun existe(path: String): Boolean = path.isNotBlank() && File(path).let {
        it.parentFile?.canonicalFile == fotos.canonicalFile && it.isFile && it.length() > 0
    }
    fun borrarFoto(path: String) {
        if (path.isNotBlank()) runCatching {
            val file = File(path)
            if (file.parentFile?.canonicalFile == fotos.canonicalFile) file.delete()
        }
    }
    fun borrarCaptura(uri: String) {
        if (uri.isNotBlank()) runCatching { context.contentResolver.delete(Uri.parse(uri), null, null) }
    }
}
