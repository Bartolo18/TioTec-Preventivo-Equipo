package com.example.tiotec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiotec.ui.EvaluacionScreen
import com.example.tiotec.viewmodel.TioTecViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: TioTecViewModel = viewModel()
                val estado by vm.estado.collectAsState()
                Column(Modifier.fillMaxSize()) {
                    if (estado.ocupado) Text("Procesando evaluación o foto...", Modifier.padding(12.dp))
                    if (estado.aviso.isNotBlank()) {
                        Text(
                            estado.aviso,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            color = if (estado.avisoError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        EvaluacionScreen(vm, estado, onGuardada = {})
                    }
                }
            }
        }
    }
}
