package com.ecalar.listaviva.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecalar.listaviva.ui.theme.neoBrutalism
import kotlinx.coroutines.launch

data class OnboardingPage(val title: String, val description: String, val icon: ImageVector)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            title = "Tu Despensa al Día",
            description = "Controla todo lo que tienes en casa de forma visual. Olvídate de duplicar compras o de no saber qué falta.",
            icon = Icons.Default.Inventory
        ),
        OnboardingPage(
            title = "Comparte sin Límites",
            description = "Perfecto para familias, pisos de estudiantes o grupos de amigos que organizan una comida o barbacoa conjunta.",
            icon = Icons.Default.Sync
        ),
        OnboardingPage(
            title = "Cero Fricción",
            description = "Escanea códigos de barras, sincroniza al instante y colabora en tiempo real con un simple código de invitación.",
            icon = Icons.Default.NoAccounts
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = MaterialTheme.colorScheme.background
    val actionColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Scaffold(containerColor = backgroundColor) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { position ->
                OnboardingNeoCard(page = pages[position])
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) actionColor else surfaceColor)
                            .border(2.dp, onSurfaceColor, CircleShape)
                            .size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("Atrás", color = onSurfaceColor, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            viewModel.completeOnboarding()
                            onFinish()
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = onPrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (pagerState.currentPage == pages.size - 1) "Empezar" else "Siguiente", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun OnboardingNeoCard(page: OnboardingPage) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 8.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
                modifier = Modifier.neoBrutalism(cornerRadius = 50.dp, borderWidth = 2.dp, shadowOffset = 4.dp, borderColor = onSurfaceColor, shadowColor = onSurfaceColor).padding(bottom = 4.dp, end = 4.dp)
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp).size(64.dp),
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = onSurfaceColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}