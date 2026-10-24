package com.example.tiotec.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiotec.model.experiencias
import com.example.tiotec.model.preguntas
import com.example.tiotec.model.respuestasValidas
import com.example.tiotec.viewmodel.PantallaEstado
import com.example.tiotec.viewmodel.TioTecViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EvaluacionScreen(vm: TioTecViewModel, estado: PantallaEstado, onGuardada: () -> Unit) {
    val f = estado.formulario
    val e = estado.errores
    val alcance = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 680.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (f.id == 0L) "Evaluación preventiva" else "Editar evaluación",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Usa un perfil y dispositivo ficticios. Responde cuatro preguntas y guarda un plan sencillo.")
            Campo("Nombre ficticio", f.nombre, e.nombre, Icons.Outlined.PersonOutline, vm::nombre)
            Campo("Dispositivo de prueba", f.dispositivo, e.dispositivo, Icons.Outlined.PhoneAndroid, vm::dispositivo)
            Campo("Sistema del dispositivo", f.sistema, e.sistema, Icons.Outlined.Devices, vm::sistema)

            Text("Experiencia con tecnología", style = MaterialTheme.typography.titleMedium)
            Opciones(experiencias, f.experiencia, vm::experiencia)
            ErrorCampo(e.experiencia)

            Text("Foto del dispositivo de prueba", style = MaterialTheme.typography.titleMedium)
            val bitmap = remember(f.foto) { if (f.foto.isBlank()) null else BitmapFactory.decodeFile(f.foto) }
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), "Foto del dispositivo", Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Fit)
            }
            SelectorFoto(vm, !estado.ocupado)
            ErrorCampo(e.foto)

            HorizontalDivider()
            Text("Hábitos preventivos", style = MaterialTheme.typography.titleLarge)
            preguntas.forEachIndexed { indice, pregunta ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pregunta.titulo, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(pregunta.texto)
                        Opciones(respuestasValidas, f.respuestas[indice]) { vm.respuesta(indice, it) }
                        ErrorCampo(e.respuestas[indice])
                    }
                }
            }
            HorizontalDivider()
            Text("Próxima revisión", style = MaterialTheme.typography.titleLarge)
            Text("Volver a evaluar en ${f.diasRevision} días")
            Slider(
                value = f.diasRevision.toFloat(),
                onValueChange = { vm.diasRevision(it.roundToInt()) },
                valueRange = 7f..90f,
                modifier = Modifier.fillMaxWidth()
            )
            ErrorCampo(e.diasRevision)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = f.ficticio, onCheckedChange = vm::ficticio)
                Text("Confirmo que estos datos son ficticios.")
            }
            ErrorCampo(e.ficticio)
            Button(
                onClick = { alcance.launch { if (vm.guardar()) onGuardada() } },
                enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth()
            ) { Text(if (f.id == 0L) "Guardar evaluación" else "Guardar cambios") }
            if (f.id != 0L) {
                TextButton(onClick = vm::nueva, modifier = Modifier.fillMaxWidth()) { Text("Cancelar edición") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Campo(
    titulo: String, valor: String, error: String?, icono: ImageVector, cambiar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor, onValueChange = cambiar, label = { Text(titulo) },
        leadingIcon = { Icon(icono, null) }, isError = error != null,
        supportingText = { if (error != null) Text(error) }, singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Opciones(opciones: List<String>, valor: String, cambiar: (String) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        opciones.forEach { opcion ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(selected = valor == opcion, onClick = { cambiar(opcion) })
                Text(opcion, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ErrorCampo(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall)
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaCampo() {
    MaterialTheme {
        Campo("Nombre ficticio", "", null, Icons.Outlined.PersonOutline) {}
    }
}
