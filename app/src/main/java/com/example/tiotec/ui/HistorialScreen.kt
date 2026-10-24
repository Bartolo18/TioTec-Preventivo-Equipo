package com.example.tiotec.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiotec.data.Revision
import com.example.tiotec.model.preguntas
import com.example.tiotec.viewmodel.PantallaEstado
import com.example.tiotec.viewmodel.TioTecViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(
    vm: TioTecViewModel, estado: PantallaEstado, ancho: WindowWidthSizeClass,
    onNueva: () -> Unit, onEditar: (Revision) -> Unit
) {
    val elegida = estado.revisiones.firstOrNull { it.id == estado.seleccionada }
    var eliminar by remember { mutableStateOf<Revision?>(null) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        if (ancho != WindowWidthSizeClass.Compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(0.42f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                    ListaRevisiones(estado, vm::seleccionar, onNueva)
                }
                Column(Modifier.weight(0.58f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                    if (elegida != null) DetalleRevision(elegida, vm, onEditar) { eliminar = elegida }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ListaRevisiones(estado, vm::seleccionar, onNueva) { revision ->
                    HorizontalDivider()
                    DetalleRevision(revision, vm, onEditar) { eliminar = revision }
                }
            }
        }
    }

    eliminar?.let { revision ->
        AlertDialog(
            onDismissRequest = { eliminar = null },
            title = { Text("Eliminar evaluación") },
            text = { Text("¿Eliminar la revisión de ${revision.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { vm.eliminar(revision); eliminar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { eliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ListaRevisiones(
    estado: PantallaEstado, seleccionar: (Long) -> Unit, onNueva: () -> Unit,
    detalleSeleccionado: @Composable ((Revision) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Evaluaciones guardadas", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Button(onClick = onNueva, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Nueva evaluación")
        }
        if (estado.revisiones.isEmpty()) Text("Todavía no hay evaluaciones. Completa el formulario para comenzar.")
        estado.revisiones.forEach { revision ->
            val seleccionada = revision.id == estado.seleccionada
            Card(
                colors = CardDefaults.cardColors(containerColor = if (seleccionada)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().clickable { seleccionar(revision.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(revision.nombre, fontWeight = FontWeight.SemiBold)
                    Text(revision.dispositivo)
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(revision.fecha)),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (seleccionada) detalleSeleccionado?.invoke(revision)
        }
    }
}

@Composable
private fun DetalleRevision(
    revision: Revision, vm: TioTecViewModel,
    onEditar: (Revision) -> Unit, onEliminar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Resultado y plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${revision.nombre} · ${revision.dispositivo} · ${revision.sistema}")
        val respuestas = revision.respuestas
        val afirmativas = respuestas.count { it == "Sí" }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("$afirmativas de 4 prácticas confirmadas", style = MaterialTheme.typography.titleMedium)
                Text("Orientación para el caso ficticio; no es un diagnóstico de seguridad.")
                val proxima = Calendar.getInstance().apply {
                    timeInMillis = revision.fecha
                    add(Calendar.DAY_OF_MONTH, revision.diasRevision)
                }.time
                Text("Próxima revisión sugerida (${revision.diasRevision} días): " +
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(proxima))
            }
        }
        preguntas.forEachIndexed { i, pregunta ->
            Text("${pregunta.titulo}: ${respuestas[i]}")
        }
        HorizontalDivider()
        Text("Acciones priorizadas", style = MaterialTheme.typography.titleLarge)
        val pendientes = (0..3).filter { respuestas[it] == "No" } +
            (0..3).filter { respuestas[it] == "No sé" }
        var soloSinCompletar by rememberSaveable(revision.id) { mutableStateOf(false) }
        if (pendientes.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Solo acciones sin completar", Modifier.weight(1f))
                Switch(checked = soloSinCompletar, onCheckedChange = { soloSinCompletar = it })
            }
        }
        if (pendientes.isEmpty()) Text("No hay acciones pendientes según estas respuestas.")
        val visibles = pendientes.withIndex().filter { (_, i) ->
            !soloSinCompletar || revision.accionesHechas and (1 shl i) == 0
        }
        if (pendientes.isNotEmpty() && visibles.isEmpty()) {
            Text("Todas las acciones están completas. Desactiva el filtro para verlas.")
        }
        visibles.forEach { (posicion, i) ->
            val hecha = revision.accionesHechas and (1 shl i) != 0
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hecha, onCheckedChange = { vm.marcarAccion(revision, i, it) })
                    Column {
                        Text("${posicion + 1}. ${preguntas[i].accion}", fontWeight = FontWeight.SemiBold)
                        Text(if (respuestas[i] == "No") "Prioridad: revisar primero" else "Prioridad: averiguar",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        HorizontalDivider()
        Text("Consejos breves", style = MaterialTheme.typography.titleLarge)
        preguntas.forEach { pregunta ->
            Text("${pregunta.titulo}: ${pregunta.consejo}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEditar(revision) }) {
                Icon(Icons.Outlined.Edit, null); Spacer(Modifier.width(4.dp)); Text("Editar")
            }
            OutlinedButton(onClick = onEliminar) {
                Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(4.dp)); Text("Eliminar")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
