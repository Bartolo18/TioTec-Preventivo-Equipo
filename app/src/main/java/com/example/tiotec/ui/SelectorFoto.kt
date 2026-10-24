package com.example.tiotec.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tiotec.viewmodel.TioTecViewModel

@Composable
fun SelectorFoto(vm: TioTecViewModel, habilitado: Boolean) {
    val context = LocalContext.current
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }
    var capturaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val uri = capturaPendiente?.let(Uri::parse)
        capturaPendiente = null
        vm.resultadoCamara(uri, exito)
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), vm::resultadoGaleria)

    fun abrirCamara() {
        vm.prepararCamara()?.let { uri ->
            capturaPendiente = uri.toString()
            try { camara.launch(uri) } catch (_: Exception) {
                capturaPendiente = null
                vm.errorCamara(uri)
            }
        }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitido ->
        permisoDenegado = !permitido
        if (permitido) abrirCamara()
        else vm.avisar("Permiso de cámara denegado. Puedes usar la galería o abrir Ajustes.", true)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                abrirCamara()
            else try { permiso.launch(Manifest.permission.CAMERA) }
            catch (_: Exception) { vm.avisar("No se pudo solicitar el permiso de cámara.", true) }
        }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Tomar foto")
        }
        OutlinedButton(onClick = {
            try { galeria.launch("image/*") }
            catch (_: Exception) { vm.avisar("No hay un selector de imágenes disponible.", true) }
        }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Elegir del dispositivo")
        }
        if (permisoDenegado) {
            Text("Si Android ya no solicita permiso, actívalo en Ajustes.")
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")))
                } catch (_: Exception) { vm.avisar("Abre Ajustes > Aplicaciones > TíoTec > Permisos.", true) }
            }) { Text("Abrir ajustes de permisos") }
        }
    }
}
