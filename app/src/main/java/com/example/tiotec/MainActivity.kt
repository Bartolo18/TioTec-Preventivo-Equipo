package com.example.tiotec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.ui.SelectorFoto
import com.example.tiotec.viewmodel.TioTecViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: TioTecViewModel = viewModel()
                val estado by vm.estado.collectAsState()
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Prueba del selector de foto", style = MaterialTheme.typography.headlineSmall)
                    SelectorFoto(vm, true)
                    Text(if (estado.formulario.foto.isBlank()) "Todavía no hay una foto." else "Foto privada: ${estado.formulario.foto}")
                    if (estado.ocupado) Text("Procesando foto...")
                    if (estado.aviso.isNotBlank()) Text(estado.aviso)
                }
            }
        }
    }
}
