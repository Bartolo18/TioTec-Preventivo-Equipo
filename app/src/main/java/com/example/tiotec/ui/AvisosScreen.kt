package com.example.tiotec.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.viewmodel.AvisosViewModel

@Composable
fun AvisosScreen(vm: AvisosViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    val enlaces = LocalUriHandler.current
    LaunchedEffect(Unit) { if (!e.consultado) vm.consultar() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Avisos de seguridad", style = MaterialTheme.typography.headlineSmall)
        Text("Avisos públicos de GitHub sobre bibliotecas Java y Android (Maven). Ayudan a revisar dependencias y mantenerlas actualizadas.")
        Text("Consulta el producto y la versión del aviso para saber si corresponde al caso de prueba.")
        Button(onClick = vm::consultar, enabled = !e.ocupado) { Text("Consultar avisos") }
        if (e.ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (e.mensaje.isNotBlank()) Text(e.mensaje, color = MaterialTheme.colorScheme.error)
        if (e.consultado && e.avisos.isEmpty()) Text("No se recibieron avisos.")
        e.avisos.forEach { aviso -> Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val gravedad = when (aviso.severity) { "critical" -> "Crítica"; "high" -> "Alta"; "moderate" -> "Media"; "low" -> "Baja"; else -> aviso.severity }
                Text("Gravedad: $gravedad", style = MaterialTheme.typography.labelLarge)
                Text(aviso.summary, style = MaterialTheme.typography.titleMedium)
                Text("${aviso.ghsa_id} · ${aviso.published_at.take(10)}")
                if (aviso.html_url.startsWith("https://github.com/advisories/"))
                    TextButton(onClick = { runCatching { enlaces.openUri(aviso.html_url) } }) { Text("Ver aviso original") }
            }
        } }
    }
}

