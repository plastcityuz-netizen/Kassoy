package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.onGoogleSignInClicked
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.PricingConfig
import com.example.data.ThemePreference
import com.example.ui.components.BalansLineBeam
import com.example.ui.components.BalansLogoMark
import com.example.ui.components.BalansScoreGauge
import com.example.ui.components.GirihPatternBackdrop
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricDeltaBadge
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

@Composable
fun WelcomeAuthScreen(
    themePreference: ThemePreference,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showDemoPreview by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        GirihPatternBackdrop(modifier = Modifier.fillMaxSize())

        // Subtle aurora glow strictly in hero backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.emerald.copy(alpha = 0.16f),
                            palette.violet.copy(alpha = 0.08f),
                            palette.background.copy(alpha = 0f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Floating Glass Navbar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(18.dp),
                color = palette.surface.copy(alpha = 0.90f),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BalansLogoMark(size = 34.dp)
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.elevated,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onToggleLanguage)
                                .testTag("welcome_lang_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Translate,
                                    contentDescription = "Tilni almashtirish",
                                    tint = palette.emeraldText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = lang.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textPrimary
                                )
                            }
                        }
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("welcome_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (themePreference == ThemePreference.LIGHT) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = "Tema",
                                tint = palette.textPrimary
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Section
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = palette.emerald.copy(alpha = 0.14f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.emerald.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = palette.emeraldText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = strings.heroBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.emeraldText
                                )
                            }
                        }

                        Text(
                            text = strings.heroTitle,
                            style = MaterialTheme.typography.displayLarge,
                            color = palette.textPrimary
                        )

                        BalansLineBeam(modifier = Modifier.fillMaxWidth())

                        Text(
                            text = strings.heroSubtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textMuted
                        )

                        // Interactive Cockpit Preview Glass Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentBorder = palette.emerald.copy(alpha = 0.32f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = strings.balansScoreTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.textMuted
                                    )
                                    Text(
                                        text = Formatters.formatUzs(585_500_000L, compact = true),
                                        style = MaterialTheme.typography.headlineMedium.merge(TabularNumberStyle),
                                        color = palette.textPrimary
                                    )
                                    MetricDeltaBadge(deltaPercent = 14.6)
                                }
                                BalansScoreGauge(score = 84)
                            }
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = palette.violet.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.violet.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.RU) {
                                        "AI CFO: «У 2 клиентов просрочка 48,2 млн сум. Подготовить напоминание?»"
                                    } else {
                                        "AI CFO: “2 ta mijozda 48,2 mln so‘m muddati o‘tgan qarz bor. Eslatma tayyorlansinmi?”"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isSigningIn = true
                                    authError = null
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            isSigningIn = false
                                            onAuthSuccess()
                                        },
                                        onAuthError = { err ->
                                            isSigningIn = false
                                            authError = err
                                        },
                                        scope = scope,
                                        onAuthCancelled = { isSigningIn = false }
                                    )
                                },
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.emerald,
                                    contentColor = palette.onEmerald
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("google_sign_in_button")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = palette.onEmerald,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.signingIn, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(strings.signInWithGoogle, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showDemoPreview = !showDemoPreview },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("view_demo_preview_button")
                            ) {
                                Text(strings.viewDemoFlow, color = palette.textPrimary)
                            }
                        }

                        if (authError != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = palette.danger.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.danger.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = authError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.danger,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Text(
                            text = strings.heroFootnote,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        Text(
                            text = strings.placeholderStatsNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                }

                // Expandable Demo Flow Section (DATA -> AI TAHLIL -> QAROR -> HARAKAT)
                item {
                    AnimatedVisibility(visible = showDemoPreview) {
                        GlassCard(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                            Text(
                                text = strings.flowTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                strings.flowSteps.forEachIndexed { idx, step ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = palette.elevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "0${idx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.emeraldText
                                            )
                                            Text(
                                                text = step,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = palette.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Before vs After Comparison
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.beforeAfterTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        SolidCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = strings.beforeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.danger
                            )
                            Spacer(Modifier.height(6.dp))
                            strings.beforePoints.forEach { pt ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Icon(Icons.Outlined.Close, null, tint = palette.danger, modifier = Modifier.size(16.dp))
                                    Text(pt, style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            BalansLineBeam(modifier = Modifier.fillMaxWidth(), animatePulse = false)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = strings.afterLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.emeraldText
                            )
                            Spacer(Modifier.height(6.dp))
                            strings.afterPoints.forEach { pt ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = palette.emeraldText, modifier = Modifier.size(16.dp))
                                    Text(pt, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                }
                            }
                        }
                    }
                }

                // Pricing Preview Carousel
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.pricingTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(PricingConfig.plans) { plan ->
                                SolidCard(
                                    modifier = Modifier.width(248.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = plan.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = palette.textPrimary
                                        )
                                        if (plan.isPopular) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = palette.emerald.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    text = strings.popularBadge,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.emeraldText,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.RU) plan.subtitleRu else plan.subtitleUz,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textMuted
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    val priceStr = plan.monthlyPriceUzs?.let { Formatters.formatUzs(it) }
                                        ?: if (lang == AppLanguage.RU) "По договору" else "Kelishuv asosida"
                                    Text(
                                        text = priceStr,
                                        style = MaterialTheme.typography.headlineMedium.merge(TabularNumberStyle),
                                        color = palette.emeraldText
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    val features = if (lang == AppLanguage.RU) plan.featuresRu else plan.featuresUz
                                    features.forEach { feat ->
                                        Text(
                                            text = "• $feat",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textPrimary,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Footer Notice (Strictly truthful, no fake certifications)
                item {
                    SolidCard(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.Lock, null, tint = palette.emeraldText)
                            Text(
                                text = if (lang == AppLanguage.RU) {
                                    "Защита: Google Sign-In, ролевой доступ (RBAC), журнал аудита и резервные правила доступа."
                                } else {
                                    "Xavfsizlik: Google Sign-In, rollar va ruxsatlar (RBAC), audit jurnali va himoyalangan bulut qoidalari."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
