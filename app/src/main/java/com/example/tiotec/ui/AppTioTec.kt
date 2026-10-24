package com.example.tiotec.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tiotec.viewmodel.TioTecViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FORMULARIO = "evaluacion"
private const val HISTORIAL = "historial"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTioTec(vm: TioTecViewModel, ancho: WindowWidthSizeClass) {
    val estado by vm.estado.collectAsState()
    val nav = rememberNavController()
    val destino = nav.currentBackStackEntryAsState().value?.destination?.route ?: FORMULARIO
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarAyuda by remember { mutableStateOf(false) }
    LaunchedEffect(estado.avisoId) {
        if (estado.aviso.isNotBlank()) {
            delay(3500)
            vm.limpiarAviso(estado.avisoId)
        }
    }
    fun ir(ruta: String) {
        nav.navigate(ruta) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("TíoTec Preventivo", Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Evaluar") },
                    selected = destino == FORMULARIO,
                    onClick = { ir(FORMULARIO); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Historial") },
                    selected = destino == HISTORIAL,
                    onClick = { ir(HISTORIAL); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.History, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cómo usar") },
                    selected = false,
                    onClick = { mostrarAyuda = true; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.Info, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(when {
                    destino == HISTORIAL -> "Historial y plan"
                    estado.formulario.id != 0L -> "Editar evaluación"
                    else -> "Nueva evaluación"
                }) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú lateral")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destino == FORMULARIO,
                    onClick = { ir(FORMULARIO) },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null) }, label = { Text("Evaluar") }
                )
                NavigationBarItem(
                    selected = destino == HISTORIAL,
                    onClick = { ir(HISTORIAL) },
                    icon = { Icon(Icons.Outlined.History, null) }, label = { Text("Historial") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (estado.ocupado) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Procesando evaluación o foto...")
                }
            }
            AnimatedVisibility(
                visible = estado.aviso.isNotBlank(),
                enter = fadeIn() + slideInVertically { -it }, exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (estado.avisoError) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(estado.aviso, Modifier.padding(12.dp),
                        color = if (estado.avisoError) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            NavHost(navController = nav, startDestination = FORMULARIO, modifier = Modifier.weight(1f)) {
                composable(FORMULARIO) {
                    EvaluacionScreen(vm, estado) { ir(HISTORIAL) }
                }
                composable(HISTORIAL) {
                    HistorialScreen(vm, estado, ancho, onNueva = { vm.nueva(); ir(FORMULARIO) },
                        onEditar = { revision -> vm.editar(revision); ir(FORMULARIO) })
                }
            }
        }
    }
    }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text("Cómo usar TíoTec") },
            text = { Text("Usa datos ficticios. En Evaluar, responde las preguntas, elige el plazo y agrega una foto. Guarda para ver el resultado y las acciones en Historial. Allí también puedes editar o eliminar la evaluación.") },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) { Text("Entendido") }
            }
        )
    }
}
