package com.example.tiotec.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.data.model.*
import com.example.tiotec.viewmodel.SistemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SistemaScreen(vm: SistemaViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    val enlaces = LocalUriHandler.current
    var sistemasAbiertos by remember { mutableStateOf(false) }
    var versionesAbiertas by remember { mutableStateOf(false) }
    LaunchedEffect(e.sistema) { if (!e.consultado) vm.consultar() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("¿Tu sistema todavía tiene soporte?", style = MaterialTheme.typography.headlineSmall)
        Text("Elige el sistema y la versión de tu equipo para consultar su estado de soporte.")
        ExposedDropdownMenuBox(expanded = sistemasAbiertos,
            onExpandedChange = { if (!e.ocupado) sistemasAbiertos = it }) {
            OutlinedTextField(value = e.sistema.etiqueta, onValueChange = {}, readOnly = true,
                enabled = !e.ocupado, label = { Text("Sistema operativo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sistemasAbiertos) },
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("selector_sistema"))
            ExposedDropdownMenu(expanded = sistemasAbiertos, onDismissRequest = { sistemasAbiertos = false }) {
                SistemaConsulta.entries.forEach { sistema ->
                    DropdownMenuItem(text = { Text(sistema.etiqueta) }, onClick = {
                        sistemasAbiertos = false; versionesAbiertas = false; vm.cambiarSistema(sistema)
                    })
                }
            }
        }
        val seleccionada = e.versiones.find { it.name == e.versionSeleccionada }
        ExposedDropdownMenuBox(expanded = versionesAbiertas,
            onExpandedChange = { if (!e.ocupado && e.versiones.isNotEmpty()) versionesAbiertas = it }) {
            OutlinedTextField(value = seleccionada?.titulo() ?: "", onValueChange = {}, readOnly = true,
                enabled = !e.ocupado && e.versiones.isNotEmpty(), label = { Text("Versión") },
                placeholder = { Text("Selecciona una versión") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(versionesAbiertas) },
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("selector_version"))
            ExposedDropdownMenu(expanded = versionesAbiertas, onDismissRequest = { versionesAbiertas = false },
                modifier = Modifier.heightIn(max = 320.dp)) {
                e.versiones.forEach { version ->
                    DropdownMenuItem(text = { Text(version.titulo()) }, onClick = {
                        versionesAbiertas = false; vm.seleccionarVersion(version.name)
                    })
                }
            }
        }
        Text(if (e.sistema == SistemaConsulta.ANDROID)
            "Encuentra la versión en Ajustes > Acerca del teléfono > Versión de Android."
            else "Encuentra la versión y edición en Configuración > Sistema > Acerca de.",
            style = MaterialTheme.typography.bodySmall)
        Button(onClick = vm::verSoporte, enabled = !e.ocupado && e.versionSeleccionada.isNotBlank(),
            modifier = Modifier.fillMaxWidth()) { Text("Consultar soporte") }
        TextButton(onClick = vm::consultar, enabled = !e.ocupado) { Text("Actualizar lista de versiones") }
        if (e.ocupado) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Obteniendo versiones…")
        }
        if (e.mensaje.isNotBlank()) Text(e.mensaje, color = MaterialTheme.colorScheme.error)
        e.resultado?.let { version ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor =
                if (version.isEol == true) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${e.sistema.etiqueta} · ${version.titulo()}", style = MaterialTheme.typography.titleMedium)
                    Text(version.estadoSoporte(), style = MaterialTheme.typography.titleLarge)
                    Text("Lanzamiento: ${fechaSoporte(version.releaseDate)}")
                    Text("Fin de soporte: ${fechaSoporte(version.eolFrom)}")
                    Text(version.orientacion())
                }
            }
        }
        HorizontalDivider()
        Text("Fuente: endoflife.date", style = MaterialTheme.typography.labelLarge)
        Text("Información general de la versión seleccionada. En Android, las actualizaciones de cada modelo dependen de su fabricante. En Windows, elige también la edición correcta.",
            style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { runCatching { enlaces.openUri(e.sistema.enlace) } }) { Text("Ver detalle de la fuente") }
    }
}
