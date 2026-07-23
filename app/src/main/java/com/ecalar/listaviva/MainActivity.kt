package com.ecalar.listaviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import com.ecalar.listaviva.ui.navigation.ListavivaNavigation
import com.ecalar.listaviva.ui.theme.ListaVivaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by preferencesRepository.isModoOscuro().collectAsState(initial = false)
            val colorPrincipal by preferencesRepository.getColorPrincipal().collectAsState(initial = "Verde")

            val tamanoTexto by preferencesRepository.getTamañoTexto().collectAsState(initial = "Normal")

            ListaVivaTheme(
                darkTheme = isDarkMode,
                colorPrincipal = colorPrincipal,
                tamanoTexto = tamanoTexto // <-- Pasamos la nueva variable aquí al motor visual
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListavivaNavigation()
                }
            }
        }
    }
}