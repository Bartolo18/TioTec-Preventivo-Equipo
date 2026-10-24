package com.example.tiotec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.ui.EvaluacionScreen
import com.example.tiotec.ui.HistorialScreen
import com.example.tiotec.viewmodel.TioTecViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: TioTecViewModel = viewModel()
                val estado by vm.estado.collectAsState()
                val ancho = calculateWindowSizeClass(this).widthSizeClass
                var mostrarHistorial by rememberSaveable { mutableStateOf(false) }
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { mostrarHistorial = false }) { Text("Evaluar") }
                        Button(onClick = { mostrarHistorial = true }) { Text("Historial") }
                    }
                    if (estado.ocupado) Text("Procesando evaluación o foto...", Modifier.padding(12.dp))
                    if (estado.aviso.isNotBlank()) Text(estado.aviso, Modifier.padding(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (mostrarHistorial) {
                            HistorialScreen(
                                vm = vm,
                                estado = estado,
                                ancho = ancho,
                                onNueva = { vm.nueva(); mostrarHistorial = false },
                                onEditar = { revision -> vm.editar(revision); mostrarHistorial = false }
                            )
                        } else {
                            EvaluacionScreen(vm, estado, onGuardada = { mostrarHistorial = true })
                        }
                    }
                }
            }
        }
    }
}
