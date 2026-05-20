package com.shadowcore.app.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.ui.components.GlassCard
import com.shadowcore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShadowCore Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Purple20.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                // Title
                Icon(Icons.Rounded.Diamond, null, Modifier.size(64.dp), tint = Purple60)
                Text("Unlock the Full Power", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("One-Time Purchase — Lifetime Access", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                Spacer(Modifier.height(8.dp))

                // Features
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    FeatureItem(Icons.Rounded.AllInclusive, "Unlimited Virtual Machines", "No more 2 VM limit")
                    Spacer(Modifier.height(12.dp))
                    FeatureItem(Icons.Rounded.PhoneAndroid, "All Android Versions", "Android 13 Tiramisu & 15 Vanilla Ice Cream")
                    Spacer(Modifier.height(12.dp))
                    FeatureItem(Icons.Rounded.Backup, "Export & Backup", "Save VMs as encrypted files")
                    Spacer(Modifier.height(12.dp))
                    FeatureItem(Icons.Rounded.SdCard, "Custom ROMs", "Load custom system images")
                    Spacer(Modifier.height(12.dp))
                    FeatureItem(Icons.Rounded.SupportAgent, "Priority Support", "Direct support channel")
                }

                Spacer(Modifier.height(8.dp))

                // Purchase button
                if (state.isPremium) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VmRunning.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = VmRunning)
                            Spacer(Modifier.width(8.dp))
                            Text("Premium Active", fontWeight = FontWeight.Bold, color = VmRunning)
                        }
                    }
                } else {
                    Button(
                        onClick = viewModel::purchase,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.isPurchasing,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple60),
                    ) {
                        Text(
                            text = state.price?.let { "Purchase for $it" } ?: "Purchase Premium",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    TextButton(onClick = viewModel::restorePurchases) {
                        Text("Restore Purchase")
                    }
                }

                // No ads badge
                Text(
                    text = "🚫 Zero Ads — No tracking, no banners, no interruptions. Ever.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(24.dp), tint = Purple60)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
