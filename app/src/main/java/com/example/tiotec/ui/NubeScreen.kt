package com.example.tiotec.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.model.*
import com.example.tiotec.viewmodel.NubeViewModel
import kotlin.math.roundToInt

@Composable
fun NubeScreen(vm: NubeViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var eliminar by remember { mutableStateOf<RevisionNube?>(null) }
    LaunchedEffect(Unit) { if (!e.consultado) vm.consultar() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Evaluaciones en nube", style = MaterialTheme.typography.headlineSmall)
        Text("Gestiona evaluaciones ficticias en el servidor. El historial y las fotos de este dispositivo se guardan por separado.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::nueva, enabled = !e.ocupado) { Text("Nueva en nube") }
            OutlinedButton(onClick = vm::consultar, enabled = !e.ocupado) { Text("Actualizar") }
        }
        if (e.ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (e.mensaje.isNotBlank()) Text(e.mensaje, color = if (e.fallo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        if (e.mostrarFormulario) Card {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (e.editando == null) "Nueva evaluación ficticia" else "Editar #${e.editando}", style = MaterialTheme.typography.titleLarge)
                val d = e.datos
                OutlinedTextField(d.nombre, { vm.cambiar(d.copy(nombre = it)) }, label = { Text("Nombre ficticio") }, enabled = !e.ocupado, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(d.dispositivo, { vm.cambiar(d.copy(dispositivo = it)) }, label = { Text("Dispositivo de prueba") }, enabled = !e.ocupado, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(d.sistema, { vm.cambiar(d.copy(sistema = it)) }, label = { Text("Sistema") }, enabled = !e.ocupado, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Experiencia")
                OpcionesNube(experiencias, d.experiencia, !e.ocupado) { vm.cambiar(d.copy(experiencia = it)) }
                preguntas.forEachIndexed { i, p ->
                    Text(p.texto, fontWeight = FontWeight.SemiBold)
                    OpcionesNube(respuestasValidas, d.respuestas[i], !e.ocupado) { r ->
                        vm.cambiar(d.copy(respuestas = d.respuestas.toMutableList().apply { set(i, r) },
                            accionesHechas = d.accionesHechas and (1 shl i).inv()))
                    }
                    if (e.editando != null && d.respuestas[i] != "Sí") Row {
                        Checkbox(d.accionesHechas and (1 shl i) != 0, { hecha ->
                            vm.cambiar(d.copy(accionesHechas = if (hecha) d.accionesHechas or (1 shl i) else d.accionesHechas and (1 shl i).inv()))
                        }, enabled = !e.ocupado)
                        Text("Acción completada", Modifier.padding(top = 12.dp))
                    }
                }
                Text("Próxima revisión: ${d.diasRevision} días")
                Slider(d.diasRevision.toFloat(), { vm.cambiar(d.copy(diasRevision = it.roundToInt())) }, enabled = !e.ocupado, valueRange = 7f..90f)
                e.errores.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                Row {
                    Button(onClick = vm::guardar, enabled = !e.ocupado) { Text("Guardar en servidor") }
                    TextButton(onClick = vm::cancelar, enabled = !e.ocupado) { Text("Cancelar") }
                }
            }
        }
        if (e.consultado && e.revisiones.isEmpty()) Text("Todavía no hay evaluaciones en el servidor.")
        e.revisiones.forEach { r -> Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("#${r.id} · ${r.nombre}", style = MaterialTheme.typography.titleMedium)
                Text("${r.dispositivo} · ${r.sistema}")
                Text("${r.respuestas.count { it == "Sí" }} de 4 prácticas confirmadas")
                Text("Revisar nuevamente en ${r.diasRevision} días")
                preguntas.forEachIndexed { i, p -> Text("${p.titulo}: ${r.respuestas.getOrElse(i) { "Sin respuesta" }}") }
                Row {
                    OutlinedButton(onClick = { vm.editar(r.id) }, enabled = !e.ocupado) { Text("Editar") }
                    TextButton(onClick = { eliminar = r }, enabled = !e.ocupado) { Text("Eliminar") }
                }
            }
        } }
    }
    eliminar?.let { r -> AlertDialog(onDismissRequest = { eliminar = null },
        title = { Text("Eliminar en servidor") }, text = { Text("¿Eliminar la evaluación ficticia de ${r.nombre}?") },
        confirmButton = { TextButton(onClick = { vm.eliminar(r.id); eliminar = null }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { eliminar = null }) { Text("Cancelar") } }) }
}
@Composable
private fun OpcionesNube(opciones: List<String>, valor: String, habilitado: Boolean, cambiar: (String) -> Unit) {
    Row(Modifier.fillMaxWidth()) { opciones.forEach { o -> Column(Modifier.weight(1f)) {
        RadioButton(valor == o, { cambiar(o) }, enabled = habilitado); Text(o)
    } } }
}

